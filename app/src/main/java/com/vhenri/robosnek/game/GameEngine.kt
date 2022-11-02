package com.vhenri.robosnek.game

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
                Pair(6,0)
            ),
                snekNumber = 0,
                currentDirection = SnekDirection.RIGHT,
                snekBodyColor = SnekPink,
                snekHeadColor = SnekPinkHead,
                score = 0
            ),
            Snek(snekBody = listOf(
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

    private fun nextSnekCoord(head: Pair<Int, Int>, direction: SnekDirection): Pair<Int, Int> {
        return when (direction){
            SnekDirection.UP -> Pair(head.first, head.second - 1) // Move up - (0, -1)
            SnekDirection.DOWN -> Pair(head.first, head.second + 1) // Move Down - (0, 1)
            SnekDirection.RIGHT -> Pair(head.first + 1, head.second) // Move right - (1, 0)
            SnekDirection.LEFT -> Pair(head.first - 1, head.second) // Move Left - (-1, 0)
        }
    }

    private fun calculateOptimalMovement(head: Pair<Int, Int>): SnekDirection? {
        val food = gameState.value.snekFood
        if (head.first > food.first && isValidDirection(nextSnekCoord(head, SnekDirection.LEFT))) return SnekDirection.LEFT
        if (head.first < food.first && isValidDirection(nextSnekCoord(head, SnekDirection.RIGHT))) return SnekDirection.RIGHT
        if (head.second > food.second && isValidDirection(nextSnekCoord(head, SnekDirection.UP))) return SnekDirection.UP
        if (head.second < food.second && isValidDirection(nextSnekCoord(head, SnekDirection.DOWN))) return SnekDirection.DOWN
        return null
    }

    private fun moveSnek(snekList: List<Snek>, currentSnekTurn: Int): List<Snek>{
        val currentSnek = snekList[currentSnekTurn]
        val optimalDirection = calculateOptimalMovement(currentSnek.snekBody.first())
        val newSnekList = snekList.toMutableList()

        val newSnek = if (optimalDirection != null){
            val newSnekBody = currentSnek.snekBody.toMutableList()
            val newHead = nextSnekCoord(currentSnek.snekBody.first(), optimalDirection)
            newSnekBody.add(0, newHead)
            currentSnek.copy(
                snekBody = newSnekBody,
                currentDirection = optimalDirection
            )
        } else {
            currentSnek.copy(
                currentDirection = null
            )
        }
        newSnekList[currentSnekTurn] = newSnek
        return newSnekList

    }

    init {
        scope.launch {
            while (true) {
                delay(500)
                val currentSnekTurn = gameState.value.currentSnekTurn
                val newSnekList = moveSnek(gameState.value.snekList, currentSnekTurn)
                 _gameState.update {
                    it.copy(
                        currentSnekTurn = updateSnekTurn(currentSnekTurn),
                        snekList = newSnekList
                    )
                }
            }
        }
    }

    companion object {
        const val BOARD_SIZE = 7
        const val NUM_SNEK_PLAYERS = 2
    }
}