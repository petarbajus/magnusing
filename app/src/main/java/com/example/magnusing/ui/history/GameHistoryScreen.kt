package com.example.magnusing.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.magnusing.ui.navigation.BottomNavBar
import com.example.magnusing.ui.navigation.BottomNavItem
import com.example.magnusing.ui.navigation.TopBar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GameHistoryScreen(
    onHomeClick: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier,
    vm: GameHistoryViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.load() }

    val state by vm.state.collectAsState()

    Surface(color = Color.Black) {
        androidx.compose.material3.Scaffold(
            topBar = { TopBar(onUserClick = onUserClick) },
            bottomBar = {
                BottomNavBar(
                    selectedItem = BottomNavItem.HISTORY,
                    onHomeClick = onHomeClick,
                    onHistoryClick = { /* already here */ }
                )
            }
        ) { padding ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black)
            ) {
                when (val s = state) {
                    is GameHistoryUiState.Loading -> {
                        Text(
                            text = "LOADING...",
                            color = Color.White,
                            fontFamily = FontFamily.Serif,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is GameHistoryUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "COULDN'T LOAD HISTORY",
                                color = Color.White,
                                fontFamily = FontFamily.Serif,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = s.message,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { vm.load() },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("RETRY", fontFamily = FontFamily.Serif)
                            }
                        }
                    }

                    is GameHistoryUiState.Loaded -> {
                        if (s.items.isEmpty()) {
                            Text(
                                text = "NO GAMES YET",
                                color = Color.White,
                                fontFamily = FontFamily.Serif,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    Text(
                                        text = "GAME HISTORY",
                                        color = Color.White,
                                        fontFamily = FontFamily.Serif,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color.White.copy(alpha = 0.25f))
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }

                                items(s.items, key = { it.id }) { game ->
                                    GameHistoryRow(game)
                                }

                                item { Spacer(Modifier.height(80.dp)) } // breathe above nav bar
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameHistoryRow(game: GameHistoryItem) {
    val bg = Color.White
    val fg = Color.Black

    val deltaText = when {
        game.eloDelta > 0 -> "+${game.eloDelta}"
        else -> game.eloDelta.toString()
    }

    val resultLabel = when (game.result.uppercase(Locale.ROOT)) {
        "WIN" -> "WIN"
        "LOSS" -> "LOSS"
        "DRAW" -> "DRAW"
        else -> game.result.uppercase(Locale.ROOT)
    }

    val dateText = remember(game.endedAt) {
        val ts = game.endedAt?.toDate()
        if (ts == null) "—"
        else SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.getDefault()).format(ts)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = resultLabel,
                color = fg,
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = deltaText,
                color = fg,
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "${game.opponentName}  (${game.opponentElo})",
            color = fg,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = "You: ${game.playerEloBefore} → ${game.playerEloAfter}   •   ${game.playerColor}",
            color = fg.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = dateText,
            color = fg.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
