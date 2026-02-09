import androidx.annotation.DrawableRes
import com.example.magnusing.R
import com.example.magnusing.ui.game.model.Piece
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.game.model.PieceType

@DrawableRes
fun pieceToDrawableRes(piece: Piece): Int {
    return when (piece.color) {
        PieceColor.White -> when (piece.type) {
            PieceType.King -> R.drawable.white_king
            PieceType.Queen -> R.drawable.white_queen
            PieceType.Rook -> R.drawable.white_rook
            PieceType.Bishop -> R.drawable.white_bishop
            PieceType.Knight -> R.drawable.white_horse
            PieceType.Pawn -> R.drawable.white_pawn
        }

        PieceColor.Black -> when (piece.type) {
            PieceType.King -> R.drawable.black_king
            PieceType.Queen -> R.drawable.black_queen
            PieceType.Rook -> R.drawable.black_rook
            PieceType.Bishop -> R.drawable.black_bishop
            PieceType.Knight -> R.drawable.black_horse
            PieceType.Pawn -> R.drawable.black_pawn
        }
    }
}
