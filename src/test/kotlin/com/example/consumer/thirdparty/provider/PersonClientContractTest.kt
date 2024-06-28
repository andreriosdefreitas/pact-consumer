package com.example.consumer.thirdparty.provider

import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTest
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.V4Pact
import au.com.dius.pact.core.model.annotations.Pact
import com.example.consumer.ConsumerApplication
import com.example.provider.http.controller.api.request.CreatePersonRequest
import com.example.provider.http.controller.api.response.CreatePersonResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*
import kotlin.test.assertEquals

@ExtendWith(SpringExtension::class)
@PactTestFor(providerName = "provider-api", port = "8888")
@SpringBootTest(classes = [ConsumerApplication::class], properties = ["client.provider.url: localhost:8888"])
@PactConsumerTest
class PersonClientContractTest(@Autowired private val objectMapper: ObjectMapper,
                               @Autowired private val providerClient: ProviderClient) {

    private val personId = "23bb5352-303a-485e-a75b-6b1a97727cde"
    private val firstName = "FirstA"
    private val lastName = "Last"
    private val email = "Email"
    private val personRequest = CreatePersonRequest(firstName, lastName, email)
    private val personResponse = CreatePersonResponse(UUID.fromString(personId), firstName, lastName, email)

    @Pact(provider = "provider-api", consumer = "consumer-client")
    fun createPerson(builder: PactDslWithProvider): V4Pact =
        builder
            .given("create a person")
            .uponReceiving("a request to create a person")
            .path("/person")
            .method(HttpMethod.POST.name())
            .headers(mapOf(Pair(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)))
            .body(objectMapper.writeValueAsString(personRequest))
            .willRespondWith()
            .headers((mapOf(Pair(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))))
            .status(HttpStatus.CREATED.value())
            .body(objectMapper.writeValueAsString(personResponse))
            .toPact(V4Pact::class.java)

    @Test
    @PactTestFor(pactMethod = "createPerson")
    fun verifyCreatePerson() {
        val response = providerClient.createPerson(personRequest)
        assertEquals(personId, response.id.toString())
    }

}