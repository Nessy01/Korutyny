import kotlinx.coroutines.*
import java.awt.font.TextMeasurer
import kotlin.system.measureTimeMillis

fun main() {
    example6()
}

fun example1() {
    runBlocking { // umozliwia wykonanie korutyny w biezacym watku, jeden watek moze wykonywac wiele korutyn
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
    GlobalScope.launch {//zamist runBlocking
        println("KOLEJNY TASK PO WSZYSTKICH INNYCH")
    }
}

fun example2() {
    GlobalScope.launch {
        delay(1000)
        println("GLOBAL SCOPE")
    }

    runBlocking {

        launch {
            delay(2000)
            println("FIRST MESSAGE")
        }
        launch {
            delay(2000)
            println("SECOND MESSAGE")
        }
        launch {
            delay(100)
            println("THIRD MESSAGE")
        }
        async {
            println("Hello there")
        }

    }

    println("RUNBLOCKING HAS ENDED")
}

fun example3() {
    runBlocking {
        val job = launch {
            println("Hello world")
            delay(1000)
            println("MESSAGE NOT VIEWED")
        }
        delay(500)
        job.cancel() // cancells job coroutine
    }
}

/** Wszystko wykonuje się asynchronicznie
 * na początku wykona się "FIRST PRINT" ponieważ najszybciej będzie trwać jego wykonanie,
 * następnie odpala się korutyna, w której asynchronicznie wykonują się dane funkcje, a więc całość będzie trwać tyle,
 * co najdłuższa funkcja w korutynie, a więc 4 sekundy. Jednocześnie asynchronicznie wykonuje się funkcja delay(4100)
 * a więc praktycznie zaraz po poprzedniej korutynie delay przestaje działać i od razu wykonują się dalsze instrukcje
 * które trwają około kolejnych 4 sekund.
 * całość programu zatem trwa ok 8 sekund.
 * EKSPERYMENT: Zmniejsz delay do 1 sekundy i zgadnij co się stanie
 */
fun example4() {
    val continuum = measureTimeMillis {
        runBlocking {

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

            launch {
                val time2 = measureTimeMillis {
                    delay(4100)
                    println(doSomethingUsefulOne())
                }
                println("Part 2 lasted for $time2")
            }
        }
    }
    println("WHOLE PROGRAM completed in $continuum ms")
}

fun example5() {
    runBlocking {
        launch {
            val time = measureTimeMillis {
                println("FIRST PRINT")
                val one = doSomethingUsefulOne()
                val two = doSomethingUsefulTwo()
                val three = doSomethingUsefulTwo()
                println("The answer is: ${one + two + three}")
                delay(4100)
                println(doSomethingUsefulOne())
            }
            println("ended in $time")
        }
    }
}

suspend fun doSomethingUsefulOne(): Int {
    delay(4000L) // pretend we are doing something useful here
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1500L) // pretend we are doing something useful here, too
    return 29
}

/** CORUTINE DISPATCHERS - opisują w jakich wątków korutyna używa
 * Dzięki nim można ograniczyć wykonanie korutyny do danego wątku, wysłać je do puli wątków lub pozwolić działać bez ograniczeń
 * Launch bez parametrów dziedziczy kontekst ( a więc dispatcher ) z runBlocking korutyny, która działa w głównym wątku
 */
fun example6() {
    runBlocking {
        launch { // context of the parent, main runBlocking coroutine
            println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
        }
        launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
            println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
        }
        launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher
            println("Default               : I'm working in thread ${Thread.currentThread().name}")
        }
        launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
            println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
        }
        println("IM WORKING IN         : ${Thread.currentThread().name}")
    }
}

