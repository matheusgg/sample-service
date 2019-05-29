package br.com.sample.controller

import br.com.sample.AbstractTest
import org.junit.Test
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class UserControllerTest : AbstractTest() {

    @Test
    fun retrieve() {
        super.mockMvc.perform(get("/users"))
            .andExpect(status().isOk)
            .andDo(print())
            .andDo(super.document())
    }

    @Test
    fun save() {
        val user = """{
                        "name": "Teste",
                        "age": 18
                    }"""
        super.mockMvc.perform(
            post("/users")
                .contentType(APPLICATION_JSON_VALUE)
                .content(user)
        )
            .andExpect(status().isOk)
            .andDo(print())
            .andDo(super.document())
    }
}