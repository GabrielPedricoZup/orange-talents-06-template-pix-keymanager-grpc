package com.zupedu.gabrielpedrico.endpoints.consulta

import com.zupedu.gabrielpedrico.ConsultaChavePixRequest
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ConsultaChavePixRequest.paraConsultaChavePixRequest(validador: Validator): ConsultaChavePix{

   val consultaChavePix = ConsultaChavePix(clientId = pixId.clientId,
                        pixId = pixId.pixId)

    val erros = validador.validate(consultaChavePix)
    if (erros.isNotEmpty()) {
        throw ConstraintViolationException(erros)
    }

    return consultaChavePix

}
//Para o fluxo alternativo
fun ConsultaChavePixRequest.paraConsultaChavePixRequestAlt(validador: Validator): ConsultaChavePixAlt{

    val consultaChavePixAlt = ConsultaChavePixAlt(chavePix = chavePix)

    val erros = validador.validate(consultaChavePixAlt)
    if (erros.isNotEmpty()) {
        throw ConstraintViolationException(erros)
    }

    return consultaChavePixAlt

}