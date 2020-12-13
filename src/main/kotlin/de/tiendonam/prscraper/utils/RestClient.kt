package de.tiendonam.prscraper.utils

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Instant

@Service
class RestClient (
        @Value("\${scraping.auth}")
        private val authorization: String
) {

    private val logger = LoggerFactory.getLogger(RestClient::class.java)
    private val rest = RestTemplate()
    private val headers = HttpHeaders().apply {
        add("Content-Type", "application/json")
        add("Accept", "*/*")
        add("Authorization", authorization)
    }

    fun get(url: String): String? {
        val requestEntity = HttpEntity("", headers)
        val responseEntity = rest.exchange(url, HttpMethod.GET, requestEntity, String::class.java)
        val remaining = responseEntity.headers["X-RateLimit-Remaining"]?.firstOrNull()
        val resetAt = responseEntity.headers["X-RateLimit-Reset"]?.firstOrNull()?.toLongOrNull()
        if (remaining != null) {
            logger.info("X-RateLimit-Remaining: $remaining")
        }
        if (resetAt != null) {
            val remainingTime = resetAt - Instant.now().epochSecond
            val min = remainingTime / 60
            val sec = remainingTime % 60
            val minString = min.toString().padStart(2, '0')
            val secString = sec.toString().padStart(2, '0')
            logger.info("X-RateLimit-Reset: in $minString:$secString")
        }
        return responseEntity.body
    }

    fun post(url: String, json: String = ""): String? {
        val requestEntity = HttpEntity(json, headers)
        val responseEntity = rest.exchange(url, HttpMethod.POST, requestEntity, String::class.java)
        return responseEntity.body
    }

    fun put(url: String, json: String = ""): HttpStatus {
        val requestEntity = HttpEntity(json, headers)
        val responseEntity = rest.exchange(url, HttpMethod.PUT, requestEntity, String::class.java)
        return responseEntity.statusCode
    }

    fun delete(url: String): HttpStatus {
        val requestEntity = HttpEntity("", headers)
        val responseEntity = rest.exchange(url, HttpMethod.DELETE, requestEntity, String::class.java)
        return responseEntity.statusCode
    }
}