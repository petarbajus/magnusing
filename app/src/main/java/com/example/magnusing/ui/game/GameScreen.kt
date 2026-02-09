package com.example.magnusing.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.magnusing.ui.game.engine.StockfishEngine
import com.example.magnusing.ui.game.logic.GameViewModel
import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.game.model.PieceType
import com.example.magnusing.ui.theme.MagnusingTheme
import pieceToDrawableRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
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

    // Stop engine when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            vm.stopEngine()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Game") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {

            ChessBoard(
                board = state.board,
                selectedSquare = state.selectedSquare,
                legalTargets = state.targetMoves.keys,
                onSquareClick = vm::onSquareTapped,
                perspective = state.playerColor
            )

            if (state.pendingPromotionMove != null) {
                PromotionDialog(
                    color = state.sideToMove,
                    onPick = vm::onPromotionChosen
                )
            }
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
                            painter = painterResource(id = pieceToDrawableRes(Piece(type, color))),
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
        GameScreen(onBackClick = {})
    }
}
