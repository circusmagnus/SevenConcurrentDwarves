import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

suspend fun main(args: Array<String>) {
    println("Hello World!")
    val kitchen = Kitchen()

    coroutineScope {
        repeat(7) { dwarfId ->
            launch {
                val dinner = kitchen.getDinner()
                println("Dwarf #$dwarfId is eating dinner: $dinner")
            }
        }
    }
    kitchen.invalidateDinner()
    val fuelRemaining = kitchen.fuel.amount
    println("Dinner eaten, fuel remaining: $fuelRemaining")
}

class Kitchen {

    private var currentDinner: Dinner = Dinner.EMPTY
    val fuel: Fuel = Fuel(10)

    suspend fun getDinner(): Dinner = if (currentDinner.isReady) currentDinner else cook(fuel)

    fun invalidateDinner() {
        currentDinner = currentDinner.copy(isReady = false)
    }
}

data class Dinner(
    val ingredients: String,
    val isReady: Boolean
){
    companion object{
        val EMPTY = Dinner("", isReady = false)
    }
}

data class Fuel(var amount: Int)

suspend fun cook(fuel: Fuel): Dinner {
    fuel.amount--
    delay(1_000)
    return Dinner(ingredients = Random.nextInt(100).toString(), isReady = true)
}