package com.example.magnusing.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.magnusing.ui.game.GameScreen
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.home.HomeScreen
import com.example.magnusing.ui.newgame.Category
import com.example.magnusing.ui.newgame.NewGameScreen
import com.example.magnusing.ui.newgame.Opponent
import com.example.magnusing.R
import com.example.magnusing.ui.newgame.OpponentsData

private object Routes {
    const val HOME = "home"
    const val NEW_GAME = "new_game"

    // ✅ now includes opponentId
    const val GAME = "game/{color}/{opponentId}"

    fun game(color: String, opponentId: String) = "game/$color/$opponentId"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val opponents = OpponentsData.all

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onPlayClick = { navController.navigate(Routes.NEW_GAME) },
                onMoreClick = { /* TODO */ },
                onUserClick = { /* TODO */ }
            )
        }

        composable(Routes.NEW_GAME) {
            NewGameScreen(
                onBackClick = { navController.popBackStack() },
                onPlayClick = { opponent, side ->
                    val colorArg = if (side == PieceColor.White) "w" else "b"
                    navController.navigate(Routes.game(colorArg, opponent.id))
                }
            )
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(
                navArgument("color") { type = NavType.StringType },
                navArgument("opponentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val colorArg = backStackEntry.arguments?.getString("color") ?: "w"
            val opponentId = backStackEntry.arguments?.getString("opponentId") ?: "trump"

            val playerColor = if (colorArg == "b") PieceColor.Black else PieceColor.White

            val opponent = opponents.firstOrNull { it.id == opponentId } ?: opponents.first()

            GameScreen(
                opponent = opponent,
                onBackClick = { navController.popBackStack() },
                playerColor = playerColor
            )
        }
    }
}
