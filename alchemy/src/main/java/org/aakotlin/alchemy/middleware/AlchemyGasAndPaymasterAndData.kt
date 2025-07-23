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
import org.web3j.utils.Numeric
import java.io.IOException
import java.math.BigInteger

class AlchemyGasAndPaymasterAndData: Response<AlchemyGasAndPaymasterAndData.GasAndPaymasterAndData>() {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = ResponseDeserialiser::class)
    override fun setResult(result: GasAndPaymasterAndData) {
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

    class GasAndPaymasterAndData @JsonCreator constructor(
        @JsonProperty(value = "paymaster")
        val paymaster: String?,
        @JsonProperty(value = "paymasterAndData")
        val paymasterAndData: String?,
        @JsonProperty(value = "paymasterData")
        val paymasterData: String?,
        @JsonProperty(value = "callGasLimit")
        val callGasLimitStr: String,
        @JsonProperty(value = "verificationGasLimit")
        val verificationGasLimitStr: String,
        @JsonProperty(value = "preVerificationGas")
        val preVerificationGasStr: String,
        @JsonProperty(value = "maxFeePerGas")
        val maxFeePerGasStr: String,
        @JsonProperty(value = "maxPriorityFeePerGas")
        val maxPriorityFeePerGasStr: String,
        @JsonProperty(value = "paymasterVerificationGasLimit")
        val paymasterVerificationGasLimitStr: String?,
        @JsonProperty(value = "paymasterPostOpGasLimit")
        val paymasterPostOpGasLimitStr: String?,
        @JsonProperty(value = "error")
        val error: ErrorObject?
    ) {
        val callGasLimit: BigInteger
            get() = Numeric.decodeQuantity(callGasLimitStr)

        val verificationGasLimit: BigInteger
            get() = Numeric.decodeQuantity(verificationGasLimitStr)

        val preVerificationGas: BigInteger
            get() = Numeric.decodeQuantity(preVerificationGasStr)

        val maxFeePerGas: BigInteger
            get() = Numeric.decodeQuantity(maxFeePerGasStr)

        val maxPriorityFeePerGas: BigInteger
            get() = Numeric.decodeQuantity(maxPriorityFeePerGasStr)

        val paymasterVerificationGasLimit: BigInteger
            get() = Numeric.decodeQuantity(paymasterVerificationGasLimitStr)

        val paymasterPostOpGasLimit: BigInteger
            get() = Numeric.decodeQuantity(paymasterPostOpGasLimitStr)
    }

    class ResponseDeserialiser : JsonDeserializer<GasAndPaymasterAndData>() {
        private val objectReader = ObjectMapperFactory.getObjectReader()

        @Throws(IOException::class)
        override fun deserialize(
            jsonParser: JsonParser,
            deserializationContext: DeserializationContext
        ): GasAndPaymasterAndData? {
            return if (jsonParser.currentToken != JsonToken.VALUE_NULL) {
                objectReader.readValue(jsonParser, GasAndPaymasterAndData::class.java)
            } else {
                null // null is wrapped by Optional in above getter
            }
        }
    }
}
