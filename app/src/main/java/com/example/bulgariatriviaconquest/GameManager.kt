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
    var lives: Int = 1, // Normal territories have 1 life, Kingdoms will have 2
    val adjacentTerritories: List<String>
)

// 3. The Object is the "Brain"
object GameManager {
    var currentRound = 1
    var attackTurnsCompleted = 0
    var redLastResult: Boolean = false
    var blueLastResult: Boolean = false
    var currentTurn = Team.RED
    var currentPhase = GamePhase.CAPTURE

    var redTargetRegion: String? = null
    var blueTargetRegion: String? = null

    var redPoints = 0
    var bluePoints = 0
    var currentAttackRound = 1
    val MAX_ATTACK_ROUNDS = 6

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
        INVALID,
        ALREADY_OWNED,
        ALREADY_TARGETED,
        NOT_ADJACENT,
        ENEMY_TERRITORY_IN_CAPTURE // <-- Add this!
    }

    fun checkMove(regionName: String, player: Team): MoveResult {
        val territory = territories[regionName] ?: return MoveResult.INVALID

        // 1. You can NEVER attack your own territory
        if (territory.owner == player) return MoveResult.ALREADY_OWNED

        // 2. Someone already clicked it this turn
        if (redTargetRegion == regionName || blueTargetRegion == regionName) return MoveResult.ALREADY_TARGETED

        // 3. Phase-Specific Rules
        if (currentPhase == GamePhase.CAPTURE) {
            // In Capture Phase, you can ONLY click Neutral land
            if (territory.owner != Team.NEUTRAL) return MoveResult.ENEMY_TERRITORY_IN_CAPTURE
        } else {
            // In Attack Phase, you can ONLY click Enemy land
            if (territory.owner == Team.NEUTRAL) return MoveResult.INVALID
        }

        // 4. Adjacency Check (You must attack from your borders!)
        val playerOwnsAny = territories.values.any { it.owner == player }
        if (playerOwnsAny) {
            val isAdjacent = territory.adjacentTerritories.any { territories[it]?.owner == player }
            if (!isAdjacent) return MoveResult.NOT_ADJACENT
        }

        return MoveResult.VALID
    }

    // Add this to the top of GameManager to track early knockouts
    var instantWinner: Team? = null
    fun resolveDuel(winner: Team, targetRegion: String) {
        val territory = territories[targetRegion]!!
        val previousOwner = territory.owner

        if (previousOwner == Team.NEUTRAL) {
            // Deadlock situation (1 neutral left)
            territory.owner = winner
            if (winner == Team.RED) redPoints += 100 else bluePoints += 100
        } else {
            // Standard Attack Phase situation
            if (winner == previousOwner) {
                // Defender won! They keep it and get points.
                if (winner == Team.RED) redPoints += 150 else bluePoints += 150
            } else {
                // Attacker won! They steal it. (Kingdom logic applies here)
                if (territory.isKingdom) {
                    territory.lives -= 1
                    if (territory.lives <= 0) {
                        instantWinner = winner
                        if (winner == Team.RED) redPoints += 300 else bluePoints += 300
                    } else {
                        // Damaged Kingdom
                        if (winner == Team.RED) redPoints += 150 else bluePoints += 150
                    }
                } else {
                    // Stole a normal territory
                    territory.owner = winner
                    if (winner == Team.RED) redPoints += 300 else bluePoints += 300
                }
            }
        }
    }
    fun resolveRound(redCorrect: Boolean, blueCorrect: Boolean) {
        // --- 1. PROCESS RED'S ATTACK ---
        redTargetRegion?.let { name ->
            val territory = territories[name]!!
            if (redCorrect) {
                if (territory.owner == Team.NEUTRAL) {
                    // Capture neutral land
                    territory.owner = Team.RED
                    redPoints += 100
                } else if (territory.owner == Team.BLUE) {
                    // Attacking Blue territory (Stealing)
                    if (territory.isKingdom) {
                        territory.lives -= 1
                        if (territory.lives <= 0) {
                            territory.owner = Team.RED
                            redPoints += 300
                            bluePoints = 0 // Knockout penalty
                            instantWinner = Team.RED
                        } else {
                            redPoints += 150 // Hit the castle
                        }
                    } else {
                        territory.owner = Team.RED
                        redPoints += 300
                    }
                }
            } else {
                // Red failed. Blue gets defense points if they were the owner
                if (territory.owner == Team.BLUE) bluePoints += 150
            }
        }

        // --- 2. PROCESS BLUE'S ATTACK ---
        // (Only process if Red didn't just end the game by destroying a Kingdom)
        blueTargetRegion?.let { name ->
            val territory = territories[name]!!
            if (blueCorrect && instantWinner == null) {
                if (territory.owner == Team.NEUTRAL) {
                    territory.owner = Team.BLUE
                    bluePoints += 100
                } else if (territory.owner == Team.RED) {
                    // Attacking Red territory (Stealing)
                    if (territory.isKingdom) {
                        territory.lives -= 1
                        if (territory.lives <= 0) {
                            territory.owner = Team.BLUE
                            bluePoints += 300
                            redPoints = 0 // Knockout penalty
                            instantWinner = Team.BLUE
                        } else {
                            bluePoints += 150 // Hit the castle
                        }
                    } else {
                        territory.owner = Team.BLUE
                        bluePoints += 300
                    }
                }
            } else if (!blueCorrect && instantWinner == null) {
                // Blue failed. Red gets defense points if they were the owner
                if (territory.owner == Team.RED) redPoints += 150
            }
        }

        // --- 3. PHASE CHECK ---
        // Check if map is full to switch to Attack Phase
        val neutralExists = territories.values.any { it.owner == Team.NEUTRAL }
        if (!neutralExists) {
            currentPhase = GamePhase.ATTACK
        }
    }

    fun startNewGame() {
        territories.values.forEach {
            it.owner = Team.NEUTRAL
            it.isKingdom = false
            it.lives = 1 // Reset lives
        }

        // Setup initial Kingdoms with 2 LIVES
        territories["София"]?.apply {
            owner = Team.RED
            isKingdom = true
            lives = 2
        }
        territories["Бургас"]?.apply {
            owner = Team.BLUE
            isKingdom = true
            lives = 2
        }

        currentPhase = GamePhase.CAPTURE
        currentTurn = Team.RED
        redPoints = 0
        bluePoints = 0
        currentAttackRound = 1
        instantWinner = null
        QuestionBank.resetQuestions()
    }
}