import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() {
    //example1()
    //example2()
    example3()
}
fun example1(){
    runBlocking{ //umozliwia wykonanie korutyny w biezacym watku, jeden watek moze wykonywac wiele korutyn
        val job = launch {
            println("Start #1 on thread ${Thread.currentThread().name}")
            yield() // suspension point
            println("Exit #1 on thread ${Thread.currentThread().name}")
            delay(1000) // czeka sekunde
            println("XD")
        }
        launch {
            println("Start #2 on thread ${Thread.currentThread().name}")
            println("Exit #2 on thread ${Thread.currentThread().name}")
        }
    }
    GlobalScope.launch{//zamist runBlocking
            println("KOLEJNY TASK PO WSZYSTKICH INNYCH")
    }


    }

fun example2() {
    runBlocking {
        val job = launch {
            println("Start #1 on thread ${Thread.currentThread().name}")
            //yield() // suspension point
            println("Exit #1 on thread ${Thread.currentThread().name}")
            delay(1000) // czeka sekunde
            println("MESSAGE NOT VIEWED")
        }
        delay(500)
        job.cancel() // do zaprezentowania jak canceluje
    }
}
fun example3(){
    runBlocking {
        val continuum = measureTimeMillis {
        println("FIRST PRINT")
            launch {
                val time = measureTimeMillis {
                    val one = async { doSomethingUsefulOne() }
                    val two = async { doSomethingUsefulTwo() }
                    val three = async { doSomethingUsefulTwo() }
                    println("The answer is ${one.await() + two.await() + three.await()}")
                }
                println("Completed in $time ms")
            }
            delay(4100)
            println("DELAY HAS ENDED")
            println(doSomethingUsefulOne())

    }
        println("WHOLE PROGRAM completed in $continuum ms")
    }
    //KORUTYNY WYKONUJĄ SIĘ ASYNCHRONICZNIE, ACZKOLWIEK PIERWSZE ZOSTANĄ WYPRINTOWANE
}
suspend fun doSomethingUsefulOne(): Int {
    delay(4000L) // pretend we are doing something useful here
    return 13
}
suspend fun doSomethingUsefulTwo(): Int {
    delay(1500L) // pretend we are doing something useful here, too
    return 29
}