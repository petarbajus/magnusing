package com.example.magnusing.ui.game.engine

import com.example.magnusing.ui.game.logic.idx
import com.example.magnusing.ui.game.model.Move
import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceType
import kotlin.math.abs

fun uciToMove(
    uci: String,
    board: List<Piece?>,
    enPassantTargetSquare: Int?
): Move {
    val from = algebraicToIdx(uci.substring(0, 2))
    val to = algebraicToIdx(uci.substring(2, 4))

    val movingPiece = board.getOrNull(from)

    // Promotion (e7e8q)
    val promoType: PieceType? = if (uci.length >= 5) {
        when (uci[4]) {
            'q' -> PieceType.Queen
            'r' -> PieceType.Rook
            'b' -> PieceType.Bishop
            'n' -> PieceType.Knight
            else -> null
        }
    } else null

    val isCastleKingSide =
        movingPiece?.type == PieceType.King &&
                ((from == idx(7, 4) && to == idx(7, 6)) || (from == idx(0, 4) && to == idx(0, 6)))

    val isCastleQueenSide =
        movingPiece?.type == PieceType.King &&
                ((from == idx(7, 4) && to == idx(7, 2)) || (from == idx(0, 4) && to == idx(0, 2)))

    val isEnPassant =
        movingPiece?.type == PieceType.Pawn &&
                enPassantTargetSquare != null &&
                to == enPassantTargetSquare &&
                board[to] == null &&
                abs((to % 8) - (from % 8)) == 1

    return Move(
        from = from,
        to = to,
        isPromotion = promoType != null,
        promotionPiece = promoType,
        isCastleKingSide = isCastleKingSide,
        isCastleQueenSide = isCastleQueenSide,
        isEnPassant = isEnPassant
    )
}

private fun algebraicToIdx(s: String): Int {
    val file = s[0] - 'a'        // 0..7
    val rank = s[1] - '0'        // '1'..'8'
    val row = 8 - rank           // rank 8 => row 0
    val col = file
    return row * 8 + col
}
