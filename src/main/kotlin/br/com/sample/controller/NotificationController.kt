package br.com.sample.controller

import br.com.sample.client.NotificationClient
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
    fun send(@RequestBody request: NotificationRequest) {
        request.push?.users!!.chunked(100)
            .parallelStream()
            .forEach {
                runCatching {
                    val req = request.copy(push = Push(it))
                    val response = this.client.create(req)
                    this.client.send(response.uuid)
                }.onSuccess {
                    log.info("==================> Success")
                }.onFailure {
                    log.error("==================> Error {}", it)
                }
            }
    }
}