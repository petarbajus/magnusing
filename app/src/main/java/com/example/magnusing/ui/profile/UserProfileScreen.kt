package com.example.magnusing.ui.profile

import ChessRepository
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.magnusing.R

@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    repo: ChessRepository = ChessRepository()
) {
    // Hard-coded identity
    val username = "Magnus"
    val avatarRes = R.drawable.steve_jobs

    // Fetch ELO from Firestore
    val eloState by produceState<Int?>(initialValue = null) {
        value = try {
            repo.fetchCurrentElo()
        } catch (_: Throwable) {
            1200 // fallback if offline / missing doc
        }
    }

    Scaffold(
        topBar = {
            Surface(color = Color.Black) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "USER PROFILE",
                        color = Color.White,
                        fontFamily = FontFamily.Serif,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = username.uppercase(),
                color = Color.White,
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(20.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ELO",
                        color = Color.Black,
                        fontFamily = FontFamily.Serif,
                        style = MaterialTheme.typography.labelLarge
                    )

                    Spacer(Modifier.height(8.dp))


                    Text(
                        text = eloState?.toString() ?: "…",
                        color = Color.Black,
                        fontFamily = FontFamily.Serif,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}
