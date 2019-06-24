package br.com.sample.controller

import br.com.sample.client.NotificationClient
import br.com.sample.model.NotificationProcessResponse
import br.com.sample.model.NotificationRequest
import br.com.sample.model.Push
import mu.KLogger
import mu.KotlinLogging.logger
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notifications")
class NotificationController(private val log: KLogger = logger { }, private val client: NotificationClient) {

    @PostMapping
    fun send(@RequestBody request: NotificationRequest): NotificationProcessResponse {
        val usersWithError = mutableListOf<Long>()
        request.push?.users!!.chunked(100)
            .parallelStream()
            .forEach { users ->
                runCatching {
                    val req = request.copy(push = Push(users))
                    val response = this.client.create(req)
                    this.client.send(response.uuid)
                }.onFailure {
                    usersWithError.addAll(users)
                }
            }
        return NotificationProcessResponse(usersWithError)
    }
}