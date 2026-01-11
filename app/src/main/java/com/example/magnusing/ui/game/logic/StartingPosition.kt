package com.example.magnusing.ui.game.logic

import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.game.model.PieceType

fun startingBoard(): List<Piece?> {
    // index = row * 8 + col, where row=0 is the top of the board
    val board = MutableList<Piece?>(64) { null }

    fun set(row: Int, col: Int, piece: Piece) {
        board[row * 8 + col] = piece
    }

    // Black pieces (top)
    val B = PieceColor.Black
    set(0, 0, Piece(PieceType.Rook, B))
    set(0, 1, Piece(PieceType.Knight, B))
    set(0, 2, Piece(PieceType.Bishop, B))
    set(0, 3, Piece(PieceType.Queen, B))
    set(0, 4, Piece(PieceType.King, B))
    set(0, 5, Piece(PieceType.Bishop, B))
    set(0, 6, Piece(PieceType.Knight, B))
    set(0, 7, Piece(PieceType.Rook, B))
    for (col in 0 until 8) set(1, col, Piece(PieceType.Pawn, B))

    // White pieces (bottom)
    val W = PieceColor.White
    for (col in 0 until 8) set(6, col, Piece(PieceType.Pawn, W))
    set(7, 0, Piece(PieceType.Rook, W))
    set(7, 1, Piece(PieceType.Knight, W))
    set(7, 2, Piece(PieceType.Bishop, W))
    set(7, 3, Piece(PieceType.Queen, W))
    set(7, 4, Piece(PieceType.King, W))
    set(7, 5, Piece(PieceType.Bishop, W))
    set(7, 6, Piece(PieceType.Knight, W))
    set(7, 7, Piece(PieceType.Rook, W))

    return board
}
