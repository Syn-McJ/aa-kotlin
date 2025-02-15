/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.provider

import org.aakotlin.core.Chain

data class ProviderConfig(
    val chain: Chain,
    val connectionConfig: ConnectionConfig,
    val opts: SmartAccountProviderOpts? = null,
)