package com.zupedu.gabrielpedrico.endpoints.deleta

import com.zupedu.gabrielpedrico.DeletaChavePixRequest
import com.zupedu.gabrielpedrico.DeletaPixGrpcServiceGrpc
import com.zupedu.gabrielpedrico.TipoDeConta
import com.zupedu.gabrielpedrico.dtos.deleta.BcbDeletaChavePixRequest
import com.zupedu.gabrielpedrico.enums.TipoDeChave
import com.zupedu.gabrielpedrico.integrations.*
import com.zupedu.gabrielpedrico.models.ChavePix
import com.zupedu.gabrielpedrico.repositories.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class DeletaChaveEndPointTest(
    val repository: ChavePixRepository,
    val grpcClient: DeletaPixGrpcServiceGrpc.DeletaPixGrpcServiceBlockingStub,
    @Inject val itauClient: ContasDeClientesNoItauClient,
    @Inject val bcbClient:BcbClient
) {
    @MockBean(ContasDeClientesNoItauClient::class)
    fun itauClient(): ContasDeClientesNoItauClient? {
        return Mockito.mock(ContasDeClientesNoItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class DeleteClients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): DeletaPixGrpcServiceGrpc.DeletaPixGrpcServiceBlockingStub {
            return DeletaPixGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    lateinit var CHAVE_EXISTENTE: ChavePix
    lateinit var CHAVE_INEXISTENTE: String

    companion object {
        val CLIENT_ID = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890")
    }

    @BeforeEach
    fun setup() {
        CHAVE_EXISTENTE = repository.save(
            ChavePix(
                tipo = TipoDeChave.CPF,
                chave = "34939607860",
                clienteId = CLIENT_ID,
                tipoDeConta = TipoDeConta.CONTA_CORRENTE,
                conta = dadosDaContaResponse().paraContaAssociada()
            )
        )

        CHAVE_INEXISTENTE = UUID.randomUUID().toString()
    }

    @AfterEach
    fun cleanup() {
        repository.deleteAll()
    }

    @Test
    fun `deve deletar chave pix`() {
        //ação
        Mockito.`when`(
            itauClient.buscaConta(
                clienteId = CLIENT_ID.toString()
            )
        )
            .thenReturn(HttpResponse.ok(dadosDoClienteResponse()))

        Mockito.`when`(
            bcbClient.deletaConta("34939607860", BcbDeletaChavePixRequest("34939607860"))
        )
            .thenReturn(HttpResponse.ok())

        val response = grpcClient.deleta(
            DeletaChavePixRequest.newBuilder()
                .setClientId(CHAVE_EXISTENTE.clienteId.toString())
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .build()
        )

        //validação
        assertEquals(CHAVE_EXISTENTE.id.toString(), response.pixId)
        assertEquals(CHAVE_EXISTENTE.clienteId.toString(), response.clientId)
    }

    @Test
    fun `nao deve deletar chave pix quando nao existir o cliente no sistema do itau`() {
        //ação
        Mockito.`when`(
            itauClient.buscaConta(
                clienteId = "dcb6eee3-3878-4749-a0f4-ef530584205e"
            )
        )
            .thenReturn(HttpResponse.ok(dadosDoClienteResponse()))

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(
                DeletaChavePixRequest.newBuilder()
                    .setClientId(CHAVE_EXISTENTE.clienteId.toString())
                    .setPixId(CHAVE_INEXISTENTE)
                    .build()
            )
        }

        //validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não existente",status.description)
        }
    }

    @Test
    fun `nao deve deletar chave pix quando chave pix nao pertencer a cliente`(){
        //cenario
        val outroClienteId = UUID.randomUUID().toString()

        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(DeletaChavePixRequest.newBuilder()
                                .setClientId(outroClienteId)
                                .setPixId(CHAVE_EXISTENTE.id.toString())
                                .build())
        }

        with(thrown){
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertEquals("Chave pix não pertence ao cliente informado",status.description)
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

    private fun dadosDoClienteResponse(): DadosDoClienteResponse {
        return DadosDoClienteResponse(
            id =CHAVE_EXISTENTE.clienteId.toString(),
            nome ="Rafael Ponte",
            cpf ="34939607860",
            instituicao =InstituicaoResponse("UNIBANCO ITAU SA", "ITAU_UNIBANCO_ISBP")
        )
    }
}