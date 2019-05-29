package br.com.sample.mapper

import br.com.sample.mapper.base.ObjectFactory
import br.com.sample.model.ProductRequest
import br.com.sample.model.ProductResponse
import org.mapstruct.Mapper

@Mapper(uses = [ObjectFactory::class])
interface ProductMapper {

    fun toResponse(request: ProductRequest): ProductResponse
}