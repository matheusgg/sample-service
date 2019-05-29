package br.com.sample.model

import br.com.sample.mapper.base.NoArgConstructor

@NoArgConstructor
data class ProductResponse(var id: String, var name: String)