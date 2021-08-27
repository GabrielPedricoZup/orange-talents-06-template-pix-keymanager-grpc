package com.zupedu.gabrielpedrico.endpoints.consulta

import com.zupedu.gabrielpedrico.ConsultaChavePixRequest
import com.zupedu.gabrielpedrico.ConsultaPixGrpcServiceGrpc
import com.zupedu.gabrielpedrico.TipoDeConta
import com.zupedu.gabrielpedrico.dtos.consulta.BankAccount
import com.zupedu.gabrielpedrico.dtos.consulta.ConsultaChavePixBcbResponse
import com.zupedu.gabrielpedrico.dtos.consulta.Owner
import com.zupedu.gabrielpedrico.enums.TipoDeChave
import com.zupedu.gabrielpedrico.integrations.BcbClient
import com.zupedu.gabrielpedrico.integrations.DadosDaContaResponse
import com.zupedu.gabrielpedrico.integrations.InstituicaoResponse
import com.zupedu.gabrielpedrico.integrations.TitularResponse
import com.zupedu.gabrielpedrico.models.ChavePix
import com.zupedu.gabrielpedrico.repositories.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton
import org.junit.jupiter.api.assertThrows
import io.grpc.Status

@MicronautTest(transactional = false)
internal class ConsultaChaveEndPointTest(
    val repository: ChavePixRepository,
    val grpcClient: ConsultaPixGrpcServiceGrpc.ConsultaPixGrpcServiceBlockingStub,
    val bcbClient: BcbClient
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    lateinit var CHAVE_EXISTENTE:ChavePix
    lateinit var BLANK_PIXID:String
    lateinit var BLANK_CLIENTID:String

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
        BLANK_PIXID = ""
    }

    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient = mock(BcbClient::class.java)


    @Factory
    class ConsultaClients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ConsultaPixGrpcServiceGrpc.ConsultaPixGrpcServiceBlockingStub {
            return ConsultaPixGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    //Happy Patch =D
    @Test
    fun `deve consultar dados atraves de clientId e pixId`(){
        //Cenario
        Mockito.`when`(
            bcbClient.consultaChave(CHAVE_EXISTENTE.chave)).thenReturn(HttpResponse.ok(consultaChavePixBcbResponse()))

        //acao
        val request = ConsultaChavePixRequest.newBuilder()
                            .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setPixId(CHAVE_EXISTENTE.id.toString())
                            .setClientId(CHAVE_EXISTENTE.clienteId.toString()))
                            .build()

        val response = grpcClient.consulta(request)

        //validacao
        with(response) {
            assertNotNull(response)
            assertNotNull(request.pixId.clientId)
            assertNotNull(request.pixId.pixId)
            assertEquals(response.keyType,"EMAIL")
            assertEquals(response.key,CHAVE_EXISTENTE.chave.toString())
            assertEquals(response.bankAccount.participant,"60701190")
            assertEquals(response.bankAccount.branch,"0001")
            assertEquals(response.bankAccount.accountNumber,"291900")
            assertEquals(response.bankAccount.accountType,"CACC")
            assertEquals(response.owner.type,"NATURAL_PERSON")
            assertEquals(response.owner.name,"Rafael M C Ponte")
            assertEquals(response.owner.taxIdNumber,"02467781054")
        }
    }

    @Test
    fun `deve consultar dados atraves da chavePix apenas`(){
        //Cenario
        Mockito.`when`(
            bcbClient.consultaChave(CHAVE_EXISTENTE.chave)).thenReturn(HttpResponse.ok(consultaChavePixBcbResponse()))

        //acao
        val request = ConsultaChavePixRequest.newBuilder()
            .setChavePix(CHAVE_EXISTENTE.chave)
            .build()

        val response = grpcClient.consulta(request)

        //validacao
        with(response) {
            assertNotNull(response)
            assertNotNull(request.pixId.clientId)
            assertNotNull(request.pixId.pixId)
            assertEquals(response.keyType,"EMAIL")
            assertEquals(response.key,CHAVE_EXISTENTE.chave.toString())
            assertEquals(response.bankAccount.participant,"60701190")
            assertEquals(response.bankAccount.branch,"0001")
            assertEquals(response.bankAccount.accountNumber,"291900")
            assertEquals(response.bankAccount.accountType,"CACC")
            assertEquals(response.owner.type,"NATURAL_PERSON")
            assertEquals(response.owner.name,"Rafael M C Ponte")
            assertEquals(response.owner.taxIdNumber,"02467781054")
        }
    }

    //Bad Patch =,(
    @Test
    fun `nao deve consultar dados atraves de clientId & pixId quando clientId nulo`(){
        //cenario
        val request = ConsultaChavePixRequest.newBuilder()
            .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                .setPixId(CHAVE_EXISTENTE.id.toString())
            ).build()

        //acao
        var thrown = assertThrows<StatusRuntimeException>{ grpcClient.consulta(request)}

        //validacao
        with(thrown) {
            assertEquals(Status.UNKNOWN.code, status.code)
            assertEquals("UNKNOWN: clientId: não deve estar em branco", this.message)
        }
    }

    @Test
    fun `nao deve consultar dados atraves de clientId & pixId quando pixId nulo`(){
        //cenario
        val request = ConsultaChavePixRequest.newBuilder()
            .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                .setClientId(CHAVE_EXISTENTE.clienteId.toString())
            ).build()

        //acao
        var thrown = assertThrows<StatusRuntimeException>{ grpcClient.consulta(request)}

        //validacao
        with(thrown) {
            assertEquals(Status.UNKNOWN.code, status.code)
        }
    }

    @Test
    fun `nao deve consultar dados atraves de clientId & pixId quando pixId nao for UUID valido`(){
        //cenario
        val request = ConsultaChavePixRequest.newBuilder()
            .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                .setClientId(CHAVE_EXISTENTE.clienteId.toString())
                .setPixId("12312123pixinvalido")
            ).build()

        //acao
        var thrown = assertThrows<StatusRuntimeException>{ grpcClient.consulta(request)}

        //validacao
        with(thrown) {
            assertEquals(Status.UNKNOWN.code, status.code)
            assertEquals("UNKNOWN: pixId: não é um formato válido UUID", this.message)
        }
    }

    @Test
    fun `nao deve consultar dados atraves de clientId & pixId quando pixId nao existir`(){
        //cenario
        val request = ConsultaChavePixRequest.newBuilder()
            .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                .setClientId(CHAVE_EXISTENTE.clienteId.toString())
                .setPixId(UUID.randomUUID().toString())
            ).build()

        //acao
        var thrown = assertThrows<StatusRuntimeException>{ grpcClient.consulta(request)}

        //validacao
        with(thrown) {
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertEquals("PERMISSION_DENIED: Chave pix não pertence ao cliente informado", this.message)
        }
    }

    @Test
    fun `nao deve consultar dados atraves de clientId & pixId quando pixId nao existir no BCB`(){
        //cenario

        Mockito.`when`(
            bcbClient.consultaChave(CHAVE_EXISTENTE.chave)).thenReturn(HttpResponse.notFound())

        val request = ConsultaChavePixRequest.newBuilder()
            .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                .setClientId(CHAVE_EXISTENTE.clienteId.toString())
                .setPixId(CHAVE_EXISTENTE.id.toString())
            ).build()

        //acao
        var thrown = assertThrows<StatusRuntimeException>{ grpcClient.consulta(request)}

        //validacao
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("NOT_FOUND: Chave inexistente", this.message)
        }
    }

    @Test
    fun `nao deve consultar dados atraves da chavePix apenas, quando chave maior 77 caracteres`(){
        //cenario
        val request = ConsultaChavePixRequest.newBuilder()
            .setChavePix("uma_chave_pix_exageradamente_desnecessariamente_incrivelmente_grande@trollei.com")
            .build()

        val thrown = assertThrows<StatusRuntimeException>{ grpcClient.consulta(request)}

        //validacao
        with(thrown) {
            assertEquals(Status.UNKNOWN.code, status.code)
            assertEquals("UNKNOWN: chavePix: tamanho deve ser entre 0 e 77", this.message)
        }
    }

    @Test
    fun `nao deve consultar dados atraves da chavePix apenas, quando chave nao existir no BCB`(){
        //cenario

        Mockito.`when`(
            bcbClient.consultaChave("umachavepequena_porem_naotemno_bcb@test.com")).thenReturn(HttpResponse.notFound())

        val request = ConsultaChavePixRequest.newBuilder()
            .setChavePix("umachavepequena_porem_naotemno_bcb@test.com")
            .build()

        val thrown = assertThrows<StatusRuntimeException>{ grpcClient.consulta(request)}

        //validacao
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("NOT_FOUND: Chave inexistente", this.message)
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

    private fun consultaChavePixBcbResponse(): ConsultaChavePixBcbResponse {
        return ConsultaChavePixBcbResponse(
                keyType = "EMAIL",
                key = CHAVE_EXISTENTE.chave.toString(),
                bankAccount = BankAccount(participant = "60701190",
                                            branch = "0001",
                                            accountNumber ="291900",
                                            accountType = "CACC"),
                owner = Owner(type ="NATURAL_PERSON" ,
                                name ="Rafael M C Ponte" ,
                                taxIdNumber = "02467781054"),
                createdAt = LocalDateTime.now()
        )
    }
}