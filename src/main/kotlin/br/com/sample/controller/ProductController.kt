package br.com.sample.controller

import br.com.sample.mapper.ProductMapper
import br.com.sample.model.ProductRequest
import br.com.sample.model.ProductResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID.randomUUID

@RestController
@RequestMapping("/products")
class ProductController(private val mapper: ProductMapper) {

    @PostMapping
    fun save(@RequestBody request: ProductRequest): ProductResponse {
        val response = this.mapper.toResponse(request)
        response.id = randomUUID().toString()
        return response
    }
}