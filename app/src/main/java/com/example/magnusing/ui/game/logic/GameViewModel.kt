package com.example.magnusing.ui.game.logic

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.magnusing.ui.game.logic.model.Piece
import com.example.magnusing.ui.game.logic.model.PieceColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class GameStatus { Playing, Check, Checkmate, Stalemate }

data class GameUiState(
    val selectedSquare: Int? = null,
    val board: List<Piece?> = startingBoard(),
    val sideToMove: PieceColor = PieceColor.White,

    // UI helpers
    val legalTargets: Set<Int> = emptySet(),
    val targetMoves: Map<Int, Move> = emptyMap(),

    // Game status
    val status: GameStatus = GameStatus.Playing,

    // Special-rule state
    val castlingRights: CastlingRights = CastlingRights(),
    val enPassantTarget: Int? = null
)

class GameViewModel : ViewModel() {

    private companion object {
        const val TAG = "GameViewModel"
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    fun onSquareTapped(index: Int) {
        val current = _uiState.value

        // No moves after game end
        if (current.status == GameStatus.Checkmate || current.status == GameStatus.Stalemate) return

        val selected = current.selectedSquare
        val board = current.board
        val sideToMove = current.sideToMove
        val pieceAtTap = board[index]

        // CASE 1: Nothing selected yet -> select your piece and compute LEGAL moves (including EP/castling)
        if (selected == null) {
            if (pieceAtTap != null && pieceAtTap.color == sideToMove) {
                val moves = legalMovesForPiece(
                    from = index,
                    piece = pieceAtTap,
                    board = board,
                    sideToMove = sideToMove,
                    castlingRights = current.castlingRights,
                    enPassantTarget = current.enPassantTarget
                )

                _uiState.value = current.copy(
                    selectedSquare = index,
                    legalTargets = moves.map { it.to }.toSet(),
                    targetMoves = moves.associateBy { it.to }
                )
            }
            return
        }

        // CASE 2: Tap same square -> deselect
        if (selected == index) {
            _uiState.value = current.copy(
                selectedSquare = null,
                legalTargets = emptySet(),
                targetMoves = emptyMap()
            )
            return
        }

        val movingPiece = board[selected]
        if (movingPiece == null) {
            _uiState.value = current.copy(
                selectedSquare = null,
                legalTargets = emptySet(),
                targetMoves = emptyMap()
            )
            return
        }

        // CASE 3: Tap another own piece -> switch selection + recompute legal moves
        if (pieceAtTap != null && pieceAtTap.color == sideToMove) {
            val moves = legalMovesForPiece(
                from = index,
                piece = pieceAtTap,
                board = board,
                sideToMove = sideToMove,
                castlingRights = current.castlingRights,
                enPassantTarget = current.enPassantTarget
            )

            _uiState.value = current.copy(
                selectedSquare = index,
                legalTargets = moves.map { it.to }.toSet(),
                targetMoves = moves.associateBy { it.to }
            )
            return
        }

        // CASE 4: Attempt move -> must be legal and present in targetMoves
        val move = current.targetMoves[index] ?: return

        val apply = applyMoveWithRules(
            board = board,
            move = move,
            sideToMove = sideToMove,
            castlingRights = current.castlingRights,
            enPassantTarget = current.enPassantTarget
        )

        val nextSide = if (sideToMove == PieceColor.White) PieceColor.Black else PieceColor.White
        val nextBoard = apply.board

        // Determine game status for the side that is about to move
        val inCheck = isInCheck(nextBoard, nextSide)

        // IMPORTANT: hasAnyLegalMove must consider castling rights + ep target for the next player
        val anyMoves = hasAnyLegalMove(
            board = nextBoard,
            sideToMove = nextSide,
            castlingRights = apply.castlingRights,
            enPassantTarget = apply.enPassantTarget
        )

        val status = when {
            !anyMoves && inCheck -> GameStatus.Checkmate
            !anyMoves && !inCheck -> GameStatus.Stalemate
            inCheck -> GameStatus.Check
            else -> GameStatus.Playing
        }

        if (status != GameStatus.Playing) {
            Log.d(TAG, "Game status: $status (side to move: $nextSide)")
        }

        _uiState.value = current.copy(
            board = nextBoard,
            selectedSquare = null,
            legalTargets = emptySet(),
            targetMoves = emptyMap(),
            sideToMove = nextSide,
            castlingRights = apply.castlingRights,
            enPassantTarget = apply.enPassantTarget,
            status = status
        )
    }

    fun newGame() {
        _uiState.value = GameUiState(
            selectedSquare = null,
            board = startingBoard(),
            sideToMove = PieceColor.White,
            legalTargets = emptySet(),
            targetMoves = emptyMap(),
            status = GameStatus.Playing,
            castlingRights = CastlingRights(),
            enPassantTarget = null
        )
    }
}
