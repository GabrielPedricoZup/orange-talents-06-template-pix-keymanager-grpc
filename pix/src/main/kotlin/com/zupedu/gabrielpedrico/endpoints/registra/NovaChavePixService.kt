package com.zupedu.gabrielpedrico.endpoints.registra

import com.zupedu.gabrielpedrico.dtos.registra.BankAccount
import com.zupedu.gabrielpedrico.dtos.registra.BcbChavePixRequest
import com.zupedu.gabrielpedrico.dtos.registra.NovaChavePix
import com.zupedu.gabrielpedrico.dtos.registra.Owner
import com.zupedu.gabrielpedrico.handlers.ChavePixExistenteException
import com.zupedu.gabrielpedrico.integrations.BcbClient
import com.zupedu.gabrielpedrico.integrations.ContasDeClientesNoItauClient
import com.zupedu.gabrielpedrico.models.ChavePix
import com.zupedu.gabrielpedrico.repositories.ChavePixRepository
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(@Inject val repository: ChavePixRepository,
                          @Inject val itauClient: ContasDeClientesNoItauClient,
                          @Inject val bcbClient: BcbClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChave: NovaChavePix): ChavePix {
        //1. verifica se já existe essa chave no sistema
        if(repository.existsByChave(novaChave.chave))
            throw ChavePixExistenteException("Chave Pix '${novaChave.chave}' existente")
        //2. busca dados da conta no ERP do ITAU
        val response = itauClient.buscaContaPorTipo(novaChave.clienteId, novaChave.tipoDeConta.name)
        val conta = response.body()?.paraContaAssociada() ?: throw IllegalStateException("Cliente não encontrado no Itau")
        //3. grava dados da conta no ERP do BCB
        var tipoDeChave: String? = null
        if(novaChave.tipoDeConta.name == "CONTA_CORRENTE")  tipoDeChave = "CACC"
        if(novaChave.tipoDeConta.name == "CONTA_POUPANCA")  tipoDeChave = "SVGS"
        var bankAccount = BankAccount("60701190",conta.agencia.toString(),conta.numeroDaConta.toString(),tipoDeChave.toString())
        var owner: Owner = Owner("NATURAL_PERSON",conta.nomeDoTitular.toString(),conta.cpfDoTitular.toString())
        val request = BcbChavePixRequest(novaChave.tipo,novaChave.chave,bankAccount,owner)
        bcbClient.registraConta(request)
        //4. grava no banco de dados
        val chave = novaChave.paraChavePix(conta)
        repository.save(chave)

        return chave
    }
}