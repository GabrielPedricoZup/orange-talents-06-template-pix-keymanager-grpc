package com.zupedu.gabrielpedrico.endpoints.registra

import com.zupedu.gabrielpedrico.RegistraChavePixRequest
import com.zupedu.gabrielpedrico.RegistraChavePixResponse
import com.zupedu.gabrielpedrico.RegistraPixGrpcServiceGrpc
import com.zupedu.gabrielpedrico.dtos.registra.paraNovaChavePix
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegistraChaveEndpoint(@Inject private val service: NovaChavePixService) :
    RegistraPixGrpcServiceGrpc.RegistraPixGrpcServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest?,
        responseObserver: StreamObserver<RegistraChavePixResponse>?
    ) {
        val novaChave = request?.paraNovaChavePix()
        val chaveCriada = novaChave?.let { service.registra(it) }

        responseObserver?.onNext(RegistraChavePixResponse.newBuilder()
                                    .setClientId(chaveCriada?.clienteId.toString())
                                    .setPixId(chaveCriada?.id.toString())
                                    .build())
        responseObserver?.onCompleted()
    }
}