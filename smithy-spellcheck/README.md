# Smithy Proofread
This package provides two proofreading linters for Smithy models: 
1. `SpellCheck` - A basic spell checker for shape names and docstrings
2. `Proofread` - Basic grammatical checks for docstrings in models

## SpellCheck 
To add the spellcheck linter to your model, add the linter 
to the `metadata validators` list in your model file.

```smithy 
metadata validators = [
    { name: "SpellCheck" }
]
namespace com.example.my.namespace
```

To configure the linter, add parameters to the `configuration` property of 
the validator entry as follows: 

```smithy
metadata validators = [
    { 
        name: "SpellCheck",
        configuration: {
            limit: 2,
            // Other properties
        }
    }
]
```

### Configuration
The `SpellCheck` linter offers a number of configuration options to 
tune its behavior

| Parameter  | Type           | Description                                                   | Value            |
|------------|----------------|---------------------------------------------------------------|------------------|
| ignore     | `List<String>` | Words to ignore                                               | `["foo", "bar"]` |
| docstrings | `boolean`      | Whether to check docstrings (defaults to `true`)              | `false`          |
| limit      | `int`          | Maximum number of suggestions to provide for misspelled words | `4`              |

## Proofread 
The `Proofread` linter executes a number of basic grammar checks 
for docstrings in your model. 

To apply this linter to your Smithy models, add the linter to your 
`metadata validators` list in your model file.

```smithy 
metadata validators = [
    { name: "Proofread" }
]
namespace com.example.my.namespace
```

## Non-English Lanugage Support

While the current version only supports english proofreading, the 
package is designed to allow for other language support. If you 
are interested in support for a language other than english, please 
cut an issue on the repo detailing which language you would like supported.
