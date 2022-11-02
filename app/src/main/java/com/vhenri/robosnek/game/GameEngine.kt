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

class GameEngine(
    private val scope: CoroutineScope,
    private val numSneks: Int = NUM_SNEK_PLAYERS,
) {
    private val mutex = Mutex()
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

    private fun randomCoord(exclude: Pair<Int, Int>? = null): Pair<Int,Int>{
        val coord = Pair(
            rand(BOARD_SIZE),
            rand(BOARD_SIZE)
        )
        return if (coord == exclude){
            randomCoord(exclude)
        } else {
            coord
        }
    }

    private fun initSnekFood(): Pair<Int, Int> {
        return randomCoord()
    }

    private fun initSneks(numSneks: Int, foodLocation: Pair<Int, Int>): List<Snek> {
        // TODO - loop through the number of sneks required
        // - all sneks start with a score of 0
        return listOf(
            Snek(snekBody = listOf(
                randomCoord(foodLocation)
            ),
                snekNumber = 0,
                currentDirection = SnekDirection.RIGHT,
                snekBodyColor = SnekPink,
                snekHeadColor = SnekPinkHead,
                score = 0
            ),
            Snek(snekBody = listOf(
                randomCoord(foodLocation)
            ),
                snekNumber = 1,
                currentDirection = SnekDirection.RIGHT,
                snekBodyColor = SnekGreen,
                snekHeadColor = SnekGreenHead,
                score = 0
            ),
        )
    }

    private fun isValidDirection(coord: Pair<Int, Int>): Boolean {
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
//        val xDistanceToFood = head.first - food.first
//        val yDistanceToFood = head.second - food.second
//
//        if (xDistanceToFood > yDistanceToFood){
//            // move either left or right
//        } else {
//            // move either up or down
//        }

        if (head.first > food.first && isValidDirection(nextSnekCoord(head, SnekDirection.LEFT))) return SnekDirection.LEFT
        if (head.first < food.first && isValidDirection(nextSnekCoord(head, SnekDirection.RIGHT))) return SnekDirection.RIGHT
        if (head.second > food.second && isValidDirection(nextSnekCoord(head, SnekDirection.UP))) return SnekDirection.UP
        if (head.second < food.second && isValidDirection(nextSnekCoord(head, SnekDirection.DOWN))) return SnekDirection.DOWN
        return null
    }

    private fun moveSnek(snekList: List<Snek>, currentSnekTurn: Int, foodCoord: Pair<Int, Int>): Pair<List<Snek>, Pair<Int,Int>?>{
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

        return if (checkFoodEaten(newSnekList[currentSnekTurn], foodCoord)){
            onFoodEaten(newSnekList,currentSnekTurn)
        } else {
            Pair(newSnekList, null)
        }

    }

    private fun checkFoodEaten(currentSnek: Snek, foodCoord: Pair<Int,Int>): Boolean {
        return (currentSnek.snekBody.first() == foodCoord)
    }

    private fun onFoodEaten(snekList: MutableList<Snek>, currentSnekTurn: Int): Pair<List<Snek>, Pair<Int, Int>> {
        val newFoodCoord = randomCoord()
        for (snek in snekList){
            if (snek.snekNumber == currentSnekTurn){
                snek.score ++
            }
            snek.snekBody = listOf(randomCoord(newFoodCoord))
            snek.currentDirection = SnekDirection.RIGHT
        }

        return Pair(snekList, newFoodCoord)
    }

    init {
        scope.launch {
            while (true) {
                delay(500)
                val currentSnekTurn = gameState.value.currentSnekTurn
                val currentSnek = gameState.value.snekList[currentSnekTurn]
                val currentFoodCoord = gameState.value.snekFood

                // Check if the snek is stuck
                if (currentSnek.currentDirection != null ){
                    val snekList = gameState.value.snekList
                    val newSnekList = moveSnek(snekList, currentSnekTurn, currentFoodCoord)
                    _gameState.update {
                        it.copy(
                            currentSnekTurn = updateSnekTurn(currentSnekTurn),
                            snekList = newSnekList.first,
                            snekFood = newSnekList.second ?: currentFoodCoord
                        )
                    }
                } else {
                    _gameState.update {
                        it.copy(
                            currentSnekTurn = updateSnekTurn(currentSnekTurn),
                        )
                    }
                }

            }
        }
    }

    companion object {
        const val BOARD_SIZE = 7
        const val NUM_SNEK_PLAYERS = 2
    }
}