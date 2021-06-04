package test.commandline

import commandline.CmdOptions
import commandline.CommandlineArgumentParser
import commandline.CommandlineArgumentParser.Companion.getCmdOptions
import commandline.CommandlineArgumentParser.Companion.parse
import io.kotest.assertions.fail
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class CommandlineArgumentParserTest : StringSpec({
    class Options(
        var stringTestOptionOne: String = "",
        var stringTestOptionTwo: String = "",
        var stringTestOptionThree: String = "",
    ) : CmdOptions

    class ArgParser(
        override val args: Array<String>,
        override val possibleArguments: Map<Regex, (value: String?, options: Options) -> Unit>,
        override val possibleArgumentSeparatorAndLeftOutMap: Map<Char, Boolean>,
    ) : CommandlineArgumentParser<Options>

    val parser = ArgParser(
        arrayOf("-stringTestOptionOne=hello", "-stringTestOptionTwohello2", "-stringTestOptionThree", "hello3"),
        possibleArguments = mapOf(
            CommandlineArgumentParser.Option("stringTestOptionOne") { value, options ->
                value?.let { options.stringTestOptionOne = it }
            },
            CommandlineArgumentParser.Option("stringTestOptionTwo") { value, options ->
                value?.let { options.stringTestOptionTwo = it }
            },
            CommandlineArgumentParser.Option("stringTestOptionThree") { value, options ->
                value?.let { options.stringTestOptionThree = it }
            },
        ),
        possibleArgumentSeparatorAndLeftOutMap = mapOf(
            ' ' to true,
            '=' to false,
        ),
    )

    "single option equals separator" {
        parser.getSingleOption(Regex("stringTestOptionOne")) shouldBe "hello"
    }
    "single option no separator"{
        parser.getSingleOption(Regex("stringTestOptionTwo")) shouldBe "hello2"
    }
    "single option space separator"{
        parser.getSingleOption(Regex("stringTestOptionThree")) shouldBe "hello3"
    }

    "CmdOptions set right"{
        val options = parser.parse()
        options.stringTestOptionOne shouldBe "hello"
        options.stringTestOptionTwo shouldBe "hello2"
        options.stringTestOptionThree shouldBe "hello3"
    }

})