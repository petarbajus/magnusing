package com.example.magnusing.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.magnusing.ui.navigation.BottomNavBar
import com.example.magnusing.ui.navigation.BottomNavItem
import com.example.magnusing.ui.navigation.TopBar
import com.example.magnusing.ui.theme.MagnusingTheme
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.magnusing.R

@Composable
fun HomeScreen(
    onPlayClick: () -> Unit,
    onMoreClick: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopBar(onUserClick = onUserClick)
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
                .padding(padding)
        ) {

            Image(
                painter = painterResource(id = R.drawable.chess),
                contentDescription = "Chess",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.25f)
                    )
            )

            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
                    .fillMaxWidth(0.82f)
                    .height(68.dp)
                    // Custom shadow (more retro than Material elevation)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(6.dp),
                        ambientColor = androidx.compose.ui.graphics.Color.Black,
                        spotColor = androidx.compose.ui.graphics.Color.Black
                    ),
                shape = RoundedCornerShape(6.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp, // disable Material elevation
                    pressedElevation = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground, // black
                    contentColor = MaterialTheme.colorScheme.background       // white
                )
            ) {
                Text(
                    text = "PLAY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontSize = 26.sp,          // ⬅ bigger, confident
                        letterSpacing = 3.sp       // ⬅ crucial for retro feel
                    )
                )
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
