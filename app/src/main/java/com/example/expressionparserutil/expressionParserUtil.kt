package com.example.expressionparserutil

/*-This small library is an implementation of the shunting-yard algorithm:
https://en.wikipedia.org/wiki/Shunting-yard_algorithm

-Every number is calculated as a Double in order to support floating-point values. The ideal from a performance standpoint
 might be to type-check and only use Double when necessary, big I don't think such a small potential efficiency gain is
 worth the effort for such a small project. If you want to remove the ".0" on whole numbers, do it on your UI controller
 or ViewModel*/
class ExpressionParserUtil {

    companion object {
        const val operators = "-+*/^"
    }

    /*Map responsible for storing the value of possible saved variables so they can be understood and
    * processed by the parser. The implementation and storing of possible variables is the responsibility  of the
    * user of this library. Add saved variables into this data piece. Variables can only be stored with
    * latin letter identifiers.
    * I don't know if this implementation is useful at all but here it is. There's a function, imbedded
    * into the parser, for creating new variables directly from an expression containing the "=" symbol.*/
    private val variables = mutableMapOf<String, Double>()

    /*Function that standardizes the 'x' symbol into a '*' multiplying symbol, and the '÷' symbol into
    * a '/' character, both which can be read by the parser.*/
    private fun subSymbol(str: String): String {
        var outputString = ""

        for (i in str) {
            when (i) {
                'x' -> outputString += "*"
                '÷' -> outputString += "/"
                else -> outputString += i
            }
        }
        return outputString
    }


    /*Function responsible for processing digits in a string into individual numbers in a list accordingly.
    * It outputs a list of strings without spaces, in which each item is either a number (with it's potential unary
    * operator when necessary for the parsing) or a symbol. This is so the parser knows that numbers with multiple
    * digits are individual numbers and not multiple different ones.*/
    private fun numParser(str: String): List<String> {
        val outputList = mutableListOf<String>()
        var tempVar = ""
        var number = false
        for (i in str.indices) {
            when {
                str[i] == ' ' -> continue
                !str[i].isDigit() && str[i] != '.'-> {
                    number = false
                    if (tempVar.isNotEmpty()) outputList.add(tempVar); tempVar = ""
                    outputList.add(str[i].toString())
                }
                str[i].isDigit() && !number -> {
                    number = true
                    tempVar += str[i]
                }
                str[i].isDigit() && number -> {
                    tempVar += str[i]
                }
                str[i] == '.' -> {
                    tempVar += str[i]
                }
            }
        }
        if (tempVar.isNotEmpty()) outputList.add(tempVar)
        return outputList
    }

    /*Function that implements an algorithm for processing double operators and outputting a mutable list
    * with the corresponding resulting values. It is also responsible for replacing variables with it's
    * corresponding values according to the "variables" mutable map.*/
    private fun operatorParser(str: String): MutableList<String> {
        var outputString = str

        if (variables.isNotEmpty()) {
            for (entry in variables) {
                outputString = outputString.replace(entry.key, entry.value.toString())
            }
        }

        val outputList = numParser(outputString).toMutableList()

        var doubleOperator = false
        do {
            for (i in outputList.indices) {
                when {
                    outputList[i] === outputList.last() -> break
                    outputList[i] == "+" && outputList[i + 1] == "+" -> outputList.removeAt(i + 1)
                    outputList[i] == "+" && outputList[i + 1] == "-" -> outputList.removeAt(i)
                    outputList[i] == "-" && outputList[i + 1] == "-" -> {
                        outputList.removeAt(i + 1)
                        outputList[i] = "+"
                    }
                    outputList[i] == "-" && outputList[i + 1] == "+" -> outputList.removeAt(i + 1)
                }
            }

            for (i in outputList.indices) {
                if (outputList[i] === outputList.last()) break
                if ((outputList[i] == "+" || outputList[i] == "-") && (outputList[i + 1] == "+" || outputList[i + 1] == "-")){
                    doubleOperator = true
                    break
                } else doubleOperator = false
            }
        } while (doubleOperator)

        //Solves some bugs with double + and - operators
        if (outputList.first() == "+" || outputList.first() == "-") {
            when (outputList.first()) {
                "+" -> outputList.removeAt(0)
                "-" -> {
                    val firstNumber = outputList[1]
                    outputList[1] = "-$firstNumber"
                    outputList.removeAt(0)
                }
            }
        }

        //Solves some bugs with a binary operators followed by a unary operators.
        do {
            for (i in outputList.indices) {
                if (outputList[i] === outputList.first()) continue
                if (outputList[i] === outputList.last()) break
                if (outputList[i] == "+" || outputList[i] == "-" && outputList[i - 1] == "*" || outputList[i - 1] == "/" || outputList[i - 1] == "(") {
                    when (outputList[i]) {
                        "+" -> if (outputList[i + 1].first().isDigit() && !outputList[i - 1].first().isDigit()) {
                            val value = outputList[i + 1]
                            outputList[i + 1] = "+$value"
                            outputList.removeAt(i)
                            break
                        }

                        "-" -> {
                            val value = outputList[i + 1]
                            outputList[i + 1] = "-$value"
                            outputList.removeAt(i)
                            break
                        }
                    }
                }
            }
            for (i in outputList.indices) {
                if (outputList[i] === outputList.last()) break
                if ((outputList[i] == "*" || outputList[i] == "/") && (outputList[i + 1] == "+" || outputList[i + 1] == "-")){
                    doubleOperator = true
                    break
                } else doubleOperator = false
            }
        } while (doubleOperator)

        return outputList
    }


    /*Implements the shunting-yard algorithm responsible for converting the expression in the infix format
     into a post-fix formatted  expression (Reverse Polish Notation, RPN), easier for calculating.*/
    private fun infixToPostfix(exp: MutableList<String>): List<String>{
        val stack = mutableListOf<String>()
        val outputQueue = mutableListOf<String>()

        fun priority(str: String): Int {
            return when (str) {
                "-" -> 1
                "+" -> 1
                "*" -> 2
                "/" -> 2
                else -> 0
            }
        }

        for (i in exp.indices) {
            when {
                exp[i].first().isDigit() -> outputQueue.add(exp[i])

                exp[i].length > 1 && exp[i].first() == '-' || exp[i].length > 1 && exp[i].first() == '+' -> outputQueue.add(exp[i])

                operators.contains(exp[i]) -> {
                    if (stack.isEmpty()) stack.add(exp[i]) else if (stack.isNotEmpty()) {
                        while (stack.isNotEmpty() && priority(stack.last()) > priority(exp[i])) {
                            outputQueue.add(stack.last())
                            stack.removeLast()
                        }
                        stack.add(exp[i])
                    }
                }

                exp[i] == "(" -> stack.add(exp[i])

                exp[i] == ")" -> {
                    while (stack.last() != "(") {
                        outputQueue.add(stack.last())
                        stack.removeLast()
                    }
                    stack.removeLast()
                }
            }
        }

        while (stack.isNotEmpty()) {
            outputQueue.add(stack.last())
            stack.removeLast()
        }

        return outputQueue
    }


    /*Algorithm that calculates the result of a post-fix formatted expression.*/
    private fun postfixCalc(lst: List<String>): String {
        val stack = mutableListOf<String>()
        for (i in lst) {
            when {
                i.first().isDigit() -> stack.add(i)
                i === lst.first() && (i.contains(Regex("-.")) || i.contains(Regex("\\+."))) -> stack.add(i)
                i.contains(Regex("-.")) -> stack.add(i)
                i.contains(Regex("\\+.")) -> stack.add(i)
                i == "-" -> {
                    val result = stack[stack.size - 2].toDouble() - stack.last().toDouble()
                    repeat(2) {stack.removeLast()}
                    stack.add(result.toString())
                }
                i == "+" -> {
                    val result = stack[stack.size - 2].toDouble() + stack.last().toDouble()
                    repeat(2) {stack.removeLast()}
                    stack.add(result.toString())
                }
                i == "*" -> {
                    val result = stack[stack.size - 2].toDouble() * stack.last().toDouble()
                    repeat(2) {stack.removeLast()}
                    stack.add(result.toString())
                }
                i == "/" -> {
                    val result = stack[stack.size - 2].toDouble() / stack.last().toDouble()
                    repeat(2) {stack.removeLast()}
                    stack.add(result.toString())
                }
            }
        }
        return stack.first()
    }


    /*Function responsible for processing the value of an expression that might be creating a new
    * variable. It applies the previews functions and saves the value of the variable in the "variables"
    * map object.*/
    private fun variableParser(string: String) {
        var invalidName = false

        if (string.contains(Regex(".=."))) {
            for (char in string.substringBefore('=')) if (char.toLowerCase() !in 'a'..'z' && char != ' ') {
                invalidName = true
                break
            }

            var tempString = string.substringAfter('=')

            if (variables.isNotEmpty()) {
                for (entry in variables) {
                    tempString = tempString.replace(entry.key, entry.value.toString())
                }
            }

            for (char in tempString) {
                if (!char.isDigit() && !operators.contains(char) && char != '(' && char != ')' && char != ' ') {
                    invalidName = true
                    break
                }
            }

            if (!invalidName) {
                val variableKey = string.substringBefore('=').trim()
                val variableValue = this.calc(string.substringAfter('=').trim())
                variables[variableKey] = variableValue.toDouble()
            }
        }
    }

    /*Takes a given expression string and returns the mathematical result also in a string format.*/
    fun calc (str: String): String {
        return postfixCalc(infixToPostfix(operatorParser(subSymbol(str))))
    }

    /*Function that checks if the number of parenthesis is correct (if every opening parentheses
    * has a corresponding closing parentheses).*/
    fun parenthesesCheck (str: String): Boolean {
        var opCount = 1
        var clCount = 1
        for (i in str) {
            when (i) {
                '(' -> opCount++
                ')' -> clCount++
            }
        }
        if (opCount - clCount != 0) return false
        return true
    }
}
