package com.example.app.model

data class DiscoveryQueryResponse(
    val addresses: List<String>,
    val port: Int
)