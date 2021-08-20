package com.zupedu.gabrielpedrico.endpoints.deleta

import com.zupedu.gabrielpedrico.DeletaChavePixRequest
import com.zupedu.gabrielpedrico.DeletaChavePixResponse
import com.zupedu.gabrielpedrico.DeletaPixGrpcServiceGrpc
import com.zupedu.gabrielpedrico.handlers.ChavePixExistenteException
import com.zupedu.gabrielpedrico.integrations.ContasDeClientesNoItauClient
import com.zupedu.gabrielpedrico.repositories.ChavePixRepository
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator


@Singleton
class DeletaChaveEndPoint(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ContasDeClientesNoItauClient,
    @Inject val validator: Validator
) : DeletaPixGrpcServiceGrpc.DeletaPixGrpcServiceImplBase() {

    override fun deleta(request: DeletaChavePixRequest?,
                        responseObserver: StreamObserver<DeletaChavePixResponse>?) {

        val deletaChave = request?.paraDeletaChavePix(validator)
        if(repository.findById(UUID.fromString(request?.pixId.toString())).isEmpty) throw IllegalStateException("Chave pix não existente")
        val response = itauClient.buscaConta(deletaChave?.clienteId.toString())
        val conta = response.body() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        repository.deleteById(UUID.fromString(deletaChave?.chavePix))


        responseObserver?.onNext(
            DeletaChavePixResponse.newBuilder()
                .setClientId(deletaChave?.clienteId.toString())
                .setPixId(request?.pixId.toString())
                .build()
        )
        responseObserver?.onCompleted()
    }
}