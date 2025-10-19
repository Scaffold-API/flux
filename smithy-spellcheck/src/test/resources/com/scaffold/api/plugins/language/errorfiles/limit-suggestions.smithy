$version: "2"

metadata validators = [
    {
        name: "SpellCheck",
        configuration: {
            limit: 2
        }
    }
]

namespace com.spellcheck.test

structure ThingSpelingStuff {
    member_a: String
}