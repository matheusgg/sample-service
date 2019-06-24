package br.com.sample.client

import br.com.sample.model.NotificationRequest
import br.com.sample.model.NotificationResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient(name = "NotificationClient", url = "\${sample-service.integration.notification-client.url}")
interface NotificationClient {

    @PutMapping("/notification")
    fun create(@RequestBody request: NotificationRequest): NotificationResponse

    @PostMapping("/notification/{uuid}/send", consumes = [APPLICATION_JSON_VALUE])
    fun send(@PathVariable uuid: String): ResponseEntity<String>
}