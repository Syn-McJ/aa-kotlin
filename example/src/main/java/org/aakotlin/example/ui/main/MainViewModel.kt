/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.example.ui.main

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.ExtraLoginOptions
import com.web3auth.core.types.LoginParams
import com.web3auth.core.types.Provider
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import org.aakotlin.alchemy.account.ModularAccountV2
import org.aakotlin.alchemy.middleware.AlchemyGasManagerConfig
import org.aakotlin.alchemy.middleware.erc7677Middleware
import org.aakotlin.alchemy.middleware.withAlchemyGasManager
import org.aakotlin.alchemy.provider.AlchemyProvider
import org.aakotlin.core.Address
import org.aakotlin.core.Chain
import org.aakotlin.core.UserOperationCallData
import org.aakotlin.core.auth.AccountMode
import org.aakotlin.core.provider.ConnectionConfig
import org.aakotlin.core.provider.ISmartAccountProvider
import org.aakotlin.core.provider.ProviderConfig
import org.aakotlin.core.provider.SmartAccountProviderOpts
import org.aakotlin.core.signer.LocalAccountSigner
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.contracts.eip20.generated.ERC20
import org.web3j.crypto.Credentials
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger

enum class AuthMode {
    WEB3_AUTH,
    PRIVATE_KEY
}

enum class Step {
    NOT_STARTED,
    KEY,
    ADDRESS,
    READY,
    MINTING,
    CONFIRMING,
    DONE,
    ERROR
}

data class UIState(
    val step: Step = Step.NOT_STARTED,
    val address: String? = null,
    val error: String? = null,
    val balance: String? = null,
    val explorerLink: String? = null
)

class MainViewModel : ViewModel() {
    companion object {
        val chain = Chain.BaseSepolia

        private const val JIFFYSCAN_BASE_URL = "https://jiffyscan.xyz/userOpHash/"
        private const val USDC_TOKEN_ADDRESS = "0xCFf7C6dA719408113DFcb5e36182c6d5aa491443"

        // replace with your Alchemy API key
        private const val ALCHEMY_API_KEY = "VL04Y5WbMvKHO05PIKtTsmifkEaz8UYU"
        // replace with your Alchemy gas policy ID
        private const val ALCHEMY_GAS_POLICY_ID = "6e224b86-72a7-491b-84b4-b5741e337b10"

        // replace with your Web3Auth Client ID
        // these IDs are from Web3Auth example
        const val WEB3_AUTH_CLIENT_ID = "BPi5PB_UiIZ-cPz1GtV5i1I2iOSOHuimiXBI0e-Oe_u6X3oVAbCiAZOTEBtTXw4tsluTITPqA8zMsfxIKMjiqNQ"
        const val AUTH0_CLIENT_ID = "hUVVf4SEsZT7syOiL0gLU9hFEtm2gQ6O"
        
        // Private key that controls the wallet
        private const val PRIVATE_KEY = "0x394add01e3372e6a2752894d5e502810ae59609e53de4f176cee6098b18e4bc6"
    }

    // Authentication mode - default to hardcoded private key
    private val authMode: AuthMode = AuthMode.WEB3_AUTH
    
    private lateinit var web3Auth: Web3Auth
    private var alchemyToken: ERC20? = null
    private var scaProvider: ISmartAccountProvider? = null

    private val _uiState = MutableLiveData(UIState())
    val uiState: LiveData<UIState> = _uiState

    fun init(web3Auth: Web3Auth) {
        this.web3Auth = web3Auth
        
        // If using private key mode, initialize immediately
        if (authMode == AuthMode.PRIVATE_KEY) {
            setKeyStateWithPrivateKey(PRIVATE_KEY)
        }
    }

    fun login() {
        if (authMode == AuthMode.PRIVATE_KEY) {
            // Already logged in with hardcoded key, do nothing
            return
        }
        
        web3Auth.login(
            LoginParams(
                Provider.JWT,
                extraLoginOptions = ExtraLoginOptions(
                    domain = "https://web3auth.au.auth0.com",
                    verifierIdField = "sub"
                )
            )
        ).whenComplete { _, error ->
            setKeyState(true, error)
        }
    }

    fun logout() {
        if (authMode == AuthMode.PRIVATE_KEY) {
            // Cannot logout with hardcoded key
            return
        }
        
        val logoutCompletableFuture = web3Auth.logout()
        logoutCompletableFuture.whenComplete { _, error ->
            setKeyState(false, error)
        }
    }

    fun setResultUrl(data: Uri?) {
        web3Auth.setResultUrl(data)
    }

    fun checkSession() {
        if (authMode == AuthMode.PRIVATE_KEY) {
            // For private key mode, we're already initialized in init()
            return
        }
        
        web3Auth.initialize().whenComplete { _, error ->
            setKeyState(true, error)
        }
    }

    fun mint() {
        scaProvider?.let { provider ->
            _uiState.value = _uiState.value?.copy(step = Step.MINTING)
            viewModelScope.launch {
                try {
                    val resultHash = sendMintUserOperation(provider)
                    _uiState.value = _uiState.value?.copy(step = Step.CONFIRMING)
                    provider.waitForUserOperationTransaction(resultHash)
                    _uiState.value = _uiState.value?.copy(step = Step.DONE, explorerLink = JIFFYSCAN_BASE_URL + resultHash)
                    refreshAlchemyTokenBalance()
                } catch (ex: Exception) {
                    _uiState.value = _uiState.value?.copy(step = Step.ERROR, error = ex.message ?: "Error while minting")
                }
            }
        }
    }

    private fun setKeyState(loggedIn: Boolean, error: Throwable?) {
        if (error == null) {
            if (loggedIn && web3Auth.getPrivkey().isNotEmpty()) {
                setupSmartContractAccount(Credentials.create(web3Auth.getPrivkey()))
                viewModelScope.launch {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            step = Step.READY,
                            address = scaProvider?.getAddress()
                        )
                    )
                }
            } else {
                _uiState.postValue(_uiState.value?.copy(step = Step.NOT_STARTED))
            }
        } else {
            _uiState.postValue(_uiState.value?.copy(step = Step.ERROR, error = error.message ?: "Error while fetching key"))
        }
    }
    
    private fun setKeyStateWithPrivateKey(privateKey: String) {
        viewModelScope.launch {
            try {
                val cleanPrivateKey = if (privateKey.startsWith("0x")) {
                    privateKey.substring(2)
                } else {
                    privateKey
                }
                val credentials = Credentials.create(cleanPrivateKey)
                setupSmartContractAccount(credentials)
                _uiState.postValue(
                    _uiState.value?.copy(
                        step = Step.READY,
                        address = scaProvider?.getAddress()
                    )
                )
            } catch (ex: Exception) {
                _uiState.postValue(
                    _uiState.value?.copy(
                        step = Step.ERROR,
                        error = "Failed to import private key: ${ex.message}"
                    )
                )
            }
        }
    }

    private fun setupSmartContractAccount(credentials: Credentials) {
        val connectionConfig = ConnectionConfig(ALCHEMY_API_KEY, null, null)
        val provider = AlchemyProvider(
            ProviderConfig(
                chain,
                connectionConfig,
                SmartAccountProviderOpts(50, 500)
            )
        ).withAlchemyGasManager(
            AlchemyGasManagerConfig(ALCHEMY_GAS_POLICY_ID, connectionConfig)
        )
        .erc7677Middleware(policyId = ALCHEMY_GAS_POLICY_ID)

        val signer = LocalAccountSigner()
        val account = ModularAccountV2(
            provider.rpcClient,
            null,
            signer,
            chain,
            mode = AccountMode.EIP7702
        )

        signer.setCredentials(credentials)
        scaProvider = provider.apply { connect(account) }
        alchemyToken = ERC20.load(
            USDC_TOKEN_ADDRESS,
            provider.rpcClient,
            credentials,
            DefaultGasProvider()
        )
    }

    private suspend fun sendMintUserOperation(provider: ISmartAccountProvider): String {
        val address = provider.getAddress()
        val function = Function(
            "mint",
            listOf(
                org.web3j.abi.datatypes.Address(address),
                org.web3j.abi.datatypes.generated.Uint256(BigInteger("11700000000000000000000"))
            ),
            listOf()
        )

        val encoded = FunctionEncoder.encode(function)
        return provider.sendUserOperation(
            UserOperationCallData(
                Address(USDC_TOKEN_ADDRESS),
                Numeric.hexStringToByteArray(encoded),
            )
        ).hash
    }

    private suspend fun refreshAlchemyTokenBalance() {
        val balance = alchemyToken?.balanceOf(
            scaProvider?.getAddress()
        )?.sendAsync()?.await()?.toBigDecimal()
        val balanceStr = balance?.divide(BigDecimal.TEN.pow(18))?.toPlainString()
        _uiState.postValue(_uiState.value?.copy(balance = balanceStr))
    }
}