package com.example.magnusing.ui.game.model

import com.example.magnusing.ui.game.logic.GameStatus
import com.example.magnusing.ui.game.logic.startingBoard

data class GameUiState(
    val selectedSquare: Int? = null,
    val board: List<Piece?> = startingBoard(),
    val sideToMove: PieceColor = PieceColor.White,
    val targetMoves: Map<Int, Move> = emptyMap(),
    val gameStatus: GameStatus = GameStatus.Playing,
    val castlingRights: CastlingRights = CastlingRights(),
    val enPassantTargetSquare: Int? = null,
    val pendingPromotionMove: Move? = null,
    val playerColor: PieceColor = PieceColor.White
)
