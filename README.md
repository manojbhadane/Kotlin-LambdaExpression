# Kotlin-LambdaExpression

Functions uses with and withoud Lambda
--------------------------------------------------------------------

        1.  addTwoNumbers(3, 7)
        2.  addTwoNumbers(3, 7, object : MyInterface {
            override fun sum(sum: Int) {
                println("--Sum is----" + sum)
              }
            })
            
        3.  // Lambda syntax  : (arg1,arg2) -> returnType = { arg1 : DataType, arg2 : DataType -> --logic here--}
            var lambda: (Int, Int) -> Int = { x: Int, y: Int -> x + y }
            addTwoNumbers(3, 7, lambda)
            addTwoNumbers(3, 7, { x: Int, y: Int -> x + y })

        4.  var LargeStringLambda: (String, String) -> Boolean = { a: String, b: String -> a.length > b.length }
            println("--is manoj string is large ----" + LargeStringLambda("manoj", "manojbhadane"))

        5.  var LargeNumberLambda: (Int, Int) -> Int = { a: Int, b: Int -> if (a > b) a else b }
            println("--large number is among 3,7 is ----" + LargeNumberLambda(3, 7))

        6.  var myName: (String) -> Unit = { name: String -> println("My name is '$name'") }
            myName("manoj")
            
            
Functions Definitions
--------------------------------------------------------------------

    fun addTwoNumbers(no1: Int, no2: Int) {
        println("--Sum is----" + (no1 + no2))
    }

    fun addTwoNumbers(no1: Int, no2: Int, sumListener: MyInterface) {
        sumListener.sum((no1 + no2))
    }

    fun addTwoNumbers(no1: Int, no2: Int, myLambda: (Int, Int) -> Int) {
        var result = myLambda(no1, no2)
        println("--Sum is----" + result)
    }

    public interface MyInterface {
        public fun sum(sum: Int)
    }
