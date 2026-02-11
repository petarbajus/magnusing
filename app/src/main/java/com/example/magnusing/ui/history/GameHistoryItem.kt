package com.example.magnusing.ui.history

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class GameHistoryItem(
    val id: String,
    val endedAt: Timestamp?,
    val result: String,
    val opponentName: String,
    val opponentElo: Int,
    val playerColor: String,
    val playerEloBefore: Int,
    val playerEloAfter: Int,
    val eloDelta: Int
)

fun DocumentSnapshot.toGameHistoryItem(): GameHistoryItem {
    fun intField(key: String, fallback: Int = 0): Int {
        val any = get(key)
        return when (any) {
            is Long -> any.toInt()
            is Int -> any
            is Double -> any.toInt()
            else -> fallback
        }
    }

    return GameHistoryItem(
        id = id,
        endedAt = getTimestamp("endedAt"),
        result = getString("result") ?: "UNKNOWN",
        opponentName = getString("opponentName") ?: "Opponent",
        opponentElo = intField("opponentElo"),
        playerColor = getString("playerColor") ?: "WHITE",
        playerEloBefore = intField("playerEloBefore"),
        playerEloAfter = intField("playerEloAfter"),
        eloDelta = intField("eloDelta")
    )
}
