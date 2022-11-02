package com.vhenri.robosnek.game

import android.util.Log
import com.vhenri.robosnek.models.GameState
import com.vhenri.robosnek.models.SnekDirection
import com.vhenri.robosnek.models.Snek
import com.vhenri.robosnek.ui.theme.SnekGreen
import com.vhenri.robosnek.ui.theme.SnekGreenHead
import com.vhenri.robosnek.ui.theme.SnekPink
import com.vhenri.robosnek.ui.theme.SnekPinkHead
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GameEngine(
    private val scope: CoroutineScope,
    private val numSneks: Int = NUM_SNEK_PLAYERS,
) {
    private val mutex = Mutex()
    private val _gameState: MutableStateFlow<GameState> =
        MutableStateFlow(initGameState())
    val gameState = _gameState.asStateFlow()

    var move = Pair(1, 0)
        set(value) {
            scope.launch {
                mutex.withLock {
                    field = value
                }
            }
        }

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
                snekNumber = 0,
                currentDirection = SnekDirection.RIGHT,
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
                snekNumber = 1,
                currentDirection = SnekDirection.RIGHT,
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

    fun calculateValidDirections(head: Pair<Int, Int>): List<SnekDirection?>{
        val validDirections = mutableListOf<SnekDirection>()

        if (isValidDirection(moveSnek(head,SnekDirection.UP))) validDirections.add(SnekDirection.UP)
        if (isValidDirection(moveSnek(head, SnekDirection.DOWN))) validDirections.add(SnekDirection.DOWN)
        if (isValidDirection(moveSnek(head, SnekDirection.LEFT))) validDirections.add(SnekDirection.LEFT)
        if (isValidDirection(moveSnek(head, SnekDirection.RIGHT))) validDirections.add(SnekDirection.RIGHT)

        return validDirections
    }

    fun isValidDirection(coord: Pair<Int, Int>): Boolean {
        // check top & bottom boundaries
        if (coord.first < 0 || coord.first > BOARD_SIZE -1) return false
        // check left & right boundaries
        if (coord.second <0 || coord.second > BOARD_SIZE -1) return false
        // check for body
        for (snek in gameState.value.snekList){
            if (snek.snekBody.contains(coord)) return false
        }
        return true
    }

    private fun updateSnekTurn(currentTurn: Int): Int {
        return if (currentTurn + 1 > numSneks -1 ){
            0
        } else {
            currentTurn + 1
        }
    }

    private fun moveSnek(head: Pair<Int, Int>, direction: SnekDirection): Pair<Int, Int> {
        return when (direction){
            SnekDirection.UP -> Pair(head.first, head.second - 1) // Move up - (0, -1)
            SnekDirection.DOWN -> Pair(head.first, head.second + 1) // Move Down - (0, 1)
            SnekDirection.RIGHT -> Pair(head.first + 1, head.second) // Move right - (1, 0)
            SnekDirection.LEFT -> Pair(head.first - 1, head.second) // Move Left - (-1, 0)
        }
    }

    init {
        scope.launch {
            while (true) {
                delay(500)
                val currentSnekTurn = gameState.value.currentSnekTurn
                val currentSnek = gameState.value.snekList[currentSnekTurn]
                val validDirections = calculateValidDirections(currentSnek.snekBody.first())
                Log.d("###", "Current Snek: ${currentSnek.snekNumber} - ${validDirections}")
                _gameState.update {
                    it.copy(
                        currentSnekTurn = updateSnekTurn(currentSnekTurn)
                    )
                }
//                _gameState.update {
//                    TODO()
//                }
            }
        }
    }

    companion object {
        const val BOARD_SIZE = 7
        const val NUM_SNEK_PLAYERS = 2
    }
}