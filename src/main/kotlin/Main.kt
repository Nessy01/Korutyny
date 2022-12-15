import kotlinx.coroutines.*

fun main() {
    runBlocking{ //umozliwia wykonanie korutyny w biezacym watku, jeden watek moze wykonywac wiele korutyn
//        val job = launch {
//            println("Starting")
//            delay(1000) // czeka sekunde
//            println("Exiting") //nie wyswietli sie bo korutyna zostala anulowana job.cancel
//        }
//        delay(500)
//        job.cancel()
        //dispatcher
        launch(Dispatchers.IO) {//dispatcher określa wątek wykonujący korutynę
            println("A coroutine on thread ${Thread.currentThread().name}")
        }
        // pokazanie punktu wstrzymania
        val job = launch {
            println("Start #1 on thread ${Thread.currentThread().name}")
            yield() // suspension point
            println("Exit #1 on thread ${Thread.currentThread().name}")
            delay(1000) // czeka sekunde
            println("XD")
        }
//        job.cancel() // do zaprezentowania jak canceluje
        launch {
            println("Start #2 on thread ${Thread.currentThread().name}")
            println("Exit #2 on thread ${Thread.currentThread().name}")
        }
//        Wykonana została pierwsza instrukcja z pierwszej korutyny.
//        Korutyna w punkcie wstrzymania zostaje wstrzymana.
//        Druga korutyna zostaje w całości wykonana.
//        Wstrzymana wcześniej korutyna zostaje wznowiona.
    }

}