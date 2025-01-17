package com.zupedu.gabrielpedrico.repositories

import com.zupedu.gabrielpedrico.models.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {
     fun existsByChave(chave: String?): Boolean
    fun existsByIdAndClienteId(fromString: UUID?, clientId: UUID): Boolean
    fun existsByClienteIdAndId(fromString: UUID?, fromString1: UUID?): Boolean
     fun findAllByClienteId(clientId: UUID): List<ChavePix>
}