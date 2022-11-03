package com.vhenri.robosnek.game

import com.vhenri.robosnek.models.GameState
import com.vhenri.robosnek.models.SnekDirection
import com.vhenri.robosnek.models.Snek
import com.vhenri.robosnek.ui.theme.SnekBlue
import com.vhenri.robosnek.ui.theme.SnekBlueHead
import com.vhenri.robosnek.ui.theme.SnekGreen
import com.vhenri.robosnek.ui.theme.SnekGreenHead
import com.vhenri.robosnek.ui.theme.SnekOrange
import com.vhenri.robosnek.ui.theme.SnekOrangeHead
import com.vhenri.robosnek.ui.theme.SnekPink
import com.vhenri.robosnek.ui.theme.SnekPinkHead
import java.lang.Math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameEngine(
    private val scope: CoroutineScope,
    private val numSneks: Int = NUM_SNEK_PLAYERS,
    private val boardSize: Int,
    private val moveFoodDuring: Boolean = MOVE_FOOD_DURING_PLAY
) {

    companion object {
        const val BOARD_SIZE = 7
        const val NUM_SNEK_PLAYERS = 4 // max 4
        const val MOVE_FOOD_DURING_PLAY = false
    }

    private val _gameState: MutableStateFlow<GameState> =
        MutableStateFlow(initGameState())
    val gameState = _gameState.asStateFlow()

    private fun initGameState(): GameState {
        val snekFoodLocation = initSnekFood()
        val snekList = initSneks(numSneks, snekFoodLocation)
        return GameState(
            snekFood = snekFoodLocation,
            snekList = snekList,
            currentSnekTurn = rand(numSneks),
            stuckSneks = 0,
            stalemateRounds = 0
        )
    }

    private fun randomCoord(exclude: List<Pair<Int, Int>>? = null): Pair<Int,Int>{
        val coord = Pair(
            rand(boardSize),
            rand(boardSize)
        )
        return if ( exclude!= null && exclude.contains(coord) && exclude.size != (boardSize*boardSize)){
            randomCoord(exclude)
        } else {
            coord
        }
    }

    private fun initSnekFood(): Pair<Int, Int> {
        return randomCoord()
    }

    private fun initSneks(numSneks: Int, foodLocation: Pair<Int, Int>): List<Snek> {
        return listOf(
            Snek(snekBody = listOf(
                randomCoord(listOf(foodLocation))
            ),
                snekNumber = 0,
                currentDirection = SnekDirection.RIGHT,
                snekBodyColor = SnekPink,
                snekHeadColor = SnekPinkHead,
                score = 0
            ),
            Snek(snekBody = listOf(
                randomCoord(listOf(foodLocation))
            ),
                snekNumber = 1,
                currentDirection = SnekDirection.RIGHT,
                snekBodyColor = SnekGreen,
                snekHeadColor = SnekGreenHead,
                score = 0
            ),
            Snek(snekBody = listOf(
                randomCoord(listOf(foodLocation))
            ),
                snekNumber = 2,
                currentDirection = SnekDirection.RIGHT,
                snekBodyColor = SnekBlue,
                snekHeadColor = SnekBlueHead,
                score = 0
            ),
            Snek(snekBody = listOf(
                randomCoord(listOf(foodLocation))
            ),
                snekNumber = 3,
                currentDirection = SnekDirection.RIGHT,
                snekBodyColor = SnekOrange,
                snekHeadColor = SnekOrangeHead,
                score = 0
            ),
        ).take(numSneks)
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

    private fun tryMovingHorizontal(validDirections: List<SnekDirection>, head: Int, food: Int): SnekDirection? {
        return if (head > food && validDirections.contains(SnekDirection.LEFT)) SnekDirection.LEFT
        else if (validDirections.contains(SnekDirection.RIGHT)) SnekDirection.RIGHT
        else null
    }

    private fun tryMovingVertical(validDirections: List<SnekDirection>, head: Int, food: Int): SnekDirection? {
        return if (head > food && validDirections.contains(SnekDirection.UP)) SnekDirection.UP
        else if (validDirections.contains(SnekDirection.DOWN)) SnekDirection.DOWN
        else null
    }

    fun calculateValidDirections(head: Pair<Int, Int>): List<SnekDirection?>{
        val validDirections = mutableListOf<SnekDirection>()

        if (isValidDirection(nextSnekCoord(head,SnekDirection.UP))) validDirections.add(SnekDirection.UP)
        if (isValidDirection(nextSnekCoord(head, SnekDirection.DOWN))) validDirections.add(SnekDirection.DOWN)
        if (isValidDirection(nextSnekCoord(head, SnekDirection.LEFT))) validDirections.add(SnekDirection.LEFT)
        if (isValidDirection(nextSnekCoord(head, SnekDirection.RIGHT))) validDirections.add(SnekDirection.RIGHT)

        return validDirections
    }

    private fun isValidDirection(coord: Pair<Int, Int>): Boolean {
        // check top & bottom boundaries
        if (coord.first < 0 || coord.first > boardSize -1) return false
        // check left & right boundaries
        if (coord.second <0 || coord.second > boardSize -1) return false
        // check for body
        for (snek in gameState.value.snekList){
            if (snek.snekBody.contains(coord)) return false
        }
        return true
    }

    private fun calculateOptimalMovementBasic(head: Pair<Int, Int>): SnekDirection? {
        // check if stuck
        var validDirections = calculateValidDirections(head)
        if (validDirections.isEmpty()) return null
        else if (validDirections.size == 1) return validDirections.first()
        else validDirections = validDirections as List<SnekDirection>

        val food = gameState.value.snekFood
        // you're in the correct x plane! navigate up or down towards food
        if (head.first == food.first){
            val direction = tryMovingVertical(validDirections, head.second, food.second)
            if (direction != null) return direction
        }
        // you're in the correct y plane! navigate left or right towards food
        if (head.second == food.second){
            val direction = tryMovingHorizontal(validDirections, head.first, food.first)
            if (direction != null) return direction
        }

        // you need to move towards the correct x & y

        // try to move optimally
        val xDistanceToFood = head.first - food.first
        val yDistanceToFood = head.second - food.second

        if (abs(xDistanceToFood) > abs(yDistanceToFood)) {
            // try moving left or right first
            val direction = tryMovingHorizontal(validDirections, head.first, food.first)
            if (direction != null) return direction
        } else if (abs(xDistanceToFood) < abs(yDistanceToFood)){
            // try moving up or down
            val direction = tryMovingVertical(validDirections, head.second, food.second)
            if (direction != null) return direction
        }

        // just try to move!
        when(rand(2)){
            0 -> {
                // try horizontal, then vertical
                var direction = tryMovingHorizontal(validDirections, head.first, food.first)
                if (direction != null) return direction
                direction = tryMovingVertical(validDirections, head.second, food.second)
                if (direction != null) return direction
            }
            1 -> {
                // try vertical, then horizontal
                var direction = tryMovingVertical(validDirections, head.second, food.second)
                if (direction != null) return direction
                direction = tryMovingHorizontal(validDirections, head.first, food.first)
                if (direction != null) return direction
            }
        }

        // pick one at random lol
        val selectRandom = rand(validDirections.size)
        return validDirections[selectRandom]
    }

    private fun moveSnek(snekList: List<Snek>, currentSnekTurn: Int, foodCoord: Pair<Int, Int>): Pair<List<Snek>, Pair<Int,Int>?>{
        val currentSnek = snekList[currentSnekTurn]
        val moveDirection = calculateOptimalMovementBasic(currentSnek.snekBody.first())
        val newSnekList = snekList.toMutableList()

        val newSnek = if (moveDirection != null){
            val newSnekBody = currentSnek.snekBody.toMutableList()
            val newHead = nextSnekCoord(currentSnek.snekBody.first(), moveDirection)
            newSnekBody.add(0, newHead)
            currentSnek.copy(
                snekBody = newSnekBody,
                currentDirection = moveDirection
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
            snek.snekBody = listOf(randomCoord(listOf(newFoodCoord)))
            snek.currentDirection = SnekDirection.RIGHT
        }

        return Pair(snekList, newFoodCoord)
    }

    private fun moveFoodDuringPlay(currentFoodLocation: Pair<Int, Int>, snekList: List<Snek> ): Pair<Int, Int> {
        val allOccupiedLocations: MutableList<Pair<Int, Int>> = mutableListOf(currentFoodLocation)
        return if (allOccupiedLocations.size != (boardSize*boardSize)){
            for (snek in snekList){
                allOccupiedLocations+=snek.snekBody
            }
            randomCoord(allOccupiedLocations)
        } else {
            currentFoodLocation
        }

    }

    init {
        scope.launch {
            while (true) {
                delay(100)
                val currentSnekTurn = gameState.value.currentSnekTurn
                val currentSnek = gameState.value.snekList[currentSnekTurn]
                val currentFoodCoord = gameState.value.snekFood

                // Check if the snek is stuck
                if (currentSnek.currentDirection != null ){
                    val snekList = gameState.value.snekList
                    val newSnekList = moveSnek(snekList, currentSnekTurn, currentFoodCoord)
                    val nextTurn = if (newSnekList.second != null ) rand(numSneks) else updateSnekTurn(currentSnekTurn)
                    val foodCoord = if (moveFoodDuring) moveFoodDuringPlay(currentFoodCoord, newSnekList.first) else currentFoodCoord
                    val stuckSneks =  if (newSnekList.second != null ) 0 else gameState.value.stuckSneks
                    _gameState.update {
                        it.copy(
                            currentSnekTurn = nextTurn,
                            snekList = newSnekList.first,
                            snekFood = newSnekList.second ?: foodCoord,
                            stuckSneks = stuckSneks
                        )
                    }
                } else {
                    // check if all sneks are stuck
                    if (gameState.value.stuckSneks == gameState.value.snekList.size){
                        val newSnekList = onFoodEaten(gameState.value.snekList.toMutableList(), -1)
                        val stalemateCount = gameState.value.stalemateRounds + 1
                        _gameState.update {
                            it.copy(
                                currentSnekTurn = rand(numSneks),
                                snekList = newSnekList.first,
                                snekFood = newSnekList.second,
                                stuckSneks = 0,
                                stalemateRounds = stalemateCount
                            )
                        }
                    } else {
                        val stuckSneks = gameState.value.stuckSneks + 1
                        _gameState.update {
                            it.copy(
                                currentSnekTurn = updateSnekTurn(currentSnekTurn),
                                stuckSneks = stuckSneks
                            )
                        }
                    }
                }

            }
        }
    }
}