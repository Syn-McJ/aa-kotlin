/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.auth

import com.fasterxml.jackson.annotation.JsonProperty
import org.aakotlin.core.Address
import java.math.BigInteger

/**
 * EIP-7702 Authorization tuple for account delegation
 */
data class Eip7702Auth(
    /** Chain ID for the authorization */
    val chainId: String,
    /** Nonce for the authorization */
    val nonce: String,
    /** Implementation contract address to delegate to */
    val address: String,
    /** Signature r component */
    val r: String,
    /** Signature s component */
    val s: String,
    /** Signature yParity component (0 or 1) */
    @get:JsonProperty("yParity")
    val yParity: String
)

/**
 * Authorization data structure for EIP-7702 delegation
 */
data class Authorization(
    /** Chain ID where the authorization is valid */
    val chainId: Long,
    /** Implementation contract address to delegate execution to */
    val contractAddress: String,
    /** Account nonce for replay protection */
    val nonce: BigInteger
)

/**
 * Authorization signature components
 */
data class AuthorizationSignature(
    /** Signature r component */
    val r: String,
    /** Signature s component */
    val s: String,
    /** Recovery ID / yParity (0 or 1) */
    @get:JsonProperty("yParity")
    val yParity: String
)

/**
 * Account mode enumeration for different account types
 */
enum class AccountMode {
    /** Traditional ERC-4337 smart contract account */
    DEFAULT,
    /** EIP-7702 delegated EOA account */
    EIP7702
}