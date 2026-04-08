package com.adrian.diptrack.data.di

import com.adrian.diptrack.data.remote.source.SocketManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClient {
            install(Logging) {
                level = LogLevel.ALL
                logger = Logger.DEFAULT
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
            defaultRequest {
                url("http://145.79.15.182:5000/api/")
            }
        }
    }
    singleOf(::SocketManager)
}
