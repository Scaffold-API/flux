$version: "2"

metadata validators = [
    {
        name: "SpellCheck",
        configuration: {
            docstrings: false
        }
    }
]

namespace com.spellcheck.test

/// Contains a speling error!
structure MyStructure {
    // Member docstring typo
    /// Member docs errir
    member: String
}
