$version: "2"

metadata validators = [
    {name: "SpellCheck"}
]

// Namespace typo
namespace com.spellcheck.tst

// Struct Docstring typo
/// Contains a speling error!
structure MyStructure {
    // Member docstring typo
    /// Member docs errir
    member: String
}

// Structure name error
structure ThingSpelingStuff {
    // Member name error
    member_abot: String
}


