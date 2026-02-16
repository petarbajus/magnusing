package com.example.magnusing.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlin.math.min
import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceColor
import pieceToDrawableRes

@Composable
fun ChessBoard(
    board: List<Piece?>,
    selectedSquare: Int?,
    legalTargets: Set<Int>,
    perspective: PieceColor,
    onSquareClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    fun toBoardIndex(displayRow: Int, displayCol: Int): Int =
        if (perspective == PieceColor.White)
            displayRow * 8 + displayCol
        else
            (7 - displayRow) * 8 + (7 - displayCol)

    // Classic chess palette: warm light + green dark
    val lightSquare = Color(0xFFF0EAD6) // parchment
    val darkSquare = Color(0xFF4F6F52)  // muted green

    // Selection highlight (neutral, works on both squares)
    val selectedOverlay = Color(0xFFB8C1B1)

    // Subtle grid lines so tiles are separated (helps readability)
    val gridLine = Color.Black.copy(alpha = 0.10f)

    // Target markers (adaptive to square color)
    val markerOnLight = Color.Black.copy(alpha = 0.35f)
    val markerOnDark = Color.White.copy(alpha = 0.45f)

    BoxWithConstraints(modifier = modifier) {
        val boardSize = min(this.maxWidth, this.maxHeight)

        Card(
            modifier = Modifier.size(boardSize),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // Frame padding inside the card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = Color.Black.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (row in 0 until 8) {
                        Row(modifier = Modifier.weight(1f)) {
                            for (col in 0 until 8) {
                                val isLight = (row + col) % 2 == 0
                                val index = toBoardIndex(row, col)
                                val isSelected = index == selectedSquare

                                val baseColor = if (isLight) lightSquare else darkSquare
                                val squareColor = if (isSelected) selectedOverlay else baseColor
                                val markerColor = if (isLight) markerOnLight else markerOnDark

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(squareColor)
                                        .border(0.5.dp, gridLine)
                                        .clickable { onSquareClick(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    val piece = board[index]
                                    val isTarget = index in legalTargets

                                    // Legal move markers (always visible now)
                                    if (isTarget && piece == null) {
                                        // Dot with outline for maximum contrast
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(markerColor)
                                                .border(
                                                    width = 1.dp,
                                                    color = markerColor.copy(alpha = 0.6f),
                                                    shape = CircleShape
                                                )
                                        )
                                    }

                                    if (isTarget && piece != null) {
                                        // Capture ring
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, markerColor, CircleShape)
                                        )
                                    }

                                    // Piece image
                                    if (piece != null) {
                                        Image(
                                            painter = painterResource(id = pieceToDrawableRes(piece)),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
