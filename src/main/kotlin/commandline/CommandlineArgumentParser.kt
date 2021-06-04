@file:Suppress("FunctionName")

package commandline

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

interface CommandlineArgumentParser<T : CmdOptions> {
    val args: Array<String>
    val possibleArguments: Map<Regex, (value: String?, options: T) -> Unit>

    /**
     * Example: possibleArgumentSeparatorAndLeftOutMap = mapOf(' ' to true, '=' to false)
     * This would allow -v=2.0 or -v2.0 or -v 2.0
     * This map is evaluated left to right
     */
    val possibleArgumentSeparatorAndLeftOutMap: Map<Char, Boolean>

    /**
     * Always uses the last occurrence of the options
     * @param separatorsAndLeftOutMap is evaluated *left to right*
     */
    private fun getSingleOption(
        name: Regex,
        separatorsAndLeftOutMap: Map<Char, Boolean> = possibleArgumentSeparatorAndLeftOutMap
    ): String? {
        val findOptionInAllSeparators: String? = null
        for ((separator, canBeLeftOut) in separatorsAndLeftOutMap) {
            singleOptionHelper(name, separator, canBeLeftOut, separatorsAndLeftOutMap.keys)
        }
        return findOptionInAllSeparators
    }

    fun singleOptionHelper(
        name: Regex,
        separator: Char,
        separatorCanBeLeftOut: Boolean,
        allSeparators: Iterable<Char>
    ): String? {
        val optionIdentifier = "[-]+(${name.pattern})[$separator]${if (separatorCanBeLeftOut) "?" else ""}"
        //All separators and '-' can't be part of the actual option value
        val actualOptionPart = "[^[${allSeparators.joinToString("")}-]]+"
        val regex = Regex("$optionIdentifier$actualOptionPart")
        return if (separator != ' ') {
            val foundArgumentIdentifier = args.findLast { it.matches(regex) }
            foundArgumentIdentifier?.split(Regex(optionIdentifier))
                ?.getOrNull(1)
                ?.lowercase()
        } else {
            //Has to be handled separately, because then it is not in one array entry
            val foundArgumentIdentifier = args.findLast { it.matches(Regex("$optionIdentifier($actualOptionPart)?")) }
            if (foundArgumentIdentifier?.matches(regex) == true) {
                foundArgumentIdentifier.split(Regex(optionIdentifier)).getOrNull(1)?.lowercase()
            } else {
                args.getOrNull(args.lastIndexOf(foundArgumentIdentifier) + 1)?.lowercase()
            }
        }
    }

    fun Option(
        first: String,
        second: (String?, CmdOptions) -> Unit
    ): Pair<Regex, (String?, CmdOptions) -> Unit> {
        return Regex(first) to second
    }

    fun getCmdOptionsInternal(kClass: KClass<T>): T {
        val options: T = kClass.constructors.firstOrNull { constructor ->
            constructor.parameters.isEmpty() || constructor.parameters.all { kParameter -> kParameter.isOptional }
        }
            ?.callBy(emptyMap())
            ?: throw NoZeroArgumentConstructorException(kClass.constructors)
        possibleArguments.forEach { entry ->
            val value: String? = getSingleOption(entry.key)
            entry.value(value, options)
        }
        return options
    }

}

inline fun <reified T : CmdOptions> CommandlineArgumentParser<T>.getCmdOptions(): T {
    return getCmdOptionsInternal(T::class)
}

class NoZeroArgumentConstructorException(constructors: Collection<KFunction<Any>>? = null) :
    Exception(
        if (constructors != null)
            """
                Couldn't find a matching constructor that can be called with Zero arguments, all constructors are: $constructors
            """.trimIndent()
        else
            "Couldn't find a matching constructor that can be called with Zero arguments"
    ) {
}
