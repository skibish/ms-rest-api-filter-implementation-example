# Microsoft REST API Guidelines filter implementation example

This repository contains an example of how [Microsoft REST API Guidelines filter](https://github.com/microsoft/api-guidelines/blob/vNext/Guidelines.md#97-filtering) can be implemented in [Kotlin](https://kotlinlang.org) with help of ANTLR.

## Run example

```bash
./gradlew run --console=plain
input filter: john eq 'doe'
output:
{ "john": { $eq: "doe" } }
```
