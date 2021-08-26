package com.zupedu.gabrielpedrico.dtos.consulta

import com.zupedu.gabrielpedrico.validations.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class ConsultaChavePix(
    @field:NotBlank
    var clientId: String,
    @field:NotBlank
    @field:ValidUUID
    var pixId: String
)
