package br.com.sample.controller

import br.com.sample.model.UserRequest
import br.com.sample.model.UserResponse
import mu.KLogger
import mu.KotlinLogging.logger
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/users")
class UserController(private val log: KLogger = logger { }) {

    @GetMapping
    fun retrieve() = listOf(UserResponse("User 1"), UserResponse("User 2"), UserResponse("User 3"))

    @PostMapping
    fun save(@RequestBody @Valid user: UserRequest, result: BindingResult) {
        log.info("user={}", user)
        if (result.hasErrors()) {
            log.error("errors={}", result)
        }
    }
}