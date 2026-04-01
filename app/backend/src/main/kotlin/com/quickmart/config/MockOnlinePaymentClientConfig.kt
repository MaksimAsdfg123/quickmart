package com.quickmart.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.quickmart.client.payment.HttpMockOnlinePaymentGateway
import com.quickmart.client.payment.LocalMockOnlinePaymentGateway
import com.quickmart.client.payment.MockOnlinePaymentGateway
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(MockOnlinePaymentProperties::class)
class MockOnlinePaymentClientConfig {
    @Bean("mockOnlinePaymentRestClient")
    @ConditionalOnProperty(prefix = "app.integrations.mock-online-payment", name = ["enabled"], havingValue = "true")
    fun mockOnlinePaymentRestClient(
        restClientBuilder: RestClient.Builder,
        properties: MockOnlinePaymentProperties,
    ): RestClient {
        val requestFactory =
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(properties.connectTimeout.toMillis().toInt())
                setReadTimeout(properties.readTimeout.toMillis().toInt())
            }

        return restClientBuilder
            .baseUrl(properties.baseUrl)
            .requestFactory(requestFactory)
            .build()
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.integrations.mock-online-payment", name = ["enabled"], havingValue = "true")
    fun httpMockOnlinePaymentGateway(
        @Qualifier("mockOnlinePaymentRestClient")
        restClient: RestClient,
        properties: MockOnlinePaymentProperties,
        objectMapper: ObjectMapper,
    ): MockOnlinePaymentGateway =
        HttpMockOnlinePaymentGateway(
            restClient = restClient,
            properties = properties,
            objectMapper = objectMapper,
        )

    @Bean
    @ConditionalOnMissingBean(MockOnlinePaymentGateway::class)
    fun localMockOnlinePaymentGateway(): MockOnlinePaymentGateway = LocalMockOnlinePaymentGateway()
}
