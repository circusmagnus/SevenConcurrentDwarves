import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlin.random.Random

suspend fun main() {
    val kitchen = Kitchen(CoroutineScope(Dispatchers.Default))

    println("Dwarves are about to dine. Fuel level: ${kitchen.getFuelLevel()} \n")

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

    val fuelRemaining = kitchen.getFuelLevel()
    println("\nDinner eaten, fuel remaining: $fuelRemaining")
    kitchen.cancel()
}

/////////////////////////////////////////////////////////////////

class Kitchen(scope: CoroutineScope) : CoroutineScope by scope {

    private val inbox = Channel<Message>(8).apply {
        launch {
            var currentDinner: Dinner = Dinner.EMPTY
            val fuel = Fuel(10)

            consumeEach { message ->
                when (message) {
                    is GetDinner -> {
                        val readyDinner =
                            if (currentDinner.isReady) currentDinner
                            else cook(fuel).also { cooked -> currentDinner = cooked }

                        message.plate.complete(readyDinner)
                    }

                    is GetFuelLevel -> {
                        message.report.complete(fuel.amount)
                    }
                }
            }
        }
    }

    suspend fun getDinner(): Dinner {
        val plate = CompletableDeferred<Dinner>()
        inbox.send(GetDinner(plate))
        return plate.await()
    }

    suspend fun getFuelLevel(): Int {
        val report = CompletableDeferred<Int>()
        inbox.send(GetFuelLevel(report))
        return report.await()
    }

}

sealed class Message
class GetDinner(val plate: CompletableDeferred<Dinner>) : Message()
class GetFuelLevel(val report: CompletableDeferred<Int>) : Message()

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