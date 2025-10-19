$version: "2"

metadata validators = [
    {
        name: "SpellCheck",
        configuration: {
            ignore: ["abot", "speling"]
        }
    }
]

namespace com.spellcheck.test

structure ThingSpelingStuff {
    member_abot: String
}