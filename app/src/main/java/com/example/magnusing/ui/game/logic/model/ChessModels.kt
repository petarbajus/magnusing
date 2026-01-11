package com.example.magnusing.ui.game.logic.model

enum class PieceColor { White, Black }

enum class PieceType { King, Queen, Rook, Bishop, Knight, Pawn }

data class Piece(
    val type: PieceType,
    val color: PieceColor
)
