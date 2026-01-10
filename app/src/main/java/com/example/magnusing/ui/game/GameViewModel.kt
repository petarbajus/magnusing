package com.example.magnusing.ui.game

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class GameStatus { Playing, Check, Checkmate, Stalemate }
data class GameUiState(
    val selectedSquare: Int? = null,
    val board: List<Piece?> = startingBoard(),
    val sideToMove: PieceColor = PieceColor.White,
    val legalTargets: Set<Int> = emptySet(),
    val status: GameStatus = GameStatus.Playing
)

class GameViewModel : ViewModel() {
    private companion object {
        const val TAG = "GameViewModel"
    }
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    fun onSquareTapped(index: Int) {
        val current = _uiState.value

        if (current.status == GameStatus.Checkmate || current.status == GameStatus.Stalemate) return

        val selected = current.selectedSquare
        val board = current.board
        val sideToMove = current.sideToMove
        val legalTargets = current.legalTargets

        val pieceAtTap = board[index]

        // CASE 1: Nothing selected yet → select your piece and compute LEGAL targets
        if (selected == null) {
            if (pieceAtTap != null && pieceAtTap.color == sideToMove) {
                val targets = legalTargetsForPiece(index, pieceAtTap, board)
                _uiState.value = current.copy(
                    selectedSquare = index,
                    legalTargets = targets
                )
            }
            return
        }

        // CASE 2: Tap same square → deselect
        if (selected == index) {
            _uiState.value = current.copy(
                selectedSquare = null,
                legalTargets = emptySet()
            )
            return
        }

        val movingPiece = board[selected]
        if (movingPiece == null) {
            _uiState.value = current.copy(
                selectedSquare = null,
                legalTargets = emptySet()
            )
            return
        }

        // CASE 3: Tap another own piece → switch selection
        if (pieceAtTap != null && pieceAtTap.color == sideToMove) {
            val targets = legalTargetsForPiece(index, pieceAtTap, board)
            _uiState.value = current.copy(
                selectedSquare = index,
                legalTargets = targets
            )
            return
        }

        // CASE 4: Attempt move → must be legal
        if (index !in legalTargets) return

        // Apply move
        val newBoard = board.toMutableList()
        newBoard[selected] = null
        newBoard[index] = movingPiece

        val nextSide =
            if (sideToMove == PieceColor.White) PieceColor.Black else PieceColor.White

        val nextBoard = newBoard.toList()

        // Determine game status
        val inCheck = isInCheck(nextBoard, nextSide)
        val anyMoves = hasAnyLegalMove(nextBoard, nextSide)

        val status = when {
            !anyMoves && inCheck -> GameStatus.Checkmate
            !anyMoves && !inCheck -> GameStatus.Stalemate
            inCheck -> GameStatus.Check
            else -> GameStatus.Playing
        }

        Log.d(TAG, "Game status: $status (side to move: $nextSide)")

        _uiState.value = current.copy(
            board = nextBoard,
            selectedSquare = null,
            legalTargets = emptySet(),
            sideToMove = nextSide,
            status = status
        )
    }


    fun newGame() {
        _uiState.value = GameUiState(
            selectedSquare = null,
            board = startingBoard(),
            sideToMove = PieceColor.White,
            legalTargets = emptySet(),
            status = GameStatus.Playing
        )
    }
}
