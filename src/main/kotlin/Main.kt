import kotlinx.coroutines.*

fun main() {
    runBlocking{ //umozliwia wykonanie korutyny w biezacym watku, jeden watek moze wykonywac wiele korutyn
        
        launch {
            println("Start #1 on thread ${Thread.currentThread().name}")
            yield() // suspension point
            println("Exit #1 on thread ${Thread.currentThread().name}")
        }
        launch {
            delay(1000) // czeka sekunde
            println("Start #2 on thread ${Thread.currentThread().name}")
            println("Exit #2 on thread ${Thread.currentThread().name}")
        }
    }

}