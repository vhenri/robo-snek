package com.vhenri.robosnek

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.vhenri.robosnek.game.GameEngine
import com.vhenri.robosnek.models.GameState
import com.vhenri.robosnek.models.Snek
import com.vhenri.robosnek.ui.theme.BaseActivity

import com.vhenri.robosnek.ui.theme.DarkPurple
import com.vhenri.robosnek.ui.theme.SnekFoodColor
import kotlinx.coroutines.CoroutineScope

class MainActivity : BaseActivity() {
    private lateinit var scope: CoroutineScope
    private var gameEngine = GameEngine(
        scope = lifecycleScope,
    )

    @Composable
    override fun Content() {
        scope = rememberCoroutineScope()
        Column {
            GameScreen(gameEngine)
        }
    }

    @Composable
    fun GameScreen(gameEngine: GameEngine) {
        val state = gameEngine.gameState.collectAsState(initial = null)
        Column(
            modifier = Modifier.padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            state.value?.let { gameState ->
                Board(gameState)
                BoxWithConstraints(Modifier.padding(16.dp)) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(gameState.snekList) {
                            GameStats(it)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Board(state: GameState) {
        BoxWithConstraints(Modifier.padding(16.dp)) {
            val tileSize = maxWidth / GameEngine.BOARD_SIZE
            Box(
                Modifier
                    .size(maxWidth)
                    .border(2.dp, DarkPurple)
            )
            Box(
                Modifier
                    .offset(
                        x = tileSize * state.snekFood.first,
                        y = tileSize * state.snekFood.second
                    )
                    .size(tileSize)
                    .border(1.dp, Color.Black)
                    .background(
                        SnekFoodColor, RoundedCornerShape(4.dp)
                    )
            )
            state.snekList.forEach { snek ->
                val snekHead = snek.snekBody.first()
                snek.snekBody.forEach {
                    Box(
                        modifier = Modifier
                            .offset(x = tileSize * it.first, y = tileSize * it.second)
                            .size(tileSize)
                            .background(
                                snek.snekBodyColor, CircleShape
                            )
                    )
                }
                Box(
                    modifier = Modifier
                        .offset(x = tileSize * snekHead.first, y = tileSize * snekHead.second)
                        .size(tileSize)
                        .background(
                            snek.snekHeadColor, CircleShape
                        )
                )
            }
        }
    }

    @Composable
    fun GameStats(snek: Snek){
        Row {
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .padding(2.dp)
                    .background(
                        snek.snekHeadColor, CircleShape
                    )
            )
            BodyLarge(text = "Score: ${snek.score}")
        }
    }

    @Composable
    fun BodyLarge(modifier: Modifier = Modifier, text: String, textAlign: TextAlign = TextAlign.Start) {
        Text(
            modifier = modifier,
            text = text,
            color = Color.Black,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = textAlign
        )
    }
}