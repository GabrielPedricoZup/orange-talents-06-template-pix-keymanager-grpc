package com.zupedu.gabrielpedrico.dtos.registra


import com.zupedu.gabrielpedrico.TipoDeConta
import com.zupedu.gabrielpedrico.dtos.registra.ContaAssociada
import com.zupedu.gabrielpedrico.models.ChavePix
import com.zupedu.gabrielpedrico.validations.ValidPixKey
import com.zupedu.gabrielpedrico.validations.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import com.zupedu.gabrielpedrico.enums.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(

    @field:ValidUUID
    @field:NotBlank
    val clienteId: String,
    @field:NotNull
    val tipo: TipoDeChave?,
    @field:Size(max = 77)
    val chave: String,
    @field:NotNull
    val tipoDeConta: TipoDeConta
) {

        fun paraChavePix(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipo = TipoDeChave.valueOf(this.tipo!!.name),
            chave = if (this.tipo == TipoDeChave.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            tipoDeConta = TipoDeConta.valueOf(this.tipoDeConta!!.name),
            conta = conta
        )
    }
}


