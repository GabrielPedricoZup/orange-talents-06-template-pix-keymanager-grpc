package com.zupedu.gabrielpedrico.handlers

import com.zupedu.gabrielpedrico.handlers.ExceptionHandler.StatusWithDetails

/**
 * By design, this class must NOT be managed by Micronaut
 */

class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(e: Exception): StatusWithDetails {
        val status = when (e) {
            is IllegalArgumentException -> invalidArgumentHandler(e.message)
            is IllegalStateException -> failedPreconditionHandler(e.message)
            is  ChavePixExistenteException -> alreadyExistsHandler(e.message)
            is ChavePixNaoPertenceUsuarioException -> acessDenied(e.message)
            is ChavePixNaoExistenteException -> notFound(e.message)
            else -> defaultHandler(e.message)
        }
        return StatusWithDetails(status)
    }

    override fun supports(e: Exception): Boolean {
        return true
    }

}