package com.example.magnusing.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.magnusing.ui.game.GameScreen
import com.example.magnusing.ui.home.HomeScreen
import com.example.magnusing.ui.newgame.NewGameScreen
import com.example.magnusing.ui.newgame.Opponent
import com.example.magnusing.ui.newgame.SideChoice

private object Routes {
    const val HOME = "home"
    const val NEW_GAME = "new_game"

    const val GAME = "game/{color}"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

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
                onPlayClick = { _, side ->
                    val colorArg = if (side == com.example.magnusing.ui.game.model.PieceColor.White) "w" else "b"
                    navController.navigate("game/$colorArg")
                }
            )
        }

        composable(Routes.GAME) { backStackEntry ->
            val colorArg = backStackEntry.arguments?.getString("color") ?: "w"
            val playerColor =
                if (colorArg == "b") com.example.magnusing.ui.game.model.PieceColor.Black
                else com.example.magnusing.ui.game.model.PieceColor.White

            GameScreen(
                onBackClick = { navController.popBackStack() },
                playerColor = playerColor
            )
        }

    }
}
