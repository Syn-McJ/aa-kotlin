# CLAUDE.md - Project Knowledge Base

This file contains essential knowledge about the AA-Kotlin project for Claude instances working on this codebase.

## Project Overview

**AA-Kotlin** is a Kotlin implementation of ERC-4337 Account Abstraction and EIP-7702 Account Delegation, primarily designed for Android mobile applications. It provides a clean, type-safe API for interacting with smart accounts and sponsored transactions.

### Repository Structure

```
aakotlin/
├── core/                           # Core AA types and abstractions
│   ├── src/main/java/org/aakotlin/core/
│   │   ├── Types.kt               # UserOperation, Address, etc.
│   │   ├── auth/                  # EIP-7702 authorization types
│   │   ├── accounts/              # Account interfaces and base classes
│   │   ├── client/                # RPC client implementations
│   │   ├── signer/                # Signing interfaces and implementations
│   │   ├── provider/              # Provider abstractions
│   │   ├── util/                  # Utility functions
│   │   └── middleware/            # Authorization middleware
├── alchemy/                       # Alchemy-specific implementations
│   ├── src/main/java/org/aakotlin/alchemy/
│   │   ├── account/               # LightSmartContractAccount, ModularAccountV2
│   │   ├── middleware/            # Gas management, paymaster, EIP-7702
│   │   └── provider/              # AlchemyProvider
├── coinbase/                      # Coinbase provider (separate module)
├── example/                       # Android example app
└── build files, configs...
```

## Key Architectural Patterns

### 1. **Provider Pattern**
- `SmartAccountProvider` is the main interface for sending user operations
- Providers are configured with middleware for gas estimation, paymaster, etc.
- `AlchemyProvider` extends base provider with Alchemy-specific functionality

### 2. **Account Abstraction**
- `ISmartContractAccount` interface defines account behavior
- `BaseSmartContractAccount` provides common functionality
- Specific implementations: `LightSmartContractAccount`, `ModularAccountV2`

### 3. **Middleware System**
- Gas estimation, paymaster data, fee calculation are all middleware
- Middleware functions transform `UserOperationStruct` objects
- Composable via extension functions like `withGasEstimator()`, `withPaymasterMiddleware()`

### 4. **Signer Abstraction**
- `SmartAccountSigner` interface for different signing methods
- `LocalAccountSigner` for private key signing
- Supports both message signing and EIP-7702 authorization signing

## Recent EIP-7702 Implementation

### Major Components Added

1. **Authorization Types** (`core/auth/AuthorizationTypes.kt`)
   - `Eip7702Auth`: Authorization tuple for user operations
   - `Authorization`: Authorization data structure  
   - `SignedAuthorization`: Signed authorization with signature components
   - `AccountMode`: Enum for DEFAULT (ERC-4337) vs EIP7702

2. **ModularAccountV2** (`alchemy/account/ModularAccountV2.kt`)
   - Supports both ERC-4337 and EIP-7702 modes
   - Implementation address: `0x69007702764179f14F51cdce752f4f775d74E139`
   - EIP-7702 mode uses EOA address directly, ERC-4337 uses counterfactual address

3. **Authorization Middleware** (`core/middleware/AuthorizationMiddleware.kt`)
   - Handles automatic EIP-7702 authorization preparation
   - Checks delegation status before creating authorizations
   - Validates authorization tuples

4. **Gas Estimation** (`alchemy/middleware/Eip7702GasEstimator.kt`)
   - Accounts for EIP-7702 delegation overhead (~50k gas)
   - Adds authorization data gas costs (~3.2k gas)
   - First-time delegation overhead (~25k gas)

5. **Provider Extensions** (`alchemy/middleware/Eip7702Provider.kt`)
   - `withEip7702Support()` extension for AlchemyProvider
   - Automatic delegation handling
   - Gas sponsorship compatibility

### Key Design Decisions

1. **Hybrid Approach**: EIP-7702 + Gas Sponsorship = ERC-4337 Infrastructure
   - Pure EIP-7702 would use standard tx pool
   - Gas sponsorship requires bundler/paymaster (ERC-4337)
   - Our implementation uses ERC-4337 flow with EIP-7702 authorization

2. **Backwards Compatibility**: 
   - Existing ERC-4337 code continues to work unchanged
   - New `eip7702Auth` field is optional in `UserOperationStruct`
   - Same `sendUserOperation()` API for both protocols

3. **Automatic Delegation**:
   - Middleware automatically detects if delegation is needed
   - Creates and signs authorization if account not yet delegated
   - Transparent to the user - just works

## Important Implementation Details

### User Operation Flow

1. **ERC-4337 Mode**:
   ```
   User → UserOp → Gas Estimation → Paymaster → Bundler → EntryPoint → Account
   ```

2. **EIP-7702 Mode**:
   ```
   User → Authorization + UserOp → Gas Estimation → Paymaster → Bundler → EntryPoint → Delegated EOA
   ```

### Gas Sponsorship Integration

- `AlchemyGasManagerConfig` works with both protocols
- Same `policyId` configuration
- EIP-7702 adds delegation overhead to gas estimates
- Uses `alchemy_requestGasAndPaymasterAndData` RPC method

### Authorization Signing

```kotlin
// EIP-7702 authorization format (simplified)
val authData = 0x05 + rlp([chainId, contractAddress, nonce])
val authHash = keccak256(authData)
val signature = sign(authHash, privateKey)
```

### Account Address Logic

- **ERC-4337**: `getAddress()` returns counterfactual address from factory
- **EIP-7702**: `getAddress()` returns signer's EOA address directly
- This allows existing EOAs to become smart accounts without migration

## Common Patterns and Best Practices

### 1. **Provider Setup**
```kotlin
val provider = AlchemyProvider(chain, config)
    .withAlchemyGasManager(gasConfig)  // Gas sponsorship
    .withEip7702Support()              // EIP-7702 delegation
```

### 2. **Account Creation**
```kotlin
// ERC-4337 (new account)
val account = ModularAccountV2(signer, chain, mode = AccountMode.DEFAULT)

// EIP-7702 (existing EOA)
val account = ModularAccountV2(signer, chain, mode = AccountMode.EIP7702)
```

### 3. **Error Handling**
- Check for authorization validation errors
- Validate gas limits for EIP-7702 operations
- Handle delegation status appropriately

### 4. **Testing Approach**
- Unit tests for each middleware component
- Integration tests with real RPC calls
- Gas estimation validation
- Authorization signing verification

## Build System and Dependencies

- **Gradle**: Multi-module Kotlin/Android project
- **Web3j**: Ethereum interaction library
- **Jackson**: JSON serialization
- **Coroutines**: Async operations
- **Maven**: Publishing to Maven Central

### Key Files
- `build.gradle.kts`: Module build configurations
- `settings.gradle.kts`: Multi-module setup
- `gradle.properties`: Project properties

## Migration and Versioning

### From LightSmartContractAccount to ModularAccountV2
- Simple constructor change
- Same signer, different account type
- EIP-7702 mode uses EOA address (no migration needed)

### Version Compatibility
- Current: `0.1.4` (ERC-4337 only)
- Next: `0.2.0` (adds EIP-7702 support)
- Breaking changes: New required dependencies for authorization

## Testing and Examples

### Example App (`example/`)
- Android application demonstrating usage
- Real wallet integration
- Transaction sending examples
- Both ERC-4337 and EIP-7702 flows

### Key Test Cases
- Authorization signing and validation
- Gas estimation accuracy
- Delegation status detection
- Provider middleware composition
- Error handling scenarios

## External Dependencies and Integrations

### Alchemy Integration
- RPC endpoints for bundler operations
- Gas manager for sponsored transactions
- Policy-based paymaster configuration

### Supported Networks
- Ethereum Mainnet/Sepolia
- Arbitrum One/Sepolia
- Optimism (planned)
- Polygon (planned)

## Common Issues and Solutions

1. **Gas Estimation**: EIP-7702 needs higher gas limits
2. **Authorization Timing**: Must check delegation before creating auth
3. **Network Compatibility**: EIP-7702 requires supporting networks
4. **Signer Management**: Ensure signer implements authorization method

## Development Workflow

1. **Core Types First**: Define data structures in `core/`
2. **Provider Implementation**: Extend providers in `alchemy/`
3. **Middleware Integration**: Compose functionality via middleware
4. **Example Integration**: Update example app
5. **Documentation**: Update README and usage guides

This knowledge base should help future Claude instances understand the project structure, recent EIP-7702 implementation, and key architectural decisions.