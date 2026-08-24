package com.github.airstream.api.obj

import kotlinx.serialization.Serializable

@Serializable
data class DeleteUserRequest(val password: String)
