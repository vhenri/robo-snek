package com.vhenri.robosnek.models

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class GameState(
    val snekFood: Pair<Int, Int>,
    val snekList: List<Snek>,
    val currentSnekTurn: Int
)

data class Snek(
    val id: String = generateId(),
    val snekBody: List<Pair<Int, Int>>,
    val currentDirection: Int,
    val snekBodyColor: Color,
    val snekHeadColor: Color,
    val score: Int
){
    companion object {
        private const val prefix = "RoboSnek"

        private fun generateId(): String {
            return prefix + UUID.randomUUID().toString()
        }
    }
}
