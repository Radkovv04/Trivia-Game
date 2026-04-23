package com.example.bulgariatriviaconquest // Make sure this matches your package name!

// This keeps track of who owns a region
enum class Team {
    NEUTRAL, RED, BLUE
}

// This represents a single territory on your map
data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val isNumerical: Boolean = false // For the tie-breaker phase later
)
data class Territory(
    val name: String,
    val hexCode: String,
    var owner: Team = Team.NEUTRAL,
    var isKingdom: Boolean = false,
    val adjacentTerritories: List<String> // Names of territories touching this one
)

// This is the "Brain" of the game
object GameManager {
    var currentPhase = "CAPTURE" // Will change to "ATTACK" later
    var currentTurn = Team.RED
    var redPoints = 0
    var bluePoints = 0

    // The Map setup! We must tell the computer exactly what territories touch each other.
    val territories = mapOf(
        "Видин" to Territory("Видин", "#FAF7E6", adjacentTerritories = listOf("Плевен", "София")),
        "Плевен" to Territory("Плевен", "#F9F6E3", adjacentTerritories = listOf("Видин", "София", "Ловеч", "Русе", "Стара Загора")),
        "Русе" to Territory("Русе", "#FAF7E8", adjacentTerritories = listOf("Плевен", "Бургас", "Стара Загора", "Варна")),
        "Варна" to Territory("Варна", "#FAF6EB", adjacentTerritories = listOf("Русе", "Бургас")),

        "София" to Territory("София", "#FBF6E2", adjacentTerritories = listOf("Видин", "Плевен", "Ловеч", "Пловдив", "Благоевград")),
        "Ловеч" to Territory("Ловеч", "#FCF6E0", adjacentTerritories = listOf("Плевен", "Стара Загора", "Пловдив", "София")),
        "Стара Загора" to Territory("Стара Загора", "#FCF7E4", adjacentTerritories = listOf("Ловеч", "Русе", "Плевен", "Бургас", "Хасково", "Пловдив")),
        "Бургас" to Territory("Бургас", "#FAF6ED", adjacentTerritories = listOf("Варна", "Стара Загора", "Хасково", "Русе")),

        "Благоевград" to Territory("Благоевград", "#FBF6E3", adjacentTerritories = listOf("София", "Пловдив")),
        "Пловдив" to Territory("Пловдив", "#F9F6E5", adjacentTerritories = listOf("София", "Ловеч", "Стара Загора", "Хасково", "Смолян", "Благоевград")),
        "Хасково" to Territory("Хасково", "#FAF6EA", adjacentTerritories = listOf("Пловдив", "Стара Загора", "Бургас", "Смолян")),
        "Смолян" to Territory("Смолян", "#F9F6E7", adjacentTerritories = listOf("Пловдив", "Хасково"))
    )

    // Run this when the game first starts!
    fun startNewGame() {
        // Reset everything
        territories.values.forEach {
            it.owner = Team.NEUTRAL
            it.isKingdom = false
        }

        // Set Kingdoms based on your rules!
        territories["София"]?.apply {
            owner = Team.RED
            isKingdom = true
        }
        territories["Бургас"]?.apply {
            owner = Team.BLUE
            isKingdom = true
        }

        currentPhase = "CAPTURE"
        currentTurn = Team.RED // Red team (Sofia) starts first
        redPoints = 0
        bluePoints = 0
    }
}