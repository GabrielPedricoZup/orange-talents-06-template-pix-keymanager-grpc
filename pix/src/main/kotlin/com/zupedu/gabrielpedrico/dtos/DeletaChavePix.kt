package com.zupedu.gabrielpedrico.dtos

import com.zupedu.gabrielpedrico.validations.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class DeletaChavePix(
                            @field:ValidUUID
                            @field:NotBlank
                            var clienteId: String,

                            @field:NotBlank
                            var chavePix:String
                            ) {
}