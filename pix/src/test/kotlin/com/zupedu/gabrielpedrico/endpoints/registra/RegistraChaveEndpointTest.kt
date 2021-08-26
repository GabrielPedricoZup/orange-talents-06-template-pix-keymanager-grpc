package com.zupedu.gabrielpedrico.endpoints.registra

import com.zupedu.gabrielpedrico.RegistraChavePixRequest
import com.zupedu.gabrielpedrico.RegistraPixGrpcServiceGrpc
import com.zupedu.gabrielpedrico.TipoDeChave
import com.zupedu.gabrielpedrico.TipoDeConta
import com.zupedu.gabrielpedrico.dtos.registra.BankAccount
import com.zupedu.gabrielpedrico.dtos.registra.BcbChavePixRequest
import com.zupedu.gabrielpedrico.dtos.registra.Owner
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
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: RegistraPixGrpcServiceGrpc.RegistraPixGrpcServiceBlockingStub
) {

    @Inject
    lateinit var itauClient: ContasDeClientesNoItauClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar nova chave pix`() {
        //cenario
        Mockito.`when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        Mockito.`when`(
            bcbClient().registraConta(
                BcbChavePixRequest(
                    com.zupedu.gabrielpedrico.enums.TipoDeChave.EMAIL,
                    "gabriel.pedrico@zup.com.br",
                    BankAccount(
                        "60701190",
                        "1218",
                        "291900",
                        "CACC"
                    ),
                    Owner(
                        "NATURAL_PERSON",
                        "RAFAEL M C PONTE",
                        "02467781054"
                    )
                )
            )).thenReturn(HttpResponse.ok())

            //ação
            val response = grpcClient . registra (
                RegistraChavePixRequest.newBuilder()
                    .setClientId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChave("gabriel.pedrico@zup.com.br")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
                )
        //validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clientId)
            assertNotNull(pixId)
        }

    }

    @Test
    fun `nao deve registrar chave pix existente`() {
        //cenário
        repository.save(
            ChavePix(
                tipo = com.zupedu.gabrielpedrico.enums.TipoDeChave.CPF,
                chave = "41531952860",
                clienteId = CLIENTE_ID,
                tipoDeConta = TipoDeConta.CONTA_CORRENTE,
                conta = dadosDaContaResponse().paraContaAssociada()

            )
        )

        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClientId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave("41531952860")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        //validação
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '41531952860' existente", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao encontrar dados da conta cliente`() {
        //cenário
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClientId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChave("pedripedrio@gmail.com")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }
        //validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itau", status.description)
        }
    }

    fun `nao deve registrar chave pix quando parametros forem invalidos`() {
        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder().build())
        }
        //validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @MockBean(ContasDeClientesNoItauClient::class)
    fun itauClient(): ContasDeClientesNoItauClient? {
        return Mockito.mock(ContasDeClientesNoItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RegistraPixGrpcServiceGrpc.RegistraPixGrpcServiceBlockingStub {
            return RegistraPixGrpcServiceGrpc.newBlockingStub(channel)
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
}