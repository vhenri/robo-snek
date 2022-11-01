package com.vhenri.robosnek.game

import com.vhenri.robosnek.models.GameState
import com.vhenri.robosnek.models.SnakeDirection
import com.vhenri.robosnek.models.Snek
import com.vhenri.robosnek.ui.theme.SnekGreen
import com.vhenri.robosnek.ui.theme.SnekGreenHead
import com.vhenri.robosnek.ui.theme.SnekPink
import com.vhenri.robosnek.ui.theme.SnekPinkHead
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameEngine(
    private val scope: CoroutineScope,
    private val numSneks: Int = NUM_SNEK_PLAYERS,
) {
    private val _gameState: MutableStateFlow<GameState> =
        MutableStateFlow(initGameState())
    val gameState = _gameState.asStateFlow()

    private fun initGameState(): GameState {
        val snekFoodLocation = initSnekFood()
        val snekList = initSneks(numSneks, snekFoodLocation)
        return GameState(
            snekFood = snekFoodLocation,
            snekList = snekList,
            currentSnekTurn = 0
        )
    }

    private fun initSnekFood(): Pair<Int, Int> {
        // TODO - randomly choose a location for the snek food to start.
        return Pair(2,1)
    }

    private fun initSneks(numSneks: Int, foodLocation: Pair<Int, Int>): List<Snek> {
        // TODO - loop through the number of sneks required
        // - randomly choose start location (cannot be snek food?)
        // - set snek direction
        // - all sneks start with a score of 0
        return listOf(
            Snek(snekBody = listOf(
                Pair(4, 2),
                Pair(5,2),
                Pair(6,2),
                Pair(6,1),
                Pair(6,0)
            ),
                currentDirection = SnakeDirection.Right,
                snekBodyColor = SnekPink,
                snekHeadColor = SnekPinkHead,
                score = 0
            ),
            Snek(snekBody = listOf(
                Pair(1, 3),
                Pair(1, 4),
                Pair(1, 5),
                Pair(1, 6),
                Pair(0, 6)
            ),
                currentDirection = SnakeDirection.Right,
                snekBodyColor = SnekGreen,
                snekHeadColor = SnekGreenHead,
                score = 0
            ),
        )
    }

    fun onFoodEaten() {
        // TODO - increment snek score
        // - reset snek body & current direction
    }

    fun calculateValidDirections(): List<SnakeDirection?>{
        // TODO
        // 1. check for boundaries with game board
        // 2. check for snek segments (other or own)
        // return list of valid directions or empty list if none available
        return emptyList()
    }

    init {
        scope.launch {
            while (true) {
                delay(500)
                val currentSnekTurn = gameState

//                _gameState.update {
//                    TODO()
//                }
            }
        }
    }

    companion object {
        const val BOARD_SIZE = 7
        const val NUM_SNEK_PLAYERS = 1
    }
}