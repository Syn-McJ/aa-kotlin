/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.web3j.protocol.ObjectMapperFactory
import org.web3j.protocol.core.Response
import java.io.IOException

class AlchemyPaymasterAndData: Response<AlchemyPaymasterAndData.PaymasterAndData>() {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = ResponseDeserialiser::class)
    override fun setResult(result: PaymasterAndData) {
        super.setResult(result)
    }

    class ErrorObject @JsonCreator constructor(
        @JsonProperty(value = "code")
        val code: Int,
        @JsonProperty(value = "message")
        val message: String,
        @JsonProperty(value = "data")
        val data: Any?
    )

    class PaymasterAndData @JsonCreator constructor(
        @JsonProperty(value = "paymasterAndData")
        val paymasterAndData: String,
        @JsonProperty(value = "error")
        val error: ErrorObject?
    )

    class ResponseDeserialiser : JsonDeserializer<PaymasterAndData>() {
        private val objectReader = ObjectMapperFactory.getObjectReader()

        @Throws(IOException::class)
        override fun deserialize(
            jsonParser: JsonParser,
            deserializationContext: DeserializationContext
        ): PaymasterAndData? {
            return if (jsonParser.currentToken != JsonToken.VALUE_NULL) {
                objectReader.readValue(jsonParser, PaymasterAndData::class.java)
            } else {
                null // null is wrapped by Optional in above getter
            }
        }
    }
}
