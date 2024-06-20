package com.example.provider.http.controller.api.response

import java.util.*

data class CreatePersonResponse(val id: UUID, val firstName: String, val lastName: String, val email: String)