package org.aakotlin.coinbase.middleware

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

class SponsoredUserOperation: Response<SponsoredUserOperation.Result>() {
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

    class Result @JsonCreator constructor(
        @JsonProperty(value = "paymasterAndData")
        val paymasterAndData: String,
        @JsonProperty(value = "preVerificationGas")
        val preVerificationGasStr: String,
        @JsonProperty(value = "verificationGasLimit")
        val verificationGasLimitStr: String,
        @JsonProperty(value = "callGasLimit")
        val callGasLimitStr: String,
        @JsonProperty(value = "paymasterVerificationGasLimit")
        val paymasterVerificationGasLimit: String,
        @JsonProperty(value = "paymasterPostOpGasLimit")
        val paymasterPostOpGasLimit: String,
        @JsonProperty(value = "error")
        val error: ErrorObject?
    ) {
        val callGasLimit: BigInteger
            get() = Numeric.decodeQuantity(callGasLimitStr)

        val verificationGasLimit: BigInteger
            get() = Numeric.decodeQuantity(verificationGasLimitStr)

        val preVerificationGas: BigInteger
            get() = Numeric.decodeQuantity(preVerificationGasStr)
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
