package br.com.sample.config

import br.com.sample.exception.ClientErrorException
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import feign.Feign.Builder
import feign.FeignException.errorStatus
import feign.Logger.JavaLogger
import feign.httpclient.ApacheHttpClient
import org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContextBuilder
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.cloud.openfeign.support.SpringEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus.valueOf
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.apache.http.conn.ssl.TrustAllStrategy.INSTANCE as STRATEGY_INSTANCE

@Configuration
@EnableFeignClients(basePackages = ["br.com.sample.client"])
class FeignConfig {

    @Bean
    fun logger() = JavaLogger()

    @Bean
    fun javaTimeModule() = JavaTimeModule()

    @Bean
    fun feignDecoder() =
        ResponseEntityDecoder(SpringDecoder(ObjectFactory<HttpMessageConverters> { this.getHttpMessageConverters() }))

    @Bean
    fun feignEncoder() = SpringEncoder(ObjectFactory<HttpMessageConverters> { this.getHttpMessageConverters() })

    @Bean
    fun apacheHttpClient() = ApacheHttpClient(
        HttpClients.custom()
            .setSSLContext(SSLContextBuilder().loadTrustMaterial(null, STRATEGY_INSTANCE).build())
            .setSSLHostnameVerifier(INSTANCE)
            .build()
    )

    @Bean
    fun feignBuilderBeanPostProcessor() = object : BeanPostProcessor {
        override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
            return if (bean is Builder) {
                bean.errorDecoder { methodKey, response ->
                    val feignException = errorStatus(methodKey, response)
                    val status = valueOf(response.status())
                    if (status.is4xxClientError) {
                        return@errorDecoder ClientErrorException(status, feignException.message)
                    }
                    feignException
                }
            } else bean
        }
    }

    private fun getHttpMessageConverters() =
        HttpMessageConverters(MappingJackson2HttpMessageConverter(this.customObjectMapper()))

    private fun customObjectMapper() = ObjectMapper()
        .disable(WRITE_DATES_AS_TIMESTAMPS)
        .disable(FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(ACCEPT_CASE_INSENSITIVE_ENUMS)
        .setPropertyNamingStrategy(SNAKE_CASE)
        .findAndRegisterModules()
}