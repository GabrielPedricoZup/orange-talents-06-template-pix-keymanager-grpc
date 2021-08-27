package com.zupedu.gabrielpedrico.endpoints.consulta

import com.google.protobuf.Timestamp
import com.zupedu.gabrielpedrico.*
import com.zupedu.gabrielpedrico.dtos.consulta.ConsultaChavePixBcbResponse
import com.zupedu.gabrielpedrico.dtos.consulta.paraConsultaChavePixRequest
import com.zupedu.gabrielpedrico.dtos.consulta.paraConsultaChavePixRequestAlt
import com.zupedu.gabrielpedrico.handlers.ChavePixNaoExistenteException
import com.zupedu.gabrielpedrico.handlers.ChavePixNaoPertenceUsuarioException
import com.zupedu.gabrielpedrico.integrations.BcbClient
import com.zupedu.gabrielpedrico.repositories.ChavePixRepository
import io.grpc.stub.StreamObserver
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator


@Singleton
class ConsultaChaveEndPoint(
    private val validator: Validator,
    private val bcbClient: BcbClient,
    private val repository: ChavePixRepository,
) : ConsultaPixGrpcServiceGrpc.ConsultaPixGrpcServiceImplBase() {

    private lateinit var possivelResponse: ConsultaChavePixBcbResponse
    override fun consulta(
        request: ConsultaChavePixRequest?,
        responseObserver: StreamObserver<ConsultaChavePixResponse>?
    ) {
        //Caso o request seja por pixId & clientId
        if (request?.hasPixId()!!) {
            //Verifico se o request está válido de acordo com o solicitado pela feature
            var validations = request.paraConsultaChavePixRequest(validator)
            //Consulta o repositorio afim de verificar se chave pertence ao Cliente informado
            if (!repository.existsByClienteIdAndId(UUID.fromString(request?.pixId?.clientId), UUID.fromString(request?.pixId?.pixId))) throw ChavePixNaoPertenceUsuarioException("Chave pix não pertence ao cliente informado")
            //Consultar BCB para pesquisar chave pix caso nao existir exception NOT_FOUND
            var chavePix = repository.findById(UUID.fromString(request?.pixId?.pixId)).get()
            possivelResponse = bcbClient.consultaChave(chavePix?.chave.toString()).body() ?: throw ChavePixNaoExistenteException("Chave inexistente")
        }
        //Caso o request seja chavePix
        if (request?.hasChavePix()!!) {
            var validations = request.paraConsultaChavePixRequestAlt(validator)
                possivelResponse = bcbClient.consultaChave(request?.chavePix.toString()).body() ?: throw ChavePixNaoExistenteException("Chave inexistente")
        }
        //Caso ache a chave pix retornar response de acordo com requisitado na feature
        responseObserver?.onNext(
            ConsultaChavePixResponse.newBuilder()
                .setKey(possivelResponse?.key)
                .setKeyType(possivelResponse?.keyType)
                .setBankAccount(
                    BankAccount.newBuilder()
                        .setParticipant(possivelResponse?.bankAccount?.participant)
                        .setAccountType(possivelResponse?.bankAccount?.accountType)
                        .setAccountNumber(possivelResponse?.bankAccount?.accountNumber)
                        .setBranch(possivelResponse?.bankAccount?.branch).build()
                )
                .setOwner(
                    Owner.newBuilder()
                        .setName(possivelResponse?.owner?.name)
                        .setType(possivelResponse?.owner?.type)
                        .setTaxIdNumber(possivelResponse?.owner?.taxIdNumber).build()
                )
                .setCreatedAt(
                    Timestamp.newBuilder()
                        .setNanos(possivelResponse?.createdAt?.toInstant(ZoneOffset.UTC)!!.nano)
                        .setSeconds(possivelResponse?.createdAt?.toInstant(ZoneOffset.UTC)!!.epochSecond).build()
                )
                .build()
        )
        responseObserver?.onCompleted()
    }


}