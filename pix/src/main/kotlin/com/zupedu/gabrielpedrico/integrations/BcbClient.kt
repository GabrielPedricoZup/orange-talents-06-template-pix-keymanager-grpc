package com.zupedu.gabrielpedrico.integrations

import com.zupedu.gabrielpedrico.dtos.BcbChavePixRequest
import com.zupedu.gabrielpedrico.dtos.BcbDeletaChavePixRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:8082")
interface BcbClient {

    @Post("/api/v1/pix/keys")
    @Produces(MediaType.APPLICATION_XML)
    fun registraConta(@Body clienteId: BcbChavePixRequest): HttpResponse<Any?>

    @Delete("/api/v1/pix/keys/{key}")
    @Produces(MediaType.APPLICATION_XML)
    fun deletaConta(@PathVariable key:String?,@Body request:BcbDeletaChavePixRequest): HttpResponse<Any?>

}