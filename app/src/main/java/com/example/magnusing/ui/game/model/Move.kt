package com.example.magnusing.ui.game.model

data class Move(
    val from: Int,
    val to: Int,
    val isEnPassant: Boolean = false,
    val isCastleKingSide: Boolean = false,
    val isCastleQueenSide: Boolean = false
)