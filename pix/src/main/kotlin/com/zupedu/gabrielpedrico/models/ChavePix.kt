package com.zupedu.gabrielpedrico.models

import com.zupedu.gabrielpedrico.TipoDeConta
import com.zupedu.gabrielpedrico.dtos.registra.ContaAssociada
import com.zupedu.gabrielpedrico.enums.TipoDeChave
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(uniqueConstraints = [UniqueConstraint(
    name = "uk_chave_pix",
    columnNames = ["chave"]
)])
data class ChavePix(
    @field:NotNull
    @Column(nullable = false)
    var clienteId: UUID?,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var tipo: TipoDeChave?,

    @field:NotBlank
    @Column(unique = true,nullable = false)
    var chave: String?,


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var tipoDeConta: TipoDeConta?,


    var conta: ContaAssociada?
) {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: UUID? = null

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "ChavePix(clienteId=$clienteId, tipo=$tipo, chave='$chave', tipoDeConta=$tipoDeConta, conta=$conta, id=$id, criadaEm=$criadaEm)"
    }


}
