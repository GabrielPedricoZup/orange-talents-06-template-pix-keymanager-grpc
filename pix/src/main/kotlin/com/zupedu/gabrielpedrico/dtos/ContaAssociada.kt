package com.zupedu.gabrielpedrico.dtos

import javax.persistence.Embeddable

@Embeddable
data class ContaAssociada(var instituicao: String?,
                          var nomeDoTitular: String?,
                          var cpfDoTitular: String?,
                          var agencia: String?,
                          var numeroDaConta: String?)
