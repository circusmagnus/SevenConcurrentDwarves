import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlin.random.Random

suspend fun main() {
    val kitchenScope = CoroutineScope(Dispatchers.Default)
    val kitchen = Kitchen(kitchenScope)

    println("Dwarves are about to dine. Fuel level: ${kitchen.getCurrentFuel()} \n")

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

    kitchen.invalidateDinner()
    val fuelRemaining = kitchen.getCurrentFuel()
    kitchen.cancel()
    println("\nDinner eaten, fuel remaining: $fuelRemaining")
}

/////////////////////////////////////////////////////////////////

class Kitchen(scope: CoroutineScope) : CoroutineScope by scope {


    private val messages = Channel<Message>(capacity = 1024).also { channel ->
        launch {
            var currentDinner: Dinner = Dinner.EMPTY
            val fuel = Fuel(10)
            channel.consumeEach { message ->
                when (message) {
                    is GetDinner -> {
                        val dinner =
                            if (currentDinner.isReady) currentDinner
                            else cook(fuel).also { cooked -> currentDinner = cooked }

                        message.plate.complete(dinner)
                    }

                    InvalidateDinner -> {
                        currentDinner = currentDinner.copy(isReady = false)
                    }

                    is GetFuel -> {
                        message.report.complete(fuel.amount)
                    }
                }

            }
        }
    }

    suspend fun getDinner(): Dinner{
        val plate = CompletableDeferred<Dinner>()
        messages.send(GetDinner(plate))
        return plate.await()
    }

    suspend fun invalidateDinner() {
        messages.send(InvalidateDinner)
    }

    suspend fun getCurrentFuel(): Int {
        val report = CompletableDeferred<Int>()
        messages.send(GetFuel(report))
        return report.await()
    }

    sealed class Message
    class GetDinner(val plate: CompletableDeferred<Dinner>) : Message()
    object InvalidateDinner : Message()
    class GetFuel(val report: CompletableDeferred<Int>): Message()
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