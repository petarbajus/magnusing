package com.example.magnusing.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.magnusing.R
import com.example.magnusing.ui.navigation.BottomNavBar
import com.example.magnusing.ui.navigation.BottomNavItem
import com.example.magnusing.ui.navigation.TopBar
import com.example.magnusing.ui.theme.MagnusingTheme

@Composable
fun HomeScreen(
    onPlayClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopBar(onUserClick = onUserClick) },
        bottomBar = {
            BottomNavBar(
                selectedItem = BottomNavItem.HOME,
                onHomeClick = { /* already here */ },
                onHistoryClick = onHistoryClick
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
                    .background(Color.Black.copy(alpha = 0.25f))
            )

            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
                    .fillMaxWidth(0.82f)
                    .height(68.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(6.dp),
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    ),
                shape = RoundedCornerShape(6.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(
                    text = "PLAY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontSize = 26.sp,
                        letterSpacing = 3.sp
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
            onHistoryClick = {},
            onUserClick = {}
        )
    }
}
