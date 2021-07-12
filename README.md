# ExpressionParserUtil Android Library
Implementation of the shunting-yard algorithm for parsing and calculating math expressions on android applications. More information about the shunting-yard algorithm
can be found on the wiki page: https://en.wikipedia.org/wiki/Shunting-yard_algorithm

Project uses pure Kotlin and implements basic stacks and queues. It is intended as a library for future projects.
This project is based on a problem proposed on the Kotlin JetBrains Academy course.

# How to use

You can import the library to your own project using JitPack (just follow instructions):
https://jitpack.io/#jfransp/ExpressionParserUtil-Android-Library

The library not yet supports exponentiation since it wasn't useful for me when I wrote it - it accepts all the remaining basic operator symbols and also deals with double operator configurations.

To use it just instanciate an `ExpressionParserUtil()` object (if you wish to use the internal variable functions you should remember that the variable/value pairs are saved always within the same object) - then, just use the object's functions to perform operations. Example:

```
val mycalculator = ExpressionParserUtil()
```

The `calc()` function accepts an expression (string) and if the expression is valid returns the result (string). Example:

```
mycalculator.calc("10*2(2)")
```
Returns:
`40`

The `processVariable()` function parsers the expression and if it contains an "="(equals) sign, it considers it as an separator and saves the left side string as a variable in the `mycalculator.variables` (mutable map) and the right side string (number or expression result) as it's value, returning a `Unit`. The `variables` variable has no pre-set values. Only latin letters are accepted as valid variable names. Afterwards, every time that symbol is passed within an expression to the same `ExpressionParserUtil` object, it will automatically process it as it's corresponding saved value.
PS: If the `calc()` function is called with an expression containing the "="(equals) symbol, it will throw an exception.

The `parentesesCheck()` function returns `true` if the parentheses pairing is valid (if every opening parentheses has a closing parentheses) and `false` if it isn't -it's use is not necessary while using the `calc()` function; use it only if you need it.

Invalid expressions always throw exceptions, exception handling is up to you.

That's it (:


