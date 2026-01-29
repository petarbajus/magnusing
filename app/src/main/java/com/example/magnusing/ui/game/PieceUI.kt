import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.game.model.PieceType

fun pieceToUnicode(piece: Piece): String {
    return when (piece.color) {
        PieceColor.White -> when (piece.type) {
            PieceType.King -> "♔"
            PieceType.Queen -> "♕"
            PieceType.Rook -> "♖"
            PieceType.Bishop -> "♗"
            PieceType.Knight -> "♘"
            PieceType.Pawn -> "♙"
        }
        PieceColor.Black -> when (piece.type) {
            PieceType.King -> "♚"
            PieceType.Queen -> "♛"
            PieceType.Rook -> "♜"
            PieceType.Bishop -> "♝"
            PieceType.Knight -> "♞"
            PieceType.Pawn -> "♟"
        }
    }
}