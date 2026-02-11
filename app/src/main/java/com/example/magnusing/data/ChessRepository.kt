import com.example.magnusing.ui.game.model.GameResult
import com.example.magnusing.ui.newgame.Opponent
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.history.GameHistoryItem
import com.example.magnusing.ui.history.toGameHistoryItem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await


class ChessRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val stateRef = db.collection("appState").document("main")
    private val gamesCol = db.collection("games")

    suspend fun ensureStateExists(startElo: Int = 1200) {
        val snap = stateRef.get().await()
        if (!snap.exists()) {
            stateRef.set(
                mapOf(
                    "elo" to startElo,
                    "wins" to 0,
                    "losses" to 0,
                    "draws" to 0,
                    "gamesPlayed" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun recordGameFinished(
        opponent: Opponent,
        playerColor: PieceColor,
        result: GameResult
    ) {
        val score = when (result) {
            GameResult.WIN -> 1.0
            GameResult.DRAW -> 0.5
            GameResult.LOSS -> 0.0
        }

        val gameRef = gamesCol.document() // auto-id

        db.runTransaction { tx ->
            val stateSnap = tx.get(stateRef)
            val currentElo = (stateSnap.getLong("elo") ?: 1200L).toInt()
            val gamesPlayed = (stateSnap.getLong("gamesPlayed") ?: 0L).toInt()

            val k = if (gamesPlayed < 30) 32 else 20
            val update = eloUpdate(
                playerElo = currentElo,
                opponentElo = opponent.elo,
                score = score,
                kFactor = k
            )

            val (winsInc, lossesInc, drawsInc) = when (result) {
                GameResult.WIN -> Triple(1, 0, 0)
                GameResult.LOSS -> Triple(0, 1, 0)
                GameResult.DRAW -> Triple(0, 0, 1)
            }

            // Update totals
            tx.set(
                stateRef,
                mapOf(
                    "elo" to update.newElo,
                    "wins" to FieldValue.increment(winsInc.toLong()),
                    "losses" to FieldValue.increment(lossesInc.toLong()),
                    "draws" to FieldValue.increment(drawsInc.toLong()),
                    "gamesPlayed" to FieldValue.increment(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )

            // Write game history
            tx.set(
                gameRef,
                mapOf(
                    "endedAt" to FieldValue.serverTimestamp(),
                    "result" to result.name,
                    "opponentId" to opponent.id,
                    "opponentName" to opponent.name,
                    "opponentElo" to opponent.elo,
                    "playerColor" to playerColor.name,
                    "playerEloBefore" to currentElo,
                    "playerEloAfter" to update.newElo,
                    "eloDelta" to update.delta
                )
            )
        }.await()
    }

    suspend fun fetchRecentGames(limit: Long = 50): List<GameHistoryItem> {
        val snap = gamesCol
            .orderBy("endedAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        return snap.documents.map { it.toGameHistoryItem() }
    }

    suspend fun fetchCurrentElo(): Int {
        val snap = stateRef.get().await()
        return (snap.getLong("elo") ?: 1200L).toInt()
    }
}
