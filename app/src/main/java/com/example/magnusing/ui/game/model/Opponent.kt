package com.example.magnusing.ui.newgame

import androidx.annotation.DrawableRes
import com.example.magnusing.R

data class Opponent(
    val id: String,
    val name: String,
    val elo: Int,
    val category: Category,
    @DrawableRes val avatarRes: Int,
    val engineMoveTimeMs: Int
)

object OpponentsData {

    val all: List<Opponent> = listOf(
        Opponent(
            id = "trump",
            name = "Donald Trump",
            elo = 400,
            category = Category.Beginner,
            avatarRes = R.drawable.donald_trump,
            engineMoveTimeMs = 150
        ),
        Opponent(
            id = "musk",
            name = "Elon Musk",
            elo = 1200,
            category = Category.Intermediate,
            avatarRes = R.drawable.elon_musk,
            engineMoveTimeMs = 400
        ),
        Opponent(
            id = "cent",
            name = "50 Cent",
            elo = 2200,
            category = Category.Hard,
            avatarRes = R.drawable.cent_50,
            engineMoveTimeMs = 900
        )
    )

    fun byId(id: String): Opponent =
        all.firstOrNull { it.id == id } ?: all.first()
}