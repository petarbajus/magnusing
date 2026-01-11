package com.example.magnusing.ui.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.game.model.PieceType

@Composable
fun ChessBoard(
    board: List<Piece?>,
    selectedSquare: Int?,
    legalTargets: Set<Int>,
    onSquareClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 8 rows x 8 columns
    Column(
        modifier = modifier
            .size(360.dp), // temporary fixed size; we’ll make it responsive later
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        for (row in 0 until 8) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0 until 8) {
                    val isLight = (row + col) % 2 == 0
                    val index = row * 8 + col
                    val isSelected = index == selectedSquare
                    val baseColor =
                        if (isLight) Color(0xFFEEEED2) else Color(0xFF769656)
                    val squareColor =
                        if (isSelected) Color(0xFFFFF176) else baseColor

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(squareColor)
                            .clickable { onSquareClick(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        val piece = board[index]
                        val isTarget = index in legalTargets

                        if (isTarget && piece == null) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x55000000))
                            )
                        }

                        if (isTarget && piece != null) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = Color(0x88000000),
                                        shape = CircleShape
                                    )
                            )
                        }

                        if (piece != null) {
                            Text(
                                text = pieceToUnicode(piece),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun pieceToUnicode(piece: Piece): String {
    return when (piece.color) {
        PieceColor.White -> when (piece.type) {
            PieceType.King -> "♔"
            PieceType.Queen -> "♕"
            PieceType.Rook -> "♖"
            PieceType.Bishop -> "♗"
            PieceType.Knight -> "♘"
            PieceType.Pawn -> "♙"
        }
        PieceColor.Black -> when (piece.type) {
            PieceType.King -> "♚"
            PieceType.Queen -> "♛"
            PieceType.Rook -> "♜"
            PieceType.Bishop -> "♝"
            PieceType.Knight -> "♞"
            PieceType.Pawn -> "♟"
        }
    }
}
