package com.example.magnusing.ui.game

data class Move(val from: Int, val to: Int)

private fun findKingSquare(board: List<Piece?>, color: PieceColor): Int? {
    for (i in 0 until 64) {
        val p = board[i] ?: continue
        if (p.color == color && p.type == PieceType.King) return i
    }
    return null
}

fun applyMove(board: List<Piece?>, move: Move): List<Piece?> {
    val newBoard = board.toMutableList()
    val piece = newBoard[move.from]
    newBoard[move.from] = null
    newBoard[move.to] = piece
    return newBoard
}

private fun rowOf(i: Int) = i / 8
private fun colOf(i: Int) = i % 8
private fun inBounds(r: Int, c: Int) = r in 0..7 && c in 0..7
private fun idx(r: Int, c: Int) = r * 8 + c

private fun attacksKnight(from: Int, to: Int): Boolean {
    val dr = kotlin.math.abs(rowOf(from) - rowOf(to))
    val dc = kotlin.math.abs(colOf(from) - colOf(to))
    return (dr == 2 && dc == 1) || (dr == 1 && dc == 2)
}

private fun attacksKing(from: Int, to: Int): Boolean {
    val dr = kotlin.math.abs(rowOf(from) - rowOf(to))
    val dc = kotlin.math.abs(colOf(from) - colOf(to))
    return dr <= 1 && dc <= 1 && (dr + dc) != 0
}

private fun rayAttacks(
    board: List<Piece?>,
    from: Int,
    to: Int,
    dr: Int,
    dc: Int
): Boolean {
    var r = rowOf(from) + dr
    var c = colOf(from) + dc
    while (inBounds(r, c)) {
        val i = idx(r, c)
        if (i == to) return true
        if (board[i] != null) return false // blocked
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
            PieceType.Knight -> {
                if (attacksKnight(i, square)) return true
            }
            PieceType.King -> {
                if (attacksKing(i, square)) return true
            }
            PieceType.Pawn -> {
                val r = rowOf(i)
                val c = colOf(i)
                val dir = if (by == PieceColor.White) -1 else 1
                // Pawns attack diagonally forward
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

fun legalTargetsForPiece(from: Int, piece: Piece, board: List<Piece?>): Set<Int> {
    val pseudo = targetsForPiece(from, piece, board)
    val legal = mutableSetOf<Int>()
    for (to in pseudo) {
        val next = applyMove(board, Move(from, to))
        if (!isInCheck(next, piece.color)) {
            legal.add(to)
        }
    }
    return legal
}

fun hasAnyLegalMove(board: List<Piece?>, sideToMove: PieceColor): Boolean {
    for (from in 0 until 64) {
        val piece = board[from] ?: continue
        if (piece.color != sideToMove) continue
        if (legalTargetsForPiece(from, piece, board).isNotEmpty()) return true
    }
    return false
}

