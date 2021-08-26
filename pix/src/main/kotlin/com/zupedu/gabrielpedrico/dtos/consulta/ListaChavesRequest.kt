package com.zupedu.gabrielpedrico.dtos.consulta

import javax.validation.constraints.NotBlank

data class ListaChavesRequest(@field:NotBlank val clientId:String){}
