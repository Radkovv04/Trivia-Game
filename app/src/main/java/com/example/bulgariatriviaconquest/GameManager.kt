package com.example.bulgariatriviaconquest

// 1. Enums define the "States"
enum class Team { NEUTRAL, RED, BLUE }
enum class GamePhase { CAPTURE, ATTACK }

// 2. Data classes define the "Objects"
data class Territory(
    val name: String,
    val hexCode: String,
    var owner: Team = Team.NEUTRAL,
    var isKingdom: Boolean = false,
    val adjacentTerritories: List<String>
)

// 3. The Object is the "Brain"
object GameManager {
    var redLastResult: Boolean = false
    var blueLastResult: Boolean = false
    var currentTurn = Team.RED
    var currentPhase = GamePhase.CAPTURE

    var redTargetRegion: String? = null
    var blueTargetRegion: String? = null

    var redPoints = 0
    var bluePoints = 0

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

    // Define possible move results
    enum class MoveResult {
        VALID,
        ALREADY_OWNED,
        NOT_ADJACENT,
        ENEMY_TERRITORY_IN_CAPTURE,
        ALREADY_TARGETED // Fixes the "Blue stealing Red's pick" issue
    }

    fun checkMove(regionName: String, team: Team): MoveResult {
        val territory = territories[regionName] ?: return MoveResult.NOT_ADJACENT

        // 1. Can't target what you already own
        if (territory.owner == team) return MoveResult.ALREADY_OWNED

        // 2. Fix the "Capture Phase" message: Can't pick enemy land yet!
        if (currentPhase == GamePhase.CAPTURE && territory.owner != Team.NEUTRAL) {
            return MoveResult.ENEMY_TERRITORY_IN_CAPTURE
        }

        // 3. FIX: Check if the OTHER team already put a flag here this round
        if (team == Team.BLUE && regionName == redTargetRegion) return MoveResult.ALREADY_TARGETED
        if (team == Team.RED && regionName == blueTargetRegion) return MoveResult.ALREADY_TARGETED

        // 4. Adjacency check
        val isAdjacent = territory.adjacentTerritories.any { territories[it]?.owner == team }
        if (!isAdjacent) return MoveResult.NOT_ADJACENT

        return MoveResult.VALID
    }

    // Add this to the top of GameManager to track early knockouts
    var instantWinner: Team? = null

    fun resolveRound(redCorrect: Boolean, blueCorrect: Boolean) {
        // 1. Process Red's Attack
        redTargetRegion?.let { name ->
            val territory = territories[name]!!
            val previousOwner = territory.owner

            if (redCorrect) {
                if (previousOwner == Team.NEUTRAL) {
                    redPoints += 100 // Capture
                } else if (previousOwner == Team.BLUE) {
                    redPoints += 300 // Recapture/Steal
                    if (territory.isKingdom) {
                        bluePoints = 0
                        instantWinner = Team.RED // Instant win!
                    }
                }
                territory.owner = Team.RED
            } else {
                // Red failed to answer correctly. Did Blue defend?
                if (previousOwner == Team.BLUE) {
                    bluePoints += 150 // Defense Bonus
                }
            }
        }

        // 2. Process Blue's Attack
        blueTargetRegion?.let { name ->
            val territory = territories[name]!!
            val previousOwner = territory.owner

            // If Red just instantly won, skip Blue's attack
            if (instantWinner == null) {
                if (blueCorrect) {
                    if (previousOwner == Team.NEUTRAL) {
                        bluePoints += 100 // Capture
                    } else if (previousOwner == Team.RED) {
                        bluePoints += 300 // Recapture/Steal
                        if (territory.isKingdom) {
                            redPoints = 0
                            instantWinner = Team.BLUE // Instant win!
                        }
                    }
                    territory.owner = Team.BLUE
                } else {
                    // Blue failed to answer correctly. Did Red defend?
                    if (previousOwner == Team.RED) {
                        redPoints += 150 // Defense Bonus
                    }
                }
            }
        }

        // 3. Phase Switch Check
        val neutralLandLeft = territories.values.any { it.owner == Team.NEUTRAL }
        if (!neutralLandLeft) {
            currentPhase = GamePhase.ATTACK
        }
    }

    fun startNewGame() {
        // Reset all territories to Neutral and not kingdoms
        territories.values.forEach {
            it.owner = Team.NEUTRAL
            it.isKingdom = false
        }

        // Setup initial Kingdoms
        territories["София"]?.apply {
            owner = Team.RED
            isKingdom = true
        }
        territories["Бургас"]?.apply {
            owner = Team.BLUE
            isKingdom = true
        }

        currentPhase = GamePhase.CAPTURE
        currentTurn = Team.RED
        redPoints = 0
        bluePoints = 0
        instantWinner = null // Reset knockout status
        QuestionBank.resetQuestions()
    }
}