package com.example.magnusing.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.magnusing.ui.game.logic.GameViewModel
import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.game.model.PieceType
import com.example.magnusing.ui.theme.MagnusingTheme
import pieceToUnicode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    vm: GameViewModel = viewModel()
) {
    val state = vm.uiState.collectAsState().value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Game") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                onSquareClick = { vm.onSquareTapped(it) }
            )

            if (state.pendingPromotion != null) {
                PromotionDialog(
                    color = state.sideToMove,
                    onPick = { vm.onPromotionChosen(it) }
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
        onDismissRequest = { /* prevent dismiss */ },
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
                        Text(
                            text = pieceToUnicode(Piece(type, color)),
                            style = MaterialTheme.typography.headlineMedium
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
