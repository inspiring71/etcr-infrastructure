package de.tiendonam.prscraper.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val mapper = jacksonObjectMapper().apply {

    // ignore unknown json properties
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}

/**
 * Extension function to convert anything to json
 * usage: val s = a.toJSON(), where a is any object
 *
 * @return serialized json as string
 */
fun Any.toJSON(): String {
    return mapper.writeValueAsString(this)
}

/**
 * Extension function to parse json
 * usage: val obj = s.parseJSON(), where s is a string
 *
 * @return parsed object
 */
inline fun <reified T> String.parseJSON(): T {
    return mapper.readValue(this)
}