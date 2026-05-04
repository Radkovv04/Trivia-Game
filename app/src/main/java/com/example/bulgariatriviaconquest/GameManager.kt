package com.example.bulgariatriviaconquest

enum class Team { NEUTRAL, RED, BLUE }
enum class GamePhase { CAPTURE, ATTACK }
var playerUsername: String = "Играч"
data class Territory (
    val name: String,
    val hexCode: String,
    var owner: Team = Team.NEUTRAL,
    var isKingdom: Boolean = false,
    var lives: Int = 1,
    val adjacentTerritories: List<String>
)

object GameManager {
    var isRedTurnToAnswer: Boolean = true

    var playerUsername: String = "Играч"
    var isAudioEnabled = true
    var isHardMode: Boolean = false
    val MAX_ATTACK_ROUNDS = 6

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
    enum class MoveResult {
        VALID,
        INVALID,
        ALREADY_OWNED,
        ALREADY_TARGETED,
        NOT_ADJACENT,
        ENEMY_TERRITORY_IN_CAPTURE
    }

    fun checkMove(regionName: String, player: Team): MoveResult {
        val territory = territories[regionName] ?: return MoveResult.INVALID
        if (territory.owner == player) {
            return MoveResult.ALREADY_OWNED
        }
        if (redTargetRegion == regionName || blueTargetRegion == regionName) {
            return MoveResult.ALREADY_TARGETED
        }
        if (currentPhase == GamePhase.CAPTURE) {
            if (territory.owner != Team.NEUTRAL) {
                return MoveResult.ENEMY_TERRITORY_IN_CAPTURE
            }
        } else {
            if (territory.owner == Team.NEUTRAL) {
                return MoveResult.INVALID
            }
        }
        val playerOwnsAny = territories.values.any { it.owner == player }
        if (playerOwnsAny) {
            val isAdjacent = territory.adjacentTerritories.any { territories[it]?.owner == player }
            if (!isAdjacent) return MoveResult.NOT_ADJACENT
        }
        return MoveResult.VALID
    }
    var instantWinner: Team? = null
    fun resolveDuel(winner: Team, targetRegion: String) {
        val territory = territories[targetRegion]!!
        val previousOwner = territory.owner
        if (previousOwner == Team.NEUTRAL) {
            territory.owner = winner
            if (winner == Team.RED) redPoints += 100 else bluePoints += 100
        } else {
            if (winner == previousOwner) {
                if (winner == Team.RED) redPoints += 150 else bluePoints += 150
            } else {
                if (territory.isKingdom) {
                    territory.lives -= 1
                    if (territory.lives <= 0) {
                        instantWinner = winner
                        if (winner == Team.RED) redPoints += 300 else bluePoints += 300
                    } else {
                        if (winner == Team.RED) redPoints += 150 else bluePoints += 150
                    }
                } else {
                    territory.owner = winner
                    if (winner == Team.RED) redPoints += 300 else bluePoints += 300
                }
            }
        }
    }
    fun resolveRound(redCorrect: Boolean, blueCorrect: Boolean) {
        redTargetRegion?.let { name ->
            val territory = territories[name]!!
            if (redCorrect) {
                if (territory.owner == Team.NEUTRAL) {
                    territory.owner = Team.RED
                    redPoints += 100
                } else if (territory.owner == Team.BLUE) {
                    if (territory.isKingdom) {
                        territory.lives -= 1
                        if (territory.lives <= 0) {
                            territory.owner = Team.RED
                            redPoints += 300
                            bluePoints = 0
                            instantWinner = Team.RED
                        } else {
                            redPoints += 150
                        }
                    } else {
                        territory.owner = Team.RED
                        redPoints += 300
                    }
                }
            } else {
                if (territory.owner == Team.BLUE) bluePoints += 150
            }
        }
        blueTargetRegion?.let { name ->
            val territory = territories[name]!!
            if (blueCorrect && instantWinner == null) {
                if (territory.owner == Team.NEUTRAL) {
                    territory.owner = Team.BLUE
                    bluePoints += 100
                } else if (territory.owner == Team.RED) {
                    if (territory.isKingdom) {
                        territory.lives -= 1
                        if (territory.lives <= 0) {
                            territory.owner = Team.BLUE
                            bluePoints += 300
                            redPoints = 0
                            instantWinner = Team.BLUE
                        } else {
                            bluePoints += 150
                        }
                    } else {
                        territory.owner = Team.BLUE
                        bluePoints += 300
                    }
                }
            } else if (!blueCorrect && instantWinner == null) {
                if (territory.owner == Team.RED) redPoints += 150
            }
        }
        val neutralExists = territories.values.any { it.owner == Team.NEUTRAL }
        if (!neutralExists) {
            currentPhase = GamePhase.ATTACK
        }
    }

    fun startNewGame() {
        territories.values.forEach {
            it.owner = Team.NEUTRAL
            it.isKingdom = false
            it.lives = 1
        }
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
        QuestionBank.resetNormal()
        QuestionBank.resetHard()
    }
}