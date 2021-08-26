package com.zupedu.gabrielpedrico.dtos.consulta

import com.zupedu.gabrielpedrico.ListarChavesRequest
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ListarChavesRequest.paraListaChavesRequest(validator:Validator): ListaChavesRequest{

    val listaChavesPix = ListaChavesRequest(clientId = clientId)

    val erros = validator.validate(listaChavesPix)
    if (erros.isNotEmpty()) {
        throw ConstraintViolationException(erros)
    }

    return listaChavesPix
}