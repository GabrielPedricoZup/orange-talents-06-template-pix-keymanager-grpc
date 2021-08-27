package com.zupedu.gabrielpedrico.endpoints.consulta

import com.google.protobuf.Timestamp
import com.zupedu.gabrielpedrico.ListaChavesGrpcServiceGrpc
import com.zupedu.gabrielpedrico.ListarChavesRequest
import com.zupedu.gabrielpedrico.ListarChavesResponse
import com.zupedu.gabrielpedrico.dtos.consulta.paraListaChavesRequest
import com.zupedu.gabrielpedrico.handlers.ChavePixNaoExistenteException
import com.zupedu.gabrielpedrico.integrations.ContasDeClientesNoItauClient
import com.zupedu.gabrielpedrico.repositories.ChavePixRepository
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
class ListarChavesEndPoint(
    private val repository: ChavePixRepository,
    private val validator: Validator,
    private val itauClient: ContasDeClientesNoItauClient
) : ListaChavesGrpcServiceGrpc.ListaChavesGrpcServiceImplBase() {


    override fun listaChaves(request: ListarChavesRequest?, responseObserver: StreamObserver<ListarChavesResponse>?) {
        //validar se a request não esta nula nem vazia
        var possivelRequest = request?.paraListaChavesRequest(validator)
        //Verifica se clientId existe no ERP do itau caso não exista Exception NOT_FOUND
        var response = itauClient.buscaConta(possivelRequest!!.clientId)
        var status = response.body() ?: throw ChavePixNaoExistenteException("ClientId não encontrado no ERP do itau")
        //Busca dados no repositorio para exibição
        val chavesPix = repository.findAllByClienteId(UUID.fromString(possivelRequest.clientId.toString())).map{
                chavePix->
            ListarChavesResponse.ChavePix.newBuilder()
                .setClientId(chavePix.clienteId.toString())
                .setPixId(chavePix.id.toString())
                .setValorDaChave(chavePix.chave)
                .setTipoDeChave(chavePix.tipo.toString())
                .setCreatedAt(Timestamp.newBuilder()
                                    .setSeconds(chavePix.criadaEm.atZone(ZoneId.of("UTC")).toInstant().epochSecond)
                                    .setNanos(chavePix.criadaEm.atZone(ZoneId.of("UTC")).toInstant().nano)
                                    .build())

                .build()
        }

        responseObserver?.onNext(ListarChavesResponse.newBuilder()
            .addAllChavePix(chavesPix)
            .build())
            responseObserver?.onCompleted()



    }
}