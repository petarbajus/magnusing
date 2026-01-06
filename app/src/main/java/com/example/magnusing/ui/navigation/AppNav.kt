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
    const val GAME = "game"
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
                onPlayClick = { selected, side ->
                    navController.navigate(Routes.GAME)
                }
            )
        }

        composable(Routes.GAME) {
            GameScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}
