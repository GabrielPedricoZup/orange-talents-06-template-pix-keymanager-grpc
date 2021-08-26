package com.zupedu.gabrielpedrico.dtos.registra

import com.zupedu.gabrielpedrico.RegistraChavePixRequest
import com.zupedu.gabrielpedrico.TipoDeChave
import com.zupedu.gabrielpedrico.TipoDeConta
import com.zupedu.gabrielpedrico.dtos.registra.NovaChavePix
import java.lang.IllegalArgumentException

fun RegistraChavePixRequest.paraNovaChavePix () : NovaChavePix {
    return NovaChavePix(
        clienteId = clientId,
        tipo = when (tipoDeChave){
            TipoDeChave.UNKNOWN_CHAVE -> throw IllegalArgumentException("Tipo de Chave desconhecida")
            else -> com.zupedu.gabrielpedrico.enums.TipoDeChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta) {
            TipoDeConta.UNKNOWN_TIPO_CONTA -> throw IllegalArgumentException("Tipo de conta desconhecida")
            else -> TipoDeConta.valueOf(tipoDeContaValue)
        }

    )
}