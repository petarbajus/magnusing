package com.example.magnusing.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.magnusing.ui.navigation.BottomNavBar
import com.example.magnusing.ui.navigation.BottomNavItem
import com.example.magnusing.ui.navigation.TopBar
import com.example.magnusing.ui.theme.MagnusingTheme

@Composable
fun HomeScreen(
    onPlayClick: () -> Unit,
    onMoreClick: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopBar(
                onUserClick = onUserClick
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedItem = BottomNavItem.HOME,
                onHomeClick = { /* already here */ },
                onMoreClick = onMoreClick
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .padding(bottom = 96.dp)
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text("Play")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MagnusingTheme {
        HomeScreen(
            onPlayClick = {},
            onMoreClick = {},
            onUserClick = {}
        )
    }
}
