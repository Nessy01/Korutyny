import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.awt.font.TextMeasurer
import kotlin.system.measureTimeMillis

/**
 * Program napisany w celu zaprecentowania działania logiki korutyn, wątków,
 * zakresów, jak i przekazywania danych między nimi
 */
fun main() {
    example1()
}

/**
 * Przykład pierwszy prezentuje podstawowe zachowanie korutyn
 * używa funkcja yield() sprawia, że zadanie (job) zostaje wstrzymane i kontynuowane po wykananiu sekwencyjnym
 * pozostałych zadań
 * Opkaowanie komend w runBlocking{} sprawia, że wątek czeka na zakończenie wszystkich jobów wewnątrz scope,
 * natomiast użycie globalnego zakresu (GlobalScope) nie gwarantuje pewności wykonania kodu wewnątrz.
 */
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

/**
 * Przykład prezentujący użycie zakresów GlobalScrope, runBlocking
 * launch i async - są w tym wypadku tożsame, różnica w metodzyce asynca jest taka,
 * że funkcja ta zwraca wynik
 */
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

/**
 * Przykład prezentujący anulowanie zadania po przekroczeniu danego czasu
 * Komunikat 'MESSAGE NOT VIEWED' nie wyświetli się z powodu zcancellowania joba
 * po upływie 500 ms
 */
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

/**
 * Wszystko wykonuje się asynchronicznie
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

/**
 * Funkcja analogiczna do przykładu example4, jedyna różnica jest taka,
 * że tutaj metody nie wywołują się asynchronicznie, a chronologicznie
 * Czas wykonania całości programu jest dłuższy niemal dwukrotnie
 */
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

/**
 * Metoda, która wykonuje w teorii pewne długotrwałe obliczenia
 * i zwraca wynik
 */
suspend fun doSomethingUsefulOne(): Int {
    delay(4000L) // pretend we are doing something useful here
    return 13
}

/**
 * Metoda, która wykonuje w teorii pewne długotrwałe obliczenia
 * i zwraca wynik
 */
suspend fun doSomethingUsefulTwo(): Int {
    delay(1500L) // pretend we are doing something useful here, too
    return 29
}

/**
 * CORUTINE DISPATCHERS - opisują w jakich wątków korutyna używa
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

/**
 * Funkcje zawieszona
 * m.in. delay() mogą być wywołane tylko z korutyny lub innej zawieszonej funkcji
 */
fun example7() {
    runBlocking {
        suspendDelay()
    }
    println("Done.")
}

/**
 * Funkcja zawieszona wywołana z poprzez funkcje zawieszoną
 */
suspend fun suspendDelay() {
    delay(1000L)
}

/**
 * Przykład prezentujący działanie runBlocking { }
 * W przypadku normalnego wywołania jobów, Thread nie czeka na ich zakończenie
 * - napis 'World' się nie pokaże na ekranie, natomiast w przypadku opakowania go
 * w zakres runBlocking i wywołania polecenia join(), program będzie czekał na wykonanie joba
 */
fun example8() {
//fun example8() = runBlocking {
    val job = GlobalScope.launch {
        delay(2000L)
        println("World")
    }
    println("Hello")

//    job.join()
}

/**
 * Przykład pokazujący działanie Channel interface - przekazywania danych
 * między korutynami
 */
fun example9() = runBlocking<Unit> {
    val channel = Channel<String>()
    launch {
        channel.send("A1")
        channel.send("A2")
        log("A done")
    }
    launch {
        channel.send("B1")
        log("B done")
    }
    launch {
        repeat(3) {
            val x = channel.receive()
            log(x)
        }
    }
}

fun log(message: Any?) {
    println("[${Thread.currentThread().name}] $message")
}

/**
 * Funkcja yield()
 * Punkt zawieszenia
 */
fun example10() {
    runBlocking {
        launch {
            println("Start #1 on thread ${Thread.currentThread().name}")
            yield() // suspension point
            println("Exit #1 on thread ${Thread.currentThread().name}")
        }
        launch {
            println("Start #2 on thread ${Thread.currentThread().name}")
            println("Exit #2 on thread ${Thread.currentThread().name}")
        }
    }
}
