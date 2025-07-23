/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.client

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
import org.web3j.utils.Numeric
import java.io.IOException
import java.math.BigInteger

class PaymasterData: Response<PaymasterData.Result>() {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = ResponseDeserialiser::class)
    override fun setResult(result: Result) {
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

    class Sponsor @JsonCreator constructor(
        @JsonProperty(value = "name")
        val name: String?,
        @JsonProperty(value = "icon")
        val icon: String?
    )

    class Result @JsonCreator constructor(
        @JsonProperty(value = "paymaster")
        val paymaster: String?,
        @JsonProperty(value = "paymasterData")
        val paymasterData: String?,
        @JsonProperty(value = "paymasterAndData")
        val paymasterAndData: String?,
        @JsonProperty(value = "paymasterVerificationGasLimit")
        val paymasterVerificationGasLimitStr: String?,
        @JsonProperty(value = "paymasterPostOpGasLimit")
        val paymasterPostOpGasLimitStr: String?,
        @JsonProperty(value = "sponsor")
        val sponsor: Sponsor?,
        @JsonProperty(value = "error")
        val error: ErrorObject?
    ) {
        val paymasterVerificationGasLimit: BigInteger?
            get() = paymasterVerificationGasLimitStr?.let { Numeric.decodeQuantity(it) }

        val paymasterPostOpGasLimit: BigInteger?
            get() = paymasterPostOpGasLimitStr?.let { Numeric.decodeQuantity(it) }
    }

    class ResponseDeserialiser : JsonDeserializer<Result>() {
        private val objectReader = ObjectMapperFactory.getObjectReader()

        @Throws(IOException::class)
        override fun deserialize(
            jsonParser: JsonParser,
            deserializationContext: DeserializationContext
        ): Result? {
            return if (jsonParser.currentToken != JsonToken.VALUE_NULL) {
                objectReader.readValue(jsonParser, Result::class.java)
            } else {
                null // null is wrapped by Optional in above getter
            }
        }
    }
}
