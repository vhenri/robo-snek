package com.vhenri.robosnek.models

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class GameState(
    val snekFood: Pair<Int, Int>,
    val snekList: List<Snek>,
    val currentSnekTurn: Int,
    var stuckSneks: Int,
    var stalemateRounds: Int,
)

data class Snek(
    val id: String = generateId(),
    val snekNumber: Int,
    var snekBody: List<Pair<Int, Int>>,
    var currentDirection: SnekDirection?,
    val snekBodyColor: Color,
    val snekHeadColor: Color,
    var score: Int
){
    companion object {
        private const val prefix = "RoboSnek"

        private fun generateId(): String {
            return prefix + UUID.randomUUID().toString()
        }
    }
}
