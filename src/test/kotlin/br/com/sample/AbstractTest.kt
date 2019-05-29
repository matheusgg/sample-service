package br.com.sample

import capital.scalable.restdocs.AutoDocumentation.*
import capital.scalable.restdocs.SnippetRegistry.*
import capital.scalable.restdocs.jackson.JacksonResultHandlers.prepareJackson
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors.limitJsonArrayLength
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors.replaceBinaryContent
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.cli.CliDocumentation.curlRequest
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.http.HttpDocumentation.httpRequest
import org.springframework.restdocs.http.HttpDocumentation.httpResponse
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup
import org.springframework.web.context.WebApplicationContext
import java.net.URI
import javax.servlet.Filter

@AutoConfigureMockMvc
@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class AbstractTest {

    protected lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var filters: Array<Filter>

    @Autowired
    protected lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var context: WebApplicationContext

    @Value("\${info.documentationHost}")
    private lateinit var documentationHost: URI

    @get:Rule
    val restDocumentation = JUnitRestDocumentation()

    @Before
    fun setUp() {
        this.mockMvc = webAppContextSetup(this.context)
            .addFilters<DefaultMockMvcBuilder>(*this.filters)
            .alwaysDo<DefaultMockMvcBuilder>(prepareJackson(this.mapper))
            .apply<DefaultMockMvcBuilder>(
                documentationConfiguration(this.restDocumentation)
                    .uris()
                    .withScheme(this.getDocumentationScheme())
                    .withHost(this.getDocumentationHost())
                    .withPort(this.getDocumentationPort()!!)
                    .and()
                    .snippets()
                    .withDefaults(
                        curlRequest(),
                        httpRequest(),
                        httpResponse(),
                        requestFields(),
                        requestHeaders(),
                        responseFields(),
                        pathParameters(),
                        requestParameters(),
                        description(),
                        methodAndPath(),
                        responseHeaders(),
                        this.getSectionBuilder()
                    )
            )
            .build()

        PayloadDocumentation.responseFields()
    }

    protected fun document() =
        document("{class-name}/{method-name}", preprocessRequest(), this.getOperationResponsePreprocessor())!!

    private fun getSectionBuilder() = sectionBuilder()
        .snippetNames(
            AUTO_METHOD_PATH,
            AUTO_DESCRIPTION,
            AUTO_PATH_PARAMETERS,
            AUTO_REQUEST_PARAMETERS,
            AUTO_REQUEST_FIELDS,
            AUTO_REQUEST_HEADERS,
            CURL_REQUEST,
            HTTP_REQUEST,
            HTTP_RESPONSE,
            AUTO_RESPONSE_FIELDS
        )
        .skipEmpty(true)
        .build()

    private fun getOperationResponsePreprocessor() =
        preprocessResponse(replaceBinaryContent(), limitJsonArrayLength(this.mapper), prettyPrint())


    private fun getDocumentationScheme() = this.documentationHost.scheme


    private fun getDocumentationHost() = this.documentationHost.host

    private fun getDocumentationPort(): Int? {
        val port = this.documentationHost.port
        return if (port > 0) port else 80
    }
}