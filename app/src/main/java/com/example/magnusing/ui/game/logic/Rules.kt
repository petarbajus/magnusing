package com.example.magnusing.ui.game.logic

import com.example.magnusing.ui.game.logic.model.Piece
import com.example.magnusing.ui.game.logic.model.PieceColor
import com.example.magnusing.ui.game.logic.model.PieceType
import kotlin.collections.listOf
import kotlin.math.abs

// ----------------------------
// Models
// ----------------------------

data class CastlingRights(
    val whiteKingSide: Boolean = true,
    val whiteQueenSide: Boolean = true,
    val blackKingSide: Boolean = true,
    val blackQueenSide: Boolean = true
)

data class Move(
    val from: Int,
    val to: Int,
    val isEnPassant: Boolean = false,
    val isCastleKingSide: Boolean = false,
    val isCastleQueenSide: Boolean = false
)

data class ApplyResult(
    val board: List<Piece?>,
    val castlingRights: CastlingRights,
    val enPassantTarget: Int?
)

// ----------------------------
// Board helpers
// ----------------------------

private fun rowOf(i: Int) = i / 8
private fun colOf(i: Int) = i % 8
private fun idx(r: Int, c: Int) = r * 8 + c
private fun inBounds(r: Int, c: Int) = r in 0..7 && c in 0..7

// ----------------------------
// Apply move (with special rules)
// ----------------------------

fun applyMoveWithRules(
    board: List<Piece?>,
    move: Move,
    sideToMove: PieceColor,
    castlingRights: CastlingRights,
    enPassantTarget: Int?
): ApplyResult {
    val newBoard = board.toMutableList()
    val piece = newBoard[move.from] ?: return ApplyResult(board, castlingRights, enPassantTarget)

    // EP target is valid only for one ply; clear by default and set only on pawn double move.
    var nextEp: Int? = null
    var nextRights = castlingRights

    val capturedPiece = newBoard[move.to]

    // CASTLING: king move plus rook move
    if (piece.type == PieceType.King && (move.isCastleKingSide || move.isCastleQueenSide)) {
        newBoard[move.from] = null
        newBoard[move.to] = piece

        if (sideToMove == PieceColor.White) {
            if (move.isCastleKingSide) {
                // e1->g1, rook h1->f1
                val rookFrom = idx(7, 7)
                val rookTo = idx(7, 5)
                newBoard[rookTo] = newBoard[rookFrom]
                newBoard[rookFrom] = null
            } else {
                // e1->c1, rook a1->d1
                val rookFrom = idx(7, 0)
                val rookTo = idx(7, 3)
                newBoard[rookTo] = newBoard[rookFrom]
                newBoard[rookFrom] = null
            }
            nextRights = nextRights.copy(whiteKingSide = false, whiteQueenSide = false)
        } else {
            if (move.isCastleKingSide) {
                // e8->g8, rook h8->f8
                val rookFrom = idx(0, 7)
                val rookTo = idx(0, 5)
                newBoard[rookTo] = newBoard[rookFrom]
                newBoard[rookFrom] = null
            } else {
                // e8->c8, rook a8->d8
                val rookFrom = idx(0, 0)
                val rookTo = idx(0, 3)
                newBoard[rookTo] = newBoard[rookFrom]
                newBoard[rookFrom] = null
            }
            nextRights = nextRights.copy(blackKingSide = false, blackQueenSide = false)
        }

        return ApplyResult(newBoard, nextRights, nextEp)
    }

    // EN PASSANT: pawn moves to empty ep square, captures pawn behind
    if (piece.type == PieceType.Pawn && move.isEnPassant) {
        newBoard[move.from] = null
        newBoard[move.to] = piece

        val toR = rowOf(move.to)
        val toC = colOf(move.to)
        val capturedPawnSquare =
            if (sideToMove == PieceColor.White) idx(toR + 1, toC) else idx(toR - 1, toC)
        if (capturedPawnSquare in 0..63) {
            newBoard[capturedPawnSquare] = null
        }

        return ApplyResult(newBoard, nextRights, nextEp)
    }

    // Normal move (including normal captures)
    newBoard[move.from] = null
    newBoard[move.to] = piece

    // Update castling rights if king moved
    if (piece.type == PieceType.King) {
        nextRights = if (sideToMove == PieceColor.White) {
            nextRights.copy(whiteKingSide = false, whiteQueenSide = false)
        } else {
            nextRights.copy(blackKingSide = false, blackQueenSide = false)
        }
    }

    // Update castling rights if rook moved from original squares
    if (piece.type == PieceType.Rook) {
        when (move.from) {
            idx(7, 0) -> nextRights = nextRights.copy(whiteQueenSide = false)
            idx(7, 7) -> nextRights = nextRights.copy(whiteKingSide = false)
            idx(0, 0) -> nextRights = nextRights.copy(blackQueenSide = false)
            idx(0, 7) -> nextRights = nextRights.copy(blackKingSide = false)
        }
    }

    // Update castling rights if a rook is captured on its original square
    if (capturedPiece?.type == PieceType.Rook) {
        when (move.to) {
            idx(7, 0) -> nextRights = nextRights.copy(whiteQueenSide = false)
            idx(7, 7) -> nextRights = nextRights.copy(whiteKingSide = false)
            idx(0, 0) -> nextRights = nextRights.copy(blackQueenSide = false)
            idx(0, 7) -> nextRights = nextRights.copy(blackKingSide = false)
        }
    }

    // Set en passant target if pawn moved two squares
    if (piece.type == PieceType.Pawn) {
        val fromR = rowOf(move.from)
        val toR = rowOf(move.to)
        if (abs(toR - fromR) == 2) {
            val midR = (fromR + toR) / 2
            nextEp = idx(midR, colOf(move.from))
        }
    }

    return ApplyResult(newBoard, nextRights, nextEp)
}

// ----------------------------
// Attack / check detection
// ----------------------------

private fun findKingSquare(board: List<Piece?>, color: PieceColor): Int? {
    for (i in 0 until 64) {
        val p = board[i] ?: continue
        if (p.color == color && p.type == PieceType.King) return i
    }
    return null
}

private fun attacksKnight(from: Int, to: Int): Boolean {
    val dr = abs(rowOf(from) - rowOf(to))
    val dc = abs(colOf(from) - colOf(to))
    return (dr == 2 && dc == 1) || (dr == 1 && dc == 2)
}

private fun attacksKing(from: Int, to: Int): Boolean {
    val dr = abs(rowOf(from) - rowOf(to))
    val dc = abs(colOf(from) - colOf(to))
    return dr <= 1 && dc <= 1 && (dr + dc) != 0
}

private fun rayAttacks(board: List<Piece?>, from: Int, to: Int, dr: Int, dc: Int): Boolean {
    var r = rowOf(from) + dr
    var c = colOf(from) + dc
    while (inBounds(r, c)) {
        val i = idx(r, c)
        if (i == to) return true
        if (board[i] != null) return false
        r += dr
        c += dc
    }
    return false
}

fun isSquareAttacked(board: List<Piece?>, square: Int, by: PieceColor): Boolean {
    for (i in 0 until 64) {
        val p = board[i] ?: continue
        if (p.color != by) continue

        when (p.type) {
            PieceType.Knight -> if (attacksKnight(i, square)) return true
            PieceType.King -> if (attacksKing(i, square)) return true
            PieceType.Pawn -> {
                val r = rowOf(i)
                val c = colOf(i)
                val dir = if (by == PieceColor.White) -1 else 1
                for (dc in listOf(-1, 1)) {
                    val rr = r + dir
                    val cc = c + dc
                    if (inBounds(rr, cc) && idx(rr, cc) == square) return true
                }
            }
            PieceType.Bishop, PieceType.Rook, PieceType.Queen -> {
                val dirs = when (p.type) {
                    PieceType.Bishop -> listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
                    PieceType.Rook -> listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
                    else -> listOf(
                        -1 to -1, -1 to 1, 1 to -1, 1 to 1,
                        -1 to 0, 1 to 0, 0 to -1, 0 to 1
                    )
                }
                for ((dr, dc) in dirs) {
                    if (rayAttacks(board, i, square, dr, dc)) return true
                }
            }
        }
    }
    return false
}

fun isInCheck(board: List<Piece?>, color: PieceColor): Boolean {
    val kingSq = findKingSquare(board, color) ?: return false
    val attacker = if (color == PieceColor.White) PieceColor.Black else PieceColor.White
    return isSquareAttacked(board, kingSq, attacker)
}

// ----------------------------
// Pseudo-move generation (includes EP/castling)
// ----------------------------

private fun pseudoMovesForPiece(
    from: Int,
    piece: Piece,
    board: List<Piece?>,
    sideToMove: PieceColor,
    castlingRights: CastlingRights,
    enPassantTarget: Int?
): List<Move> {
    return when (piece.type) {
        PieceType.Pawn -> pawnPseudoMoves(from, piece.color, board, enPassantTarget)
        PieceType.Knight -> knightPseudoMoves(from, piece.color, board)
        PieceType.Bishop -> slidingPseudoMoves(from, piece.color, board, listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1))
        PieceType.Rook -> slidingPseudoMoves(from, piece.color, board, listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1))
        PieceType.Queen -> slidingPseudoMoves(from, piece.color, board, listOf(
            -1 to -1, -1 to 1, 1 to -1, 1 to 1,
            -1 to 0, 1 to 0, 0 to -1, 0 to 1
        ))
        PieceType.King -> kingPseudoMoves(from, piece.color, board, castlingRights)
    }
}

private fun knightPseudoMoves(from: Int, side: PieceColor, board: List<Piece?>): List<Move> {
    val r = rowOf(from)
    val c = colOf(from)
    val deltas = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)

    val out = mutableListOf<Move>()
    for ((dr, dc) in deltas) {
        val nr = r + dr
        val nc = c + dc
        if (!inBounds(nr, nc)) continue
        val to = idx(nr, nc)
        val dest = board[to]
        if (dest == null || dest.color != side) out.add(Move(from, to))
    }
    return out
}

private fun kingPseudoMoves(from: Int, side: PieceColor, board: List<Piece?>, rights: CastlingRights): List<Move> {
    val r = rowOf(from)
    val c = colOf(from)
    val deltas = listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)

    val out = mutableListOf<Move>()
    for ((dr, dc) in deltas) {
        val nr = r + dr
        val nc = c + dc
        if (!inBounds(nr, nc)) continue
        val to = idx(nr, nc)
        val dest = board[to]
        if (dest == null || dest.color != side) out.add(Move(from, to))
    }

    // Add castling candidates (we'll enforce "through check" later in legalMovesForPiece)
    if (side == PieceColor.White && from == idx(7, 4)) {
        // King-side: squares f1,g1 empty and rook on h1
        if (rights.whiteKingSide &&
            board[idx(7, 5)] == null && board[idx(7, 6)] == null &&
            board[idx(7, 7)]?.type == PieceType.Rook && board[idx(7, 7)]?.color == PieceColor.White
        ) {
            out.add(Move(from, idx(7, 6), isCastleKingSide = true))
        }
        // Queen-side: squares d1,c1,b1 empty and rook on a1
        if (rights.whiteQueenSide &&
            board[idx(7, 3)] == null && board[idx(7, 2)] == null && board[idx(7, 1)] == null &&
            board[idx(7, 0)]?.type == PieceType.Rook && board[idx(7, 0)]?.color == PieceColor.White
        ) {
            out.add(Move(from, idx(7, 2), isCastleQueenSide = true))
        }
    }

    if (side == PieceColor.Black && from == idx(0, 4)) {
        if (rights.blackKingSide &&
            board[idx(0, 5)] == null && board[idx(0, 6)] == null &&
            board[idx(0, 7)]?.type == PieceType.Rook && board[idx(0, 7)]?.color == PieceColor.Black
        ) {
            out.add(Move(from, idx(0, 6), isCastleKingSide = true))
        }
        if (rights.blackQueenSide &&
            board[idx(0, 3)] == null && board[idx(0, 2)] == null && board[idx(0, 1)] == null &&
            board[idx(0, 0)]?.type == PieceType.Rook && board[idx(0, 0)]?.color == PieceColor.Black
        ) {
            out.add(Move(from, idx(0, 2), isCastleQueenSide = true))
        }
    }

    return out
}

private fun slidingPseudoMoves(from: Int, side: PieceColor, board: List<Piece?>, dirs: List<Pair<Int, Int>>): List<Move> {
    val r0 = rowOf(from)
    val c0 = colOf(from)
    val out = mutableListOf<Move>()

    for ((dr, dc) in dirs) {
        var r = r0 + dr
        var c = c0 + dc
        while (inBounds(r, c)) {
            val to = idx(r, c)
            val dest = board[to]
            if (dest == null) {
                out.add(Move(from, to))
            } else {
                if (dest.color != side) out.add(Move(from, to))
                break
            }
            r += dr
            c += dc
        }
    }

    return out
}

private fun pawnPseudoMoves(from: Int, side: PieceColor, board: List<Piece?>, epTarget: Int?): List<Move> {
    val r = rowOf(from)
    val c = colOf(from)
    val dir = if (side == PieceColor.White) -1 else 1
    val startRow = if (side == PieceColor.White) 6 else 1

    val out = mutableListOf<Move>()

    // forward 1
    val oneR = r + dir
    if (inBounds(oneR, c)) {
        val one = idx(oneR, c)
        if (board[one] == null) {
            out.add(Move(from, one))

            // forward 2
            val twoR = r + 2 * dir
            if (r == startRow && inBounds(twoR, c)) {
                val two = idx(twoR, c)
                if (board[two] == null) out.add(Move(from, two))
            }
        }
    }

    // diagonal captures + en passant
    for (dc in listOf(-1, 1)) {
        val rr = r + dir
        val cc = c + dc
        if (!inBounds(rr, cc)) continue
        val to = idx(rr, cc)

        val dest = board[to]
        if (dest != null && dest.color != side) {
            out.add(Move(from, to))
        } else if (epTarget != null && to == epTarget) {
            out.add(Move(from, to, isEnPassant = true))
        }
    }

    return out
}

// ----------------------------
// Legal moves (filters pseudo by king safety + castling-through-check)
// ----------------------------

fun legalMovesForPiece(
    from: Int,
    piece: Piece,
    board: List<Piece?>,
    sideToMove: PieceColor,
    castlingRights: CastlingRights,
    enPassantTarget: Int?
): List<Move> {
    val pseudo = pseudoMovesForPiece(from, piece, board, sideToMove, castlingRights, enPassantTarget)
    val legal = mutableListOf<Move>()

    val opponent = if (sideToMove == PieceColor.White) PieceColor.Black else PieceColor.White

    for (m in pseudo) {
        // Extra castling checks: can't castle out of check or through attacked squares
        if (piece.type == PieceType.King && (m.isCastleKingSide || m.isCastleQueenSide)) {
            if (isInCheck(board, sideToMove)) continue

            val throughSquares = if (sideToMove == PieceColor.White) {
                if (m.isCastleKingSide) listOf(idx(7, 5), idx(7, 6)) else listOf(idx(7, 3), idx(7, 2))
            } else {
                if (m.isCastleKingSide) listOf(idx(0, 5), idx(0, 6)) else listOf(idx(0, 3), idx(0, 2))
            }

            var ok = true
            for (sq in throughSquares) {
                if (isSquareAttacked(board, sq, opponent)) { ok = false; break }
            }
            if (!ok) continue
        }

        val applied = applyMoveWithRules(
            board = board,
            move = m,
            sideToMove = sideToMove,
            castlingRights = castlingRights,
            enPassantTarget = enPassantTarget
        )

        // Must not leave your king in check
        if (!isInCheck(applied.board, sideToMove)) {
            legal.add(m)
        }
    }

    return legal
}

fun hasAnyLegalMove(
    board: List<Piece?>,
    sideToMove: PieceColor,
    castlingRights: CastlingRights,
    enPassantTarget: Int?
): Boolean {
    for (from in 0 until 64) {
        val piece = board[from] ?: continue
        if (piece.color != sideToMove) continue
        if (legalMovesForPiece(from, piece, board, sideToMove, castlingRights, enPassantTarget).isNotEmpty()) {
            return true
        }
    }
    return false
}
