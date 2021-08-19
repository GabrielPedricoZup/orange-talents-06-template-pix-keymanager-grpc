package com.zupedu.gabrielpedrico.enums

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested

internal class TipoDeChaveTest {

    @Nested
    inner class ALEATORIA {
        @Test
        fun `deve ser valido quando chave aleatoria for nula ou vazia`() {
            with(TipoDeChave.ALEATORIA) {
                assertTrue(valida(null))
                assertTrue(valida(""))
            }
        }

        @Test
        fun `nao deve ser valido quando chave aleatoria possuir um valor`() {
            with(TipoDeChave.ALEATORIA) {
                assertFalse(valida("invalido"))
            }
        }
    }

    @Nested
    inner class CPF {
        @Test
        fun `deve ser valido com cpf valido`() {
            with(TipoDeChave.CPF) {
                assertTrue(valida("41531952860"))
            }
        }

        @Test
        fun `nao deve ser valido com cpf invalido`() {
            with(TipoDeChave.CPF) {
                assertFalse(valida("invalido"))
            }
        }

        @Test
        fun `nao deve ser valido com cpf nao informado`() {
            with(TipoDeChave.CPF) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }

    @Nested
    inner class CELULAR {
        @Test
        fun `deve ser valido com celular valido`() {
            with(TipoDeChave.CELULAR) {
                assertTrue(valida("+5513991679670"))
            }
        }

        @Test
        fun `nao deve ser valido com cpf invalido`() {
            with(TipoDeChave.CELULAR) {
                assertFalse(valida("13991679670"))
                assertFalse(valida("+556969420a77712345"))
            }
        }

        @Test
        fun `nao deve ser valido com cpf nao informado`() {
            with(TipoDeChave.CELULAR) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }

    @Nested
    inner class EMAIL {
        @Test
        fun `deve ser valido com email valido`() {
            with(TipoDeChave.EMAIL) {
                assertTrue(valida("email.valido@gmail.com"))
            }
        }

        @Test
        fun `nao deve ser valido com email invalido`() {
            with(TipoDeChave.EMAIL) {
                assertFalse(valida("email.invalido.com"))
                assertFalse(valida("email.invalido.com."))
            }
        }

        @Test
        fun `nao deve ser valido com email nao informado`() {
            with(TipoDeChave.CELULAR) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }
}