package com.example.consumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class ConsumerApplication

fun main(args: Array<String>) {
	runApplication<ConsumerApplication>(*args)
}
