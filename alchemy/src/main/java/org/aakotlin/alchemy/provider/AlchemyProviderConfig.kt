/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.provider

import org.aakotlin.alchemy.ConnectionConfig
import org.aakotlin.core.Chain
import org.aakotlin.core.provider.SmartAccountProviderOpts

data class AlchemyProviderConfig(
    val chain: Chain,
    val connectionConfig: ConnectionConfig,
    val opts: SmartAccountProviderOpts? = null,
)