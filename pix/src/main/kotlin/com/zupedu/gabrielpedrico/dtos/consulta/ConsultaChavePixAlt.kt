package com.zupedu.gabrielpedrico.dtos.consulta

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
data class ConsultaChavePixAlt(
    @field:NotBlank
    @field:Size(max = 77)
    var chavePix:String)
