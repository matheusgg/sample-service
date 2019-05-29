package br.com.sample.model

import javax.validation.constraints.NotNull

class UserRequest {
    @NotNull
    lateinit var name: String

    @NotNull
    var age: Int? = 0
}