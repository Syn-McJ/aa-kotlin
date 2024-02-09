/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.aakotlin.core.UserOperationReceipt
import org.web3j.protocol.ObjectMapperFactory
import org.web3j.protocol.core.Response
import java.io.IOException

class EthGetUserOperationReceipt: Response<UserOperationReceipt>() {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = ResponseDeserializer::class)
    override fun setResult(result: UserOperationReceipt) {
        super.setResult(result)
    }

    class ResponseDeserializer : JsonDeserializer<UserOperationReceipt>() {
        private val objectReader = ObjectMapperFactory.getObjectReader()

        @Throws(IOException::class)
        override fun deserialize(
            jsonParser: JsonParser,
            deserializationContext: DeserializationContext
        ): UserOperationReceipt? {
            return if (jsonParser.currentToken != JsonToken.VALUE_NULL) {
                objectReader.readValue(jsonParser, UserOperationReceipt::class.java)
            } else {
                null // null is wrapped by Optional in above getter
            }
        }
    }
}