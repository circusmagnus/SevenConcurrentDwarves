@file:OptIn(ExperimentalCoroutinesApi::class)

import kotlinx.coroutines.*
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

    kitchen.invalidateDinner()
    val fuelRemaining = kitchen.fuel.amount
    println("\nDinner eaten, fuel remaining: $fuelRemaining")
}

/////////////////////////////////////////////////////////////////

class Kitchen {

    private var currentDinner: Dinner = Dinner.EMPTY
    val fuel: Fuel = Fuel(10)

    suspend fun getDinner(): Dinner =
        if (currentDinner.isReady) currentDinner
        else cook(fuel).also { cooked -> currentDinner = cooked }

    fun invalidateDinner() {
        currentDinner = currentDinner.copy(isReady = false)
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