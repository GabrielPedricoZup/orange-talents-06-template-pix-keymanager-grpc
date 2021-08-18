package com.zupedu.gabrielpedrico.endpoints.registra

import com.zupedu.gabrielpedrico.integrations.ContasDeClientesNoItauClient
import com.zupedu.gabrielpedrico.models.ChavePix
import com.zupedu.gabrielpedrico.dtos.NovaChavePix
import com.zupedu.gabrielpedrico.handlers.ChavePixExistenteException
import com.zupedu.gabrielpedrico.repositories.ChavePixRepository
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import javax.transaction.Transactional
import javax.validation.Valid

/*@Validated*/
@Singleton
class NovaChavePixService(@Inject val repository: ChavePixRepository,
                          @Inject val itauClient: ContasDeClientesNoItauClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChave: NovaChavePix): ChavePix {
        //1. verifica se já existe essa chave no sistema
        if(repository.existsByChave(novaChave.chave))
            throw ChavePixExistenteException("Chave Pix '${novaChave.chave}' existente")
        //2. busca dados da conta no ERP do ITAU
        val response = itauClient.buscaContaPorTipo(novaChave.clienteId!!, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.paraContaAssociada() ?: throw IllegalStateException("Cliente não encontrado no Itau")
        //3. grava no banco de dados
        val chave = novaChave.paraChavePix(conta)
        repository.save(chave)

        return chave
    }
}