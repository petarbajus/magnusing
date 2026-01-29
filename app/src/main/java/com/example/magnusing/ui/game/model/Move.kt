package com.example.magnusing.ui.game.model

data class Move(
    val from: Int,
    val to: Int,
    val isPromotion: Boolean = false,
    val promotionPiece: PieceType? = null,
    val isEnPassant: Boolean = false,
    val isCastleKingSide: Boolean = false,
    val isCastleQueenSide: Boolean = false
)