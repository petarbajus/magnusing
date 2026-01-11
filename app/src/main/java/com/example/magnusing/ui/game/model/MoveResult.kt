package com.example.magnusing.ui.game.model

data class MoveResult(
    val board: List<Piece?>,
    val castlingRights: CastlingRights,
    val enPassantTargetSquare: Int?
)