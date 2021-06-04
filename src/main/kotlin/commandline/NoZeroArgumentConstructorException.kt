package commandline

import kotlin.reflect.KFunction


class NoZeroArgumentConstructorException(constructors: Collection<KFunction<Any>>? = null) :
    Exception(
        if (constructors != null)
            """
                Couldn't find a matching constructor that can be called with Zero arguments, all constructors are: $constructors
            """.trimIndent()
        else
            "Couldn't find a matching constructor that can be called with Zero arguments"
    )
