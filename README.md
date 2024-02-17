# AA-Kotlin

### Implementation of [ERC-4337: Account Abstraction](https://eips.ethereum.org/EIPS/eip-4337) in Kotlin

For a high-level overview, read [this blog post](https://crewapp.xyz/posts/account-abstraction-mobile/).  
For Swift library, [see this](https://github.com/syn-mcj/aa-swift).

## Installation

```
implementation("org.aakotlin:core:0.1.2")
implementation("org.aakotlin:alchemy:0.1.2")
```

## Getting started

Send User Operation with Alchemy provider:

```
val provider = AlchemyProvider(
    ...
).withAlchemyGasManager(
    ...
)

val account = LightSmartContractAccount(...)
provider.connect(account)

val function = Function(
    "mint", // contract function name
    listOf(
        org.web3j.abi.datatypes.Address(provider.getAddress().address), // function parameters
    ),
    listOf()
)

val encoded = FunctionEncoder.encode(function)
provider.sendUserOperation(
    UserOperationCallData(
        contractAddress,
        Numeric.hexStringToByteArray(encoded),
    )
)
```
\
Check the Example app for the full code:
\
\
<img src="https://crewapp.xyz/images/example.gif" alt="Example gif" style="height: 600px; margin: 0 auto; display: block;" />

## Documentation
This repository is based on Alchemy's [aa-sdk](https://github.com/alchemyplatform/aa-sdk). Going through their [Account Kit documentation](https://accountkit.alchemy.com/overview/introduction.html) will give you a good idea of the structure of this library.

## Contributing
Contributions are welcome. Just open a well-structured issue or a PR.
