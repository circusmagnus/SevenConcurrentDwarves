import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlin.random.Random

suspend fun main() {
    val kitchen = Kitchen(CoroutineScope(Dispatchers.Default))

    println("Dwarves are about to dine. Fuel level: ${kitchen.fuel.amount} \n")

    coroutineScope {
        repeat(7) { dwarfId ->
            val dwarf = launch {
                val dinner = kitchen.getDinner()
                println("Dwarf #$dwarfId is eating dinner: $dinner")
            }
            if (dwarfId == 0) {
                delay(10)
                dwarf.cancel()
            }
        }
    }

    val fuelRemaining = kitchen.fuel.amount
    println("\nDinner eaten, fuel remaining: $fuelRemaining")
    kitchen.cancel()
}

/////////////////////////////////////////////////////////////////

class Kitchen(scope: CoroutineScope) : CoroutineScope by scope {

    val fuel = Fuel(10)

    private val inbox = Channel<CompletableDeferred<Dinner>>(8)

    init {
        launch {
            var currentDinner: Dinner = Dinner.EMPTY

            inbox.consumeEach { plate ->
                val readyDinner =
                    if (currentDinner.isReady) currentDinner
                    else cook(fuel).also { cooked -> currentDinner = cooked }

                plate.complete(readyDinner)
            }
        }
    }

    suspend fun getDinner(): Dinner {
        val plate = CompletableDeferred<Dinner>()
        inbox.send(plate)
        return plate.await()
    }
}

//////////////////////////////////////////////////////////////////

suspend fun cook(fuel: Fuel): Dinner {
    fuel.amount--
    delay(70)
    return Dinner(ingredients = Random.nextInt(100).toString(), isReady = true)
}

//////////////////////////////////////////////////////////////////////

data class Dinner(
    val ingredients: String,
    val isReady: Boolean
) {
    companion object {
        val EMPTY = Dinner("", isReady = false)
    }
}

data class Fuel(var amount: Int)