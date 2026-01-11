package com.example.magnusing.ui.game.model

data class ApplyResult(
    val board: List<Piece?>,
    val castlingRights: CastlingRights,
    val enPassantTargetSquare: Int?
)