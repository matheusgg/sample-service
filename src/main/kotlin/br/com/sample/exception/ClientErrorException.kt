package br.com.sample.exception

import org.springframework.http.HttpStatus

data class ClientErrorException(val status: HttpStatus, override val message: String?) : RuntimeException()