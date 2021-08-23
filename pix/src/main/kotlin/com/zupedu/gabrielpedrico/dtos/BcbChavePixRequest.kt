package com.zupedu.gabrielpedrico.dtos

import com.zupedu.gabrielpedrico.TipoDeConta
import com.zupedu.gabrielpedrico.enums.TipoDeChave

data class BcbChavePixRequest(var keyType: TipoDeChave?,
                              val key: String?,
                              var bankAccount: BankAccount?,
                              var owner: Owner
){}

data class BankAccount(var participant:String,
                        var branch: String,
                        var accountNumber: String,
                       var accountType: String
                        ){}

data class Owner(var type:String,
                       var name: String,
                       var taxIdNumber: String
){}