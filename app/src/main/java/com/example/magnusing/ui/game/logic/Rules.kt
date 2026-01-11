package com.example.magnusing.ui.game.logic

import com.example.magnusing.ui.game.model.MoveResult
import com.example.magnusing.ui.game.model.CastlingRights
import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.game.model.PieceType
import com.example.magnusing.ui.game.model.Move
import kotlin.collections.listOf
import kotlin.math.abs

fun applyMove(
    board: List<Piece?>,
    move: Move,
    sideToMove: PieceColor,
    castlingRights: CastlingRights,
    enPassantTargetSquare: Int?
): MoveResult {
    val newBoard = board.toMutableList()
    val piece = newBoard[move.from] ?: return MoveResult(board, castlingRights, enPassantTargetSquare)

    var nextEnPassantTargetSquare: Int? = null
    var nextCastlingRights = castlingRights

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
            nextCastlingRights = nextCastlingRights.copy(whiteKingSide = false, whiteQueenSide = false)
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
            nextCastlingRights = nextCastlingRights.copy(blackKingSide = false, blackQueenSide = false)
        }

        return MoveResult(newBoard, nextCastlingRights, nextEnPassantTargetSquare)
    }

    // EN PASSANT: pawn moves to empty ep square, captures pawn behind
    if (piece.type == PieceType.Pawn && move.isEnPassant) {
        newBoard[move.from] = null
        newBoard[move.to] = piece

        val destinationRow = rowOf(move.to)
        val destinationCol = colOf(move.to)
        val capturedPawnSquare =
            if (sideToMove == PieceColor.White) idx(destinationRow + 1, destinationCol) else idx(destinationRow - 1, destinationCol)

        newBoard[capturedPawnSquare] = null

        return MoveResult(newBoard, nextCastlingRights, nextEnPassantTargetSquare)
    }

    // Normal move (including normal captures)
    newBoard[move.from] = null
    newBoard[move.to] = piece

    // Update castling rights if king moved
    if (piece.type == PieceType.King) {
        nextCastlingRights = if (sideToMove == PieceColor.White) {
            nextCastlingRights.copy(whiteKingSide = false, whiteQueenSide = false)
        } else {
            nextCastlingRights.copy(blackKingSide = false, blackQueenSide = false)
        }
    }

    // Update castling rights if rook moved from original squares
    if (piece.type == PieceType.Rook) {
        when (move.from) {
            idx(7, 0) -> nextCastlingRights = nextCastlingRights.copy(whiteQueenSide = false)
            idx(7, 7) -> nextCastlingRights = nextCastlingRights.copy(whiteKingSide = false)
            idx(0, 0) -> nextCastlingRights = nextCastlingRights.copy(blackQueenSide = false)
            idx(0, 7) -> nextCastlingRights = nextCastlingRights.copy(blackKingSide = false)
        }
    }

    // Update castling rights if a rook is captured on its original square
    if (capturedPiece?.type == PieceType.Rook) {
        when (move.to) {
            idx(7, 0) -> nextCastlingRights = nextCastlingRights.copy(whiteQueenSide = false)
            idx(7, 7) -> nextCastlingRights = nextCastlingRights.copy(whiteKingSide = false)
            idx(0, 0) -> nextCastlingRights = nextCastlingRights.copy(blackQueenSide = false)
            idx(0, 7) -> nextCastlingRights = nextCastlingRights.copy(blackKingSide = false)
        }
    }

    // Set en passant target if pawn moved two squares
    if (piece.type == PieceType.Pawn) {
        val startRow = rowOf(move.from)
        val destinationRow = rowOf(move.to)
        if (abs(destinationRow - startRow) == 2) {
            val middleRow = (startRow + destinationRow) / 2
            nextEnPassantTargetSquare = idx(middleRow, colOf(move.from))
        }
    }

    return MoveResult(newBoard, nextCastlingRights, nextEnPassantTargetSquare)
}

private fun findKingSquare(board: List<Piece?>, color: PieceColor): Int? {
    for (i in 0 until 64) {
        val piece = board[i] ?: continue
        if (piece.color == color && piece.type == PieceType.King) return i
    }
    return null
}

private fun isKnightAttackingSquare(attackingSquare: Int, attackedSquare: Int): Boolean {
    val deltaRow = abs(rowOf(attackingSquare) - rowOf(attackedSquare))
    val deltaCol = abs(colOf(attackingSquare) - colOf(attackedSquare))
    return (deltaRow == 2 && deltaCol == 1) || (deltaRow == 1 && deltaCol == 2)
}

private fun isKingAttackingSquare(attackingSquare: Int, attackedSquare: Int): Boolean {
    val deltaRow = abs(rowOf(attackingSquare) - rowOf(attackedSquare))
    val deltaCol = abs(colOf(attackingSquare) - colOf(attackedSquare))
    return deltaRow <= 1 && deltaCol <= 1 && (deltaRow + deltaCol) != 0
}

private fun isSquareBeingRayAttacked(board: List<Piece?>, attackingSquare: Int, attackedSquare: Int, deltaRow: Int, deltaCol: Int): Boolean {
    var currentRow = rowOf(attackingSquare) + deltaRow
    var currentCol = colOf(attackingSquare) + deltaCol
    while (inBounds(currentRow, currentCol)) {
        val currentSquareIndex = idx(currentRow, currentCol)
        if (currentSquareIndex == attackedSquare) return true
        if (board[currentSquareIndex] != null) return false
        currentRow += deltaRow
        currentCol += deltaCol
    }
    return false
}

fun isSquareAttacked(board: List<Piece?>, attackedSquare: Int, by: PieceColor): Boolean {
    for (attackingSquare in 0 until 64) {
        val attackingPiece = board[attackingSquare] ?: continue
        if (attackingPiece.color != by) continue

        when (attackingPiece.type) {
            PieceType.Knight -> if (isKnightAttackingSquare(attackingSquare, attackedSquare)) return true
            PieceType.King -> if (isKingAttackingSquare(attackingSquare, attackedSquare)) return true
            PieceType.Pawn -> {
                val currentRow = rowOf(attackingSquare)
                val currentCol = colOf(attackingSquare)
                val dir = if (by == PieceColor.White) -1 else 1
                for (deltaCol in listOf(-1, 1)) {
                    val newRow = currentRow + dir
                    val newCol = currentCol + deltaCol
                    if (inBounds(newRow, newCol) && idx(newRow, newCol) == attackedSquare) return true
                }
            }
            PieceType.Bishop, PieceType.Rook, PieceType.Queen -> {
                val dirs = when (attackingPiece.type) {
                    PieceType.Bishop -> listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
                    PieceType.Rook -> listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
                    else -> listOf(
                        -1 to -1, -1 to 1, 1 to -1, 1 to 1,
                        -1 to 0, 1 to 0, 0 to -1, 0 to 1
                    )
                }
                for ((deltaRow, deltaCol) in dirs) {
                    if (isSquareBeingRayAttacked(board, attackingSquare, attackedSquare, deltaRow, deltaCol)) return true
                }
            }
        }
    }
    return false
}

fun isInCheck(board: List<Piece?>, color: PieceColor): Boolean {
    val kingSquare = findKingSquare(board, color) ?: return false
    val attackerColor = if (color == PieceColor.White) PieceColor.Black else PieceColor.White
    return isSquareAttacked(board, kingSquare, attackerColor)
}

private fun pseudoMovesForPiece(
    from: Int,
    piece: Piece,
    board: List<Piece?>,
    castlingRights: CastlingRights,
    enPassantTargetSquare: Int?
): List<Move> {
    return when (piece.type) {
        PieceType.Pawn -> pawnPseudoMoves(from, piece.color, board, enPassantTargetSquare)
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
    val currentRow = rowOf(from)
    val currentCol = colOf(from)
    val deltas = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)

    val out = mutableListOf<Move>()
    for ((deltaRow, deltaCol) in deltas) {
        val newRow = currentRow + deltaRow
        val newCol = currentCol + deltaCol
        if (!inBounds(newRow, newCol)) continue
        val destinationSquareIndex = idx(newRow, newCol)
        val destinationSquarePiece = board[destinationSquareIndex]
        if (destinationSquarePiece == null || destinationSquarePiece.color != side) out.add(Move(from, destinationSquareIndex))
    }
    return out
}

private fun kingPseudoMoves(from: Int, sideToMove: PieceColor, board: List<Piece?>, castlingRights: CastlingRights): List<Move> {
    val currentRow = rowOf(from)
    val currentCol = colOf(from)
    val deltas = listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)

    val out = mutableListOf<Move>()
    for ((deltaRow, deltaCol) in deltas) {
        val newRow = currentRow + deltaRow
        val newCol = currentCol + deltaCol
        if (!inBounds(newRow, newCol)) continue
        val destinationSquareIndex = idx(newRow, newCol)
        val destinationSquarePiece = board[destinationSquareIndex]
        if (destinationSquarePiece == null || destinationSquarePiece.color != sideToMove) out.add(Move(from, destinationSquareIndex))
    }

    val opponentColor = if (sideToMove == PieceColor.White) PieceColor.Black else PieceColor.White

    if (sideToMove == PieceColor.White && from == idx(7, 4)) {
        if (castlingRights.whiteKingSide &&
            board[idx(7, 5)] == null && board[idx(7, 6)] == null &&
            board[idx(7, 7)]?.type == PieceType.Rook && board[idx(7, 7)]?.color == PieceColor.White
            && !isInCheck(board, sideToMove) &&
            !areSquaresAttacked(board, opponentColor, listOf(idx(7, 5), idx(7, 6)))
        ) {
            out.add(Move(from, idx(7, 6), isCastleKingSide = true))
        }
        if (castlingRights.whiteQueenSide &&
            board[idx(7, 3)] == null && board[idx(7, 2)] == null && board[idx(7, 1)] == null &&
            board[idx(7, 0)]?.type == PieceType.Rook && board[idx(7, 0)]?.color == PieceColor.White
            && !isInCheck(board, sideToMove) &&
            !areSquaresAttacked(board, opponentColor, listOf(idx(7, 3), idx(7, 2)))
        ) {
            out.add(Move(from, idx(7, 2), isCastleQueenSide = true))
        }
    }

    if (sideToMove == PieceColor.Black && from == idx(0, 4)) {
        if (castlingRights.blackKingSide &&
            board[idx(0, 5)] == null && board[idx(0, 6)] == null &&
            board[idx(0, 7)]?.type == PieceType.Rook && board[idx(0, 7)]?.color == PieceColor.Black
            && !isInCheck(board, sideToMove) &&
            !areSquaresAttacked(board, opponentColor, listOf(idx(0, 5), idx(0, 6)))
        ) {
            out.add(Move(from, idx(0, 6), isCastleKingSide = true))
        }
        if (castlingRights.blackQueenSide &&
            board[idx(0, 3)] == null && board[idx(0, 2)] == null && board[idx(0, 1)] == null &&
            board[idx(0, 0)]?.type == PieceType.Rook && board[idx(0, 0)]?.color == PieceColor.Black
            && !isInCheck(board, sideToMove)
            && !areSquaresAttacked(board, opponentColor, listOf(idx(0, 3), idx(0, 2)))
        ) {
            out.add(Move(from, idx(0, 2), isCastleQueenSide = true))
        }
    }
    return out
}

private fun slidingPseudoMoves(from: Int, side: PieceColor, board: List<Piece?>, dirs: List<Pair<Int, Int>>): List<Move> {
    val currentRow = rowOf(from)
    val currentCol = colOf(from)
    val out = mutableListOf<Move>()

    for ((deltaRow, deltaCol) in dirs) {
        var newRow = currentRow + deltaRow
        var newCol = currentCol + deltaCol
        while (inBounds(newRow, newCol)) {
            val destinationSquareIndex = idx(newRow, newCol)
            val destinationSquarePiece = board[destinationSquareIndex]
            if (destinationSquarePiece == null) {
                out.add(Move(from, destinationSquareIndex))
            } else {
                if (destinationSquarePiece.color != side) out.add(Move(from, destinationSquareIndex))
                break
            }
            newRow += deltaRow
            newCol += deltaCol
        }
    }

    return out
}

private fun pawnPseudoMoves(from: Int, side: PieceColor, board: List<Piece?>, epTarget: Int?): List<Move> {
    val currentRow = rowOf(from)
    val currentCol = colOf(from)
    val direction = if (side == PieceColor.White) -1 else 1
    val startRow = if (side == PieceColor.White) 6 else 1

    val out = mutableListOf<Move>()

    // Forward 1
    val oneRowAfterStartRow = currentRow + direction
    if (inBounds(oneRowAfterStartRow, currentCol)) {
        val oneSquareAfterStartSquare = idx(oneRowAfterStartRow, currentCol)
        if (board[oneSquareAfterStartSquare] == null) {
            out.add(Move(from, oneSquareAfterStartSquare))

            // Forward 2
            val twoRowsAfterStartRow = currentRow + 2 * direction
            if (currentRow == startRow && inBounds(twoRowsAfterStartRow, currentCol)) {
                val twoSquaresAfterStartSquare = idx(twoRowsAfterStartRow, currentCol)
                if (board[twoSquaresAfterStartSquare] == null) out.add(Move(from, twoSquaresAfterStartSquare))
            }
        }
    }

    // Diagonal captures and en passant
    for (deltaCol in listOf(-1, 1)) {
        val newRow = currentRow + direction
        val newCol = currentCol + deltaCol
        if (!inBounds(newRow, newCol)) continue
        val diagonalSquareIndex = idx(newRow, newCol)

        val diagonalSquarePiece = board[diagonalSquareIndex]
        if (diagonalSquarePiece != null && diagonalSquarePiece.color != side) {
            out.add(Move(from, diagonalSquareIndex))
        } else if (epTarget != null && diagonalSquareIndex == epTarget) {
            out.add(Move(from, diagonalSquareIndex, isEnPassant = true))
        }
    }

    return out
}

fun legalMovesForPiece(
    from: Int,
    piece: Piece,
    board: List<Piece?>,
    sideToMove: PieceColor,
    castlingRights: CastlingRights,
    enPassantTargetSquare: Int?
): List<Move> {
    val pseudoMoves = pseudoMovesForPiece(from, piece, board, castlingRights, enPassantTargetSquare)
    val legalMoves = mutableListOf<Move>()

    for (pseudoMove in pseudoMoves) {

        val applied = applyMove(
            board = board,
            move = pseudoMove,
            sideToMove = sideToMove,
            castlingRights = castlingRights,
            enPassantTargetSquare = enPassantTargetSquare
        )

        // Must not leave your king in check
        if (!isInCheck(applied.board, sideToMove)) {
            legalMoves.add(pseudoMove)
        }
    }

    return legalMoves
}

fun hasAnyLegalMove(
    board: List<Piece?>,
    sideToMove: PieceColor,
    castlingRights: CastlingRights,
    enPassantTargetSquare: Int?
): Boolean {
    for (from in 0 until 64) {
        val piece = board[from] ?: continue
        if (piece.color != sideToMove) continue
        if (legalMovesForPiece(from, piece, board, sideToMove, castlingRights, enPassantTargetSquare).isNotEmpty()) {
            return true
        }
    }
    return false
}

private fun areSquaresAttacked(
    board: List<Piece?>,
    opponentColor: PieceColor,
    neighbouringSquares: List<Int>
): Boolean {
    for (square in neighbouringSquares) {
        if (isSquareAttacked(board, square, opponentColor)) {
            return true
        }
    }
    return false
}
