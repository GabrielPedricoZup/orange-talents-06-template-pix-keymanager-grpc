package com.zupedu.gabrielpedrico.endpoints.deleta

import com.zupedu.gabrielpedrico.DeletaChavePixRequest
import com.zupedu.gabrielpedrico.DeletaChavePixResponse
import com.zupedu.gabrielpedrico.DeletaPixGrpcServiceGrpc
import com.zupedu.gabrielpedrico.dtos.deleta.BcbDeletaChavePixRequest
import com.zupedu.gabrielpedrico.dtos.deleta.paraDeletaChavePix
import com.zupedu.gabrielpedrico.handlers.BcbErroException
import com.zupedu.gabrielpedrico.handlers.ChavePixNaoExistenteException
import com.zupedu.gabrielpedrico.handlers.ChavePixNaoPertenceUsuarioException
import com.zupedu.gabrielpedrico.integrations.BcbClient
import com.zupedu.gabrielpedrico.integrations.ContasDeClientesNoItauClient
import com.zupedu.gabrielpedrico.repositories.ChavePixRepository
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator


@Singleton
class DeletaChaveEndPoint(
    private val repository: ChavePixRepository,
    private val itauClient: ContasDeClientesNoItauClient,
    private val validator: Validator,
    private val bcbClient: BcbClient
) : DeletaPixGrpcServiceGrpc.DeletaPixGrpcServiceImplBase() {

    override fun deleta(request: DeletaChavePixRequest?,
                        responseObserver: StreamObserver<DeletaChavePixResponse>?,) {

        val deletaChave = request?.paraDeletaChavePix(validator)
        if(repository.findById(UUID.fromString(request?.pixId.toString())).isEmpty) throw ChavePixNaoExistenteException("Chave pix não existente")
        if(!repository.existsByIdAndClienteId(UUID.fromString(request?.pixId.toString()),UUID.fromString(request?.clientId.toString()))) throw ChavePixNaoPertenceUsuarioException("Chave pix não pertence ao cliente informado")
        val response = itauClient.buscaConta(deletaChave?.clienteId.toString())
        val conta = response.body() ?: throw IllegalStateException("Cliente não encontrado no Itau")
        val chavePix = repository.findById(UUID.fromString(deletaChave?.chavePix)).get()
        val status = bcbClient.deletaConta(chavePix.chave, BcbDeletaChavePixRequest(chavePix?.chave.toString()))
        if(status.code() != 200) throw BcbErroException("Falha na comunicação com ERP do BCB")
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
