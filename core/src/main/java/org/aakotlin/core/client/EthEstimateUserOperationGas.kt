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

class EthEstimateUserOperationGas: Response<EthEstimateUserOperationGas.EstimateUserOperationGas>() {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = ResponseDeserialiser::class)
    override fun setResult(result: EstimateUserOperationGas) {
        super.setResult(result)
    }

    class EstimateUserOperationGas @JsonCreator constructor(
        /* Gas overhead of this UserOperation */
        @JsonProperty(value = "preVerificationGas")
        val preVerificationGasStr: String,
        /* Actual gas used by the validation of this UserOperation */
        @JsonProperty(value = "verificationGasLimit")
        val verificationGasLimitStr: String,
        /* Value used by inner account execution */
        @JsonProperty(value = "callGasLimit")
        val callGasLimitStr: String,

        /*
         * EntryPoint v0.7.0 operations only.
         * The amount of gas to allocate for the paymaster validation code.
         * Note: `eth_estimateUserOperationGas` does not return paymasterPostOpGasLimit.
         */
        @JsonProperty(value = "paymasterVerificationGasLimit")
        val paymasterVerificationGasLimitStr: String
    ) {
        val preVerificationGas: BigInteger
            get() = Numeric.decodeQuantity(preVerificationGasStr)

        val verificationGasLimit: BigInteger
            get() = Numeric.decodeQuantity(verificationGasLimitStr)

        val callGasLimit: BigInteger
            get() = Numeric.decodeQuantity(callGasLimitStr)

        val paymasterVerificationGasLimit: BigInteger
            get() = Numeric.decodeQuantity(paymasterVerificationGasLimitStr)
    }

    class ResponseDeserialiser : JsonDeserializer<EstimateUserOperationGas>() {
        private val objectReader = ObjectMapperFactory.getObjectReader()

        @Throws(IOException::class)
        override fun deserialize(
            jsonParser: JsonParser,
            deserializationContext: DeserializationContext
        ): EstimateUserOperationGas? {
            return if (jsonParser.currentToken != JsonToken.VALUE_NULL) {
                objectReader.readValue(jsonParser, EstimateUserOperationGas::class.java)
            } else {
                null // null is wrapped by Optional in above getter
            }
        }
    }
}