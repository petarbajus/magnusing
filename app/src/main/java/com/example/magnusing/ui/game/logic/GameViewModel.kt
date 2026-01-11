package com.example.magnusing.ui.game.logic

import androidx.lifecycle.ViewModel
import com.example.magnusing.ui.game.model.CastlingRights
import com.example.magnusing.ui.game.model.GameUiState
import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class GameStatus { Playing, Check, Checkmate, Stalemate }

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    fun onSquareTapped(currentlySelectedSquare: Int) {

        val currentState = _uiState.value
        
        if (hasGameEnded(currentState.gameStatus)) 
            return

        val previouslySelectedSquare = currentState.selectedSquare
        val board = currentState.board
        val sideToMove = currentState.sideToMove
        val pieceAtTap = board[currentlySelectedSquare]

        // CASE 1 : No piece selected -> select one
        if (previouslySelectedSquare == null) {
            if (pieceAtTap != null && pieceAtTap.color == sideToMove) {
                storeLegalMovesAndSelectedSquare(
                    currentlySelectedSquare,
                    pieceAtTap,
                    board,
                    sideToMove,
                    currentState.castlingRights,
                    currentState.enPassantTargetSquare
                )
            }
            return
        }

        // CASE 2 : Same piece selected -> deselect
        if (previouslySelectedSquare == currentlySelectedSquare) {
            _uiState.value = currentState.copy(
                selectedSquare = null,
                targetMoves = emptyMap()
            )
            return
        }

        // Safety check
        val movingPiece = board[previouslySelectedSquare]
        if (movingPiece == null) {
            _uiState.value = currentState.copy(
                selectedSquare = null,
                targetMoves = emptyMap()
            )
            return
        }

        // CASE 3: Tap another own piece -> switch selection + recompute legal moves
        if (pieceAtTap != null && pieceAtTap.color == sideToMove) {
            storeLegalMovesAndSelectedSquare(
                currentlySelectedSquare,
                pieceAtTap,
                board,
                sideToMove,
                currentState.castlingRights,
                currentState.enPassantTargetSquare
            )
            return
        }

        // CASE 4: Attempt move -> must be legal and present in targetMoves
        val move = currentState.targetMoves[currentlySelectedSquare] ?: return

        val apply = applyMove(
            board = board,
            move = move,
            sideToMove = sideToMove,
            castlingRights = currentState.castlingRights,
            enPassantTargetSquare = currentState.enPassantTargetSquare
        )

        val nextSideToPlay = if (sideToMove == PieceColor.White) PieceColor.Black else PieceColor.White
        val nextBoard = apply.board

        // Determine game gameStatus for the side that is about to move
        val inCheck = isInCheck(nextBoard, nextSideToPlay)

        // IMPORTANT: hasAnyLegalMove must consider castling rights + ep target for the next player
        val anyMoves = hasAnyLegalMove(
            board = nextBoard,
            sideToMove = nextSideToPlay,
            castlingRights = apply.castlingRights,
            enPassantTargetSquare = apply.enPassantTargetSquare
        )

        val gameStatus = when {
            !anyMoves && inCheck -> GameStatus.Checkmate
            !anyMoves && !inCheck -> GameStatus.Stalemate
            inCheck -> GameStatus.Check
            else -> GameStatus.Playing
        }

        _uiState.value = currentState.copy(
            board = nextBoard,
            selectedSquare = null,
            targetMoves = emptyMap(),
            sideToMove = nextSideToPlay,
            castlingRights = apply.castlingRights,
            enPassantTargetSquare = apply.enPassantTargetSquare,
            gameStatus = gameStatus
        )
    }

    fun newGame() {
        _uiState.value = GameUiState(
            selectedSquare = null,
            board = startingBoard(),
            sideToMove = PieceColor.White,
            targetMoves = emptyMap(),
            gameStatus = GameStatus.Playing,
            castlingRights = CastlingRights(),
            enPassantTargetSquare = null
        )
    }
    private fun storeLegalMovesAndSelectedSquare(
        from: Int,
        piece: Piece,
        board: List<Piece?>,
        sideToMove: PieceColor,
        castlingRights: CastlingRights,
        enPassantTargetSquare: Int?
    ) {
        val moves = legalMovesForPiece(
            from = from,
            piece = piece,
            board = board,
            sideToMove = sideToMove,
            castlingRights = castlingRights,
            enPassantTargetSquare = enPassantTargetSquare
        )

        val currentState = _uiState.value

        _uiState.value = currentState.copy(
            selectedSquare = from,
            targetMoves = moves.associateBy { it.to }
        )
    }
    private fun hasGameEnded(gameStatus: GameStatus): Boolean {
        return gameStatus == GameStatus.Checkmate ||
                gameStatus == GameStatus.Stalemate
    }
}