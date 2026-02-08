package com.example.magnusing.ui.game.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.magnusing.ui.game.engine.StockfishEngine
import com.example.magnusing.ui.game.engine.toFen
import com.example.magnusing.ui.game.engine.uciToMove
import com.example.magnusing.ui.game.model.CastlingRights
import com.example.magnusing.ui.game.model.GameUiState
import com.example.magnusing.ui.game.model.Move
import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.game.model.PieceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class GameStatus { Playing, Check, Checkmate, Stalemate }
class GameViewModel : ViewModel() {
    private var isConfigured = false
    private val _uiState = MutableStateFlow(GameUiState())

    val uiState: StateFlow<GameUiState> = _uiState
    // --- Engine (Stockfish) integration ---
    private var engine: StockfishEngine? = null
    private var engineColor: PieceColor = PieceColor.Black
    private var playVsEngine: Boolean = true
    private var thinking: Boolean = false

    private var engineMoveTimeMs: Int = 400

    fun initEngine(stockfishEngine: StockfishEngine) {
        if (engine == null) engine = stockfishEngine
    }

    fun stopEngine() {
        val eng = engine ?: return
        engine = null

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { eng.stop() }
        }
    }

    fun setPlayVsEngine(enabled: Boolean, enginePlaysAs: PieceColor = PieceColor.Black) {
        playVsEngine = enabled
        engineColor = enginePlaysAs
        maybeMakeEngineMove()
    }

    fun onSquareTapped(currentlySelectedSquare: Int) {
        val currentState = _uiState.value

        if (hasGameEnded(currentState.gameStatus)) return
        if (thinking) return
        if (currentState.pendingPromotionMove != null) return // block input while promotion dialog is up

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

        if (move.isPromotion) {
            _uiState.value = currentState.copy(
                selectedSquare = null,
                targetMoves = emptyMap(),
                pendingPromotionMove = move
            )
            return
        }

        commitMoveAndSetCurrentState(move)
        maybeMakeEngineMove()
    }

    fun onPromotionChosen(pieceType: PieceType) {
        val currentState = _uiState.value
        val promotionMove = currentState.pendingPromotionMove ?: return

        commitMoveAndSetCurrentState(promotionMove.copy(promotionPiece = pieceType))
        maybeMakeEngineMove()
    }

    fun newGame() {
        _uiState.value = GameUiState(
            selectedSquare = null,
            board = startingBoard(),
            sideToMove = PieceColor.White,
            targetMoves = emptyMap(),
            gameStatus = GameStatus.Playing,
            castlingRights = CastlingRights(),
            enPassantTargetSquare = null,
            pendingPromotionMove = null
        )
        maybeMakeEngineMove()
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
        return gameStatus == GameStatus.Checkmate || gameStatus == GameStatus.Stalemate
    }

    private fun commitMoveAndSetCurrentState(move: Move) {
        val currentState = _uiState.value

        val apply = applyMove(
            board = currentState.board,
            move = move,
            sideToMove = currentState.sideToMove,
            castlingRights = currentState.castlingRights,
            enPassantTargetSquare = currentState.enPassantTargetSquare
        )

        val nextSideToPlay =
            if (currentState.sideToMove == PieceColor.White) PieceColor.Black else PieceColor.White

        val nextBoard = apply.board

        val inCheck = isInCheck(nextBoard, nextSideToPlay)
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
            gameStatus = gameStatus,
            pendingPromotionMove = null
        )
    }

    fun configureUiState(
        playerChoice: PieceColor,
        vsEngine: Boolean = true
    ) {
        if (isConfigured) return
        isConfigured = true

        playVsEngine = vsEngine
        engineColor =
            if (playerChoice == PieceColor.White) PieceColor.Black else PieceColor.White

        _uiState.value = _uiState.value.copy(
            board = startingBoard(),           // canonical orientation
            sideToMove = PieceColor.White,     // chess rule
            selectedSquare = null,
            targetMoves = emptyMap(),
            gameStatus = GameStatus.Playing,
            castlingRights = CastlingRights(),
            enPassantTargetSquare = null,
            pendingPromotionMove = null,
            playerColor = playerChoice
        )

        // If engine is White, it moves immediately
        maybeMakeEngineMove()
    }
    private fun maybeMakeEngineMove() {
        val s = _uiState.value
        val eng = engine ?: return

        if (!playVsEngine) return
        if (thinking) return
        if (hasGameEnded(s.gameStatus)) return
        if (s.pendingPromotionMove != null) return
        if (s.sideToMove != engineColor) return

        thinking = true

        viewModelScope.launch(Dispatchers.Main) {
            try {
                // Engine work off the main thread
                val bestUci = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    eng.start()
                    val fen = toFen(
                        board = s.board,
                        sideToMove = s.sideToMove,
                        castlingRights = s.castlingRights,
                        enPassantTargetSquare = s.enPassantTargetSquare
                    )
                    eng.bestMoveFromFen(fen, moveTimeMs = engineMoveTimeMs)
                }

                if (bestUci == "(none)" || bestUci == "0000") {
                    // no legal move: mate or stalemate for engine side
                    return@launch
                }

                val engineMove = uciToMove(
                    uci = bestUci,
                    board = s.board,
                    enPassantTargetSquare = s.enPassantTargetSquare
                )

                // If engine move is a promotion, we auto-queen unless uciToMove sets promotionPiece
                val moveToCommit =
                    if (engineMove.isPromotion && engineMove.promotionPiece == null)
                        engineMove.copy(promotionPiece = PieceType.Queen)
                    else
                        engineMove

                commitMoveAndSetCurrentState(moveToCommit)
            } finally {
                thinking = false
            }
        }
    }
}
