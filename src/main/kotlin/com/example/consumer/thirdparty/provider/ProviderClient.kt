package com.example.consumer.thirdparty.provider

import com.example.provider.http.controller.api.request.CreatePersonRequest
import com.example.provider.http.controller.api.response.CreatePersonResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "provider-api",
    url = "\${client.provider.url}"
)
interface ProviderClient {
    @PostMapping("/person", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createPerson(@RequestBody createPersonRequest: CreatePersonRequest): CreatePersonResponse
}