package com.example.magnusing.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.magnusing.R
import com.example.magnusing.ui.game.engine.StockfishEngine
import com.example.magnusing.ui.game.logic.GameViewModel
import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.game.model.PieceType
import com.example.magnusing.ui.newgame.Category
import com.example.magnusing.ui.newgame.Opponent
import com.example.magnusing.ui.theme.MagnusingTheme
import pieceToDrawableRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    opponent: Opponent,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    playerColor: PieceColor = PieceColor.White,
    vm: GameViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val engine = remember(context) { StockfishEngine(context) }

    LaunchedEffect(engine) {
        vm.initEngine(engine)
    }

    LaunchedEffect(playerColor) {
        vm.configureUiState(playerChoice = playerColor)
    }

    DisposableEffect(Unit) {
        onDispose { vm.stopEngine() }
    }

    var showQuitDialog by remember { mutableStateOf(false) }

    // Intercept system back
    BackHandler(enabled = true) {
        showQuitDialog = true
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Game") },
                navigationIcon = {
                    IconButton(onClick = { showQuitDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OpponentHeader(opponent)

            Spacer(Modifier.height(12.dp))

            ChessBoard(
                board = state.board,
                selectedSquare = state.selectedSquare,
                legalTargets = state.targetMoves.keys,
                onSquareClick = vm::onSquareTapped,
                perspective = state.playerColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            if (state.pendingPromotionMove != null) {
                PromotionDialog(
                    color = state.sideToMove,
                    onPick = vm::onPromotionChosen
                )
            }

            if (showQuitDialog) {
                AlertDialog(
                    onDismissRequest = { showQuitDialog = false },
                    title = { Text("Quit game?") },
                    text = {
                        Text("Are you sure? This game will count as a loss if you quit.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showQuitDialog = false
                                onBackClick()
                            }
                        ) { Text("Quit") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showQuitDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun OpponentHeader(opponent: Opponent) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = opponent.avatarRes),
                contentDescription = opponent.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = opponent.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${opponent.elo} ELO",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = opponent.category.name.uppercase(),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun PromotionDialog(
    color: PieceColor,
    onPick: (PieceType) -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* block dismiss */ },
        title = { Text("Promote to") },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val options = listOf(
                    PieceType.Queen,
                    PieceType.Rook,
                    PieceType.Bishop,
                    PieceType.Knight
                )

                options.forEach { type ->
                    TextButton(onClick = { onPick(type) }) {
                        Image(
                            painter = painterResource(
                                id = pieceToDrawableRes(Piece(type, color))
                            ),
                            contentDescription = "Promote to ${type.name}",
                            modifier = Modifier.size(44.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    MagnusingTheme {
        GameScreen(
            opponent = Opponent(
                id = "preview",
                name = "Magnus Bot",
                elo = 2800,
                category = Category.Hard,
                avatarRes = R.drawable.elon_musk
            ),
            onBackClick = {}
        )
    }
}
