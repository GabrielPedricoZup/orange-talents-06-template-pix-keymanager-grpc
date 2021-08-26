package com.zupedu.gabrielpedrico.dtos.deleta

import com.zupedu.gabrielpedrico.DeletaChavePixRequest
import com.zupedu.gabrielpedrico.dtos.deleta.DeletaChavePix
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun DeletaChavePixRequest.paraDeletaChavePix (validador: Validator) : DeletaChavePix {

        val deletaChavePix = DeletaChavePix(
        clienteId = clientId,
        chavePix = pixId)

    val erros = validador.validate(deletaChavePix)
    if (erros.isNotEmpty()) {
        throw ConstraintViolationException(erros)
    }

    return deletaChavePix
}