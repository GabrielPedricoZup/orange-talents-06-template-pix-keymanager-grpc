package com.zupedu.gabrielpedrico.endpoints.registra

import com.zupedu.gabrielpedrico.RegistraChavePixRequest
import com.zupedu.gabrielpedrico.TipoDeChave
import com.zupedu.gabrielpedrico.TipoDeConta
import com.zupedu.gabrielpedrico.dtos.NovaChavePix

fun RegistraChavePixRequest.paraNovaChavePix () : NovaChavePix {
    return NovaChavePix(
        clienteId = clientId,
        tipo = when (tipoDeChave){
            TipoDeChave.UNKNOWN_CHAVE -> null
            else -> TipoDeChave.valueOf(tipoDeChaveValue)
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta) {
            TipoDeConta.UNKNOWN_TIPO_CONTA -> null
            else -> TipoDeConta.valueOf(tipoDeContaValue)
        }

    )
}