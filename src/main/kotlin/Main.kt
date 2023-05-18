import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

suspend fun main() {
    val kitchen = Kitchen()

    println("Dwarves are about to dine. Fuel level: ${kitchen.fuel.amount} \n")

    coroutineScope {
        repeat(7) { dwarfId ->
            launch {
                val dinner = kitchen.getDinner()
                println("Dwarf #$dwarfId is eating dinner: $dinner")
            }
        }
    }

    val fuelRemaining = kitchen.fuel.amount
    println("\nDinner eaten, fuel remaining: $fuelRemaining")
}

/////////////////////////////////////////////////////////////////

class Kitchen {

    private val currentDinner = MutableStateFlow(Dinner.EMPTY)
    val fuel: Fuel = Fuel(AtomicInteger(10))

    suspend fun getDinner(): Dinner = currentDinner.updateAndGet { dinner ->
        if (dinner.isReady) dinner
        else cook(fuel)
    }

}

//////////////////////////////////////////////////////////////////

suspend fun cook(fuel: Fuel): Dinner {
    fuel.amount.updateAndGet { it - 1 }
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

data class Fuel(var amount: AtomicInteger)