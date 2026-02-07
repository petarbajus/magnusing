package com.example.magnusing.ui.game.engine

import com.example.magnusing.ui.game.model.CastlingRights
import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.game.model.PieceType

fun toFen(
    board: List<Piece?>,
    sideToMove: PieceColor,
    castlingRights: CastlingRights,
    enPassantTargetSquare: Int?
): String {
    val sb = StringBuilder()

    // board: row 0 is rank 8, row 7 is rank 1 (matches your idx mapping)
    for (row in 0 until 8) {
        var empty = 0
        for (col in 0 until 8) {
            val piece = board[row * 8 + col]
            if (piece == null) {
                empty++
            } else {
                if (empty > 0) {
                    sb.append(empty)
                    empty = 0
                }
                sb.append(pieceToFenChar(piece))
            }
        }
        if (empty > 0) sb.append(empty)
        if (row != 7) sb.append("/")
    }

    sb.append(" ")
    sb.append(if (sideToMove == PieceColor.White) "w" else "b")
    sb.append(" ")

    val castling = buildString {
        if (castlingRights.whiteKingSide) append("K")
        if (castlingRights.whiteQueenSide) append("Q")
        if (castlingRights.blackKingSide) append("k")
        if (castlingRights.blackQueenSide) append("q")
    }
    sb.append(if (castling.isEmpty()) "-" else castling)
    sb.append(" ")

    sb.append(enPassantTargetSquare?.let { idxToAlgebraic(it) } ?: "-")

    // halfmove/fullmove not tracked in your state yet—OK for Stockfish use
    sb.append(" 0 1")

    return sb.toString()
}

private fun pieceToFenChar(p: Piece): Char {
    val c = when (p.type) {
        PieceType.King -> 'k'
        PieceType.Queen -> 'q'
        PieceType.Rook -> 'r'
        PieceType.Bishop -> 'b'
        PieceType.Knight -> 'n'
        PieceType.Pawn -> 'p'
    }
    return if (p.color == PieceColor.White) c.uppercaseChar() else c
}

fun idxToAlgebraic(index: Int): String {
    val row = index / 8
    val col = index % 8
    val file = ('a'.code + col).toChar()
    val rank = 8 - row
    return "$file$rank"
}
