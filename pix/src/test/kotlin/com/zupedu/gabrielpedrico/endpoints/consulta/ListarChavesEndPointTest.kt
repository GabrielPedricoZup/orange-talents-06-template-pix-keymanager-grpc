package com.zupedu.gabrielpedrico.endpoints.consulta

import com.zupedu.gabrielpedrico.ListaChavesGrpcServiceGrpc
import com.zupedu.gabrielpedrico.ListarChavesRequest
import com.zupedu.gabrielpedrico.TipoDeConta
import com.zupedu.gabrielpedrico.enums.TipoDeChave
import com.zupedu.gabrielpedrico.integrations.*
import com.zupedu.gabrielpedrico.models.ChavePix
import com.zupedu.gabrielpedrico.repositories.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListarChavesEndPointTest(
    val repository: ChavePixRepository,
    val grpcClient: ListaChavesGrpcServiceGrpc.ListaChavesGrpcServiceBlockingStub,
    val itauClient: ContasDeClientesNoItauClient
) {

    lateinit var CHAVE_EXISTENTE:ChavePix
    lateinit var BLANK_CLIENTID:String
    lateinit var INVALID_CLIENTID:String

    @BeforeEach
    fun setup() {
        repository.deleteAll()
        CHAVE_EXISTENTE = repository.save(
            ChavePix(
                tipo = TipoDeChave.EMAIL,
                chave = "gabriel.pedrico@zup.com.br",
                clienteId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
                tipoDeConta = TipoDeConta.CONTA_CORRENTE,
                conta = dadosDaContaResponse().paraContaAssociada()
            )
        )

        BLANK_CLIENTID = ""
        INVALID_CLIENTID = UUID.randomUUID().toString()
    }

    @MockBean(ContasDeClientesNoItauClient::class)
    fun bcbMock(): ContasDeClientesNoItauClient = Mockito.mock(ContasDeClientesNoItauClient::class.java)


    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ListaChavesGrpcServiceGrpc.ListaChavesGrpcServiceBlockingStub {
            return ListaChavesGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    //Happy patch =D
    @Test
    fun `deve consultar chaves atraves de clientId`() {
        //cenario
        Mockito.`when`(
            itauClient.buscaConta(CHAVE_EXISTENTE.clienteId.toString())
        ).thenReturn(HttpResponse.ok(dadosDoClienteResponse()))

        //acao
        var request = ListarChavesRequest.newBuilder()
            .setClientId(CHAVE_EXISTENTE.clienteId.toString())
            .build()

        var response = grpcClient.listaChaves(request)

        //validacao
        with(response) {
            assertNotNull(response.chavePixList)
        }
    }

    //Bad patch =,(

    @Test
    fun `nao deve consultar chaves atraves de clientId quando clientId nulo ou vazio`() {
        //cenario
        Mockito.`when`(
            itauClient.buscaConta(BLANK_CLIENTID)
        ).thenReturn(HttpResponse.ok(dadosDoClienteResponse()))

        //acao
        var request = ListarChavesRequest.newBuilder()
            .setClientId(BLANK_CLIENTID)
            .build()

        var thrown = assertThrows<StatusRuntimeException> {grpcClient.listaChaves(request)}

        //validacao
        with(thrown) {
            assertEquals(Status.UNKNOWN.code, status.code)
            assertEquals("clientId: não deve estar em branco", status.description)
        }
    }

    @Test
    fun `nao deve consultar chaves atraves de clientId quando clientId nao existir no BCB`() {
        var clientIdInexistente = UUID.randomUUID().toString()
        //cenario
        Mockito.`when`(
            itauClient.buscaConta(clientIdInexistente)
        ).thenReturn(HttpResponse.notFound())

        //acao
        var request = ListarChavesRequest.newBuilder()
            .setClientId(clientIdInexistente)
            .build()

        var thrown = assertThrows<StatusRuntimeException> {grpcClient.listaChaves(request)}

        //validacao
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("ClientId não encontrado no ERP do itau", status.description)
        }
    }

    private fun dadosDaContaResponse(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", "ITAU_UNIBANCO_ISBP"),
            agencia = "1218",
            numero = "291900",
            titular = TitularResponse("973d90e9-ba09-418d-aa1e-785cf2f41350", "RAFAEL M C PONTE", "41531952860")
        )
    }

    private fun dadosDoClienteResponse(): DadosDoClienteResponse{
        return DadosDoClienteResponse(id=CHAVE_EXISTENTE.clienteId.toString(),
                                        nome="RAFAEL M C PONTE",
                                        cpf = "41531952860",
                                        instituicao = InstituicaoResponse("UNIBANCO ITAU SA", "ITAU_UNIBANCO_ISBP"))
    }
}