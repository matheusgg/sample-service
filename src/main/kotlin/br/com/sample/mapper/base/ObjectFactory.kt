package br.com.sample.mapper.base

import org.mapstruct.TargetType
import org.springframework.stereotype.Component
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap

@Component
class ObjectFactory {

    private val props: ConcurrentHashMap<Class<*>, Constructor<*>> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    fun <T> create(@TargetType type: Class<T>): T {
        val constructor = this.props.getOrPut(type) {
            val constructor = type
                .constructors
                .first { it.parameters.isEmpty() }
            constructor.isAccessible = true
            constructor
        }
        return constructor.newInstance() as T
    }
}