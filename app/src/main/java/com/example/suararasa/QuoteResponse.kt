package com.example.suararasa

data class QuoteResponse(
    val quote: QuoteDetail
)

data class QuoteDetail(
    val body: String,
    val author: String
)
