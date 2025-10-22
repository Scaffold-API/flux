$version: "2"

metadata validators = [
    {name: "SpellCheck"}
]

namespace com.spellcheck.test

/// This docstring contains a supported tag <h1>speling error</h1>.
structure ShouldError {
    field: String
}

/// This docstring contains an unsupported tag <pre>abot that</pre>.
structure ShouldNotError {
    field: String
}