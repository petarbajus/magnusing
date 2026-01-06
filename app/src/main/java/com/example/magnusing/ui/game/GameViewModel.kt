package com.example.magnusing.ui.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class GameUiState(
    val selectedSquare: Int? = null,
    val board: List<Piece?> = startingBoard(),
    val sideToMove: PieceColor = PieceColor.White,
    val legalTargets: Set<Int> = emptySet()
)

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    fun onSquareTapped(index: Int) {
        val current = _uiState.value
        val selected = current.selectedSquare
        val board = current.board
        val sideToMove = current.sideToMove
        val legalTargets = current.legalTargets

        val pieceAtTap = board[index]


        // CASE 1: Nothing selected yet -> select your piece and compute legal targets
        if (selected == null) {
            if (pieceAtTap != null && pieceAtTap.color == sideToMove) {
                val targets = targetsForPiece(index, pieceAtTap, board)
                _uiState.value = current.copy(
                    selectedSquare = index,
                    legalTargets = targets
                )
            }
            return
        }

        // CASE 2: Tap same square -> deselect and clear targets
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

        // If user taps another of their own pieces, switch selection + recompute targets
        if (pieceAtTap != null && pieceAtTap.color == sideToMove) {
            val targets = targetsForPiece(index, pieceAtTap, board)
            _uiState.value = current.copy(
                selectedSquare = index,
                legalTargets = targets
            )
            return
        }

        // CASE 3: Attempt move -> only allow if target is legal
        if (index !in legalTargets) {
            // illegal tap: do nothing (or you can clear selection; I prefer do nothing)
            return
        }

        val newBoard = board.toMutableList()
        newBoard[selected] = null
        newBoard[index] = movingPiece

        val nextSide = if (sideToMove == PieceColor.White) PieceColor.Black else PieceColor.White

        _uiState.value = current.copy(
            board = newBoard,
            selectedSquare = null,
            legalTargets = emptySet(),
            sideToMove = nextSide
        )
    }

    fun newGame() {
        _uiState.value = GameUiState(
            selectedSquare = null,
            board = startingBoard(),
            sideToMove = PieceColor.White,
            legalTargets = emptySet()
        )
    }
}
