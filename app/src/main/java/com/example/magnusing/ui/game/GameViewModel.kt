package com.example.magnusing.ui.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class GameUiState(
    val selectedSquare: Int? = null,
    val board: List<Piece?> = startingBoard(),
    val sideToMove: PieceColor = PieceColor.White
)

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    fun onSquareTapped(index: Int) {
        val current = _uiState.value
        val selected = current.selectedSquare
        val board = current.board
        val sideToMove = current.sideToMove

        val pieceAtTap = board[index]

        // CASE 1: Nothing selected yet
        if (selected == null) {
            // Only allow selecting a piece of the side whose turn it is
            if (pieceAtTap != null && pieceAtTap.color == sideToMove) {
                _uiState.value = current.copy(selectedSquare = index)
            }
            return
        }

        // CASE 2: Tapped the same square → deselect
        if (selected == index) {
            _uiState.value = current.copy(selectedSquare = null)
            return
        }

        val movingPiece = board[selected]

        // Safety check (should not normally happen)
        if (movingPiece == null) {
            _uiState.value = current.copy(selectedSquare = null)
            return
        }

        // Extra safety: selected piece must belong to sideToMove
        if (movingPiece.color != sideToMove) {
            _uiState.value = current.copy(selectedSquare = null)
            return
        }

        // CASE 3: Tapped another piece of the same side → change selection
        if (pieceAtTap != null && pieceAtTap.color == sideToMove) {
            _uiState.value = current.copy(selectedSquare = index)
            return
        }

        // CASE 4: Move (still illegal moves allowed)
        val newBoard = board.toMutableList()
        newBoard[selected] = null
        newBoard[index] = movingPiece // capture by overwrite

        val nextSide =
            if (sideToMove == PieceColor.White) PieceColor.Black
            else PieceColor.White

        _uiState.value = current.copy(
            board = newBoard,
            selectedSquare = null,
            sideToMove = nextSide
        )
    }

    fun newGame() {
        _uiState.value = GameUiState(
            selectedSquare = null,
            board = startingBoard(),
            sideToMove = PieceColor.White
        )
    }
}
