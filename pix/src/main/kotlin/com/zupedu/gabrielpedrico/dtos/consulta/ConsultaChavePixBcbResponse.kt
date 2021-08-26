package com.zupedu.gabrielpedrico.dtos.consulta


import java.time.LocalDateTime


data class ConsultaChavePixBcbResponse(
    val keyType: String,
    val key: String,
    var bankAccount: BankAccount,
    var owner: Owner,
    var createdAt: LocalDateTime
)

data class BankAccount(
    var participant: String,
    var branch: String,
    var accountNumber: String,
    var accountType: String
)

data class Owner(
    var type: String,
    var name: String,
    var taxIdNumber: String
)
