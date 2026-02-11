package com.example.magnusing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class BottomNavItem {
    HOME,
    HISTORY
}

@Composable
fun BottomNavBar(
    selectedItem: BottomNavItem,
    onHomeClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Surface(color = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.25f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 76.dp)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RetroNavItem(
                    selected = selectedItem == BottomNavItem.HOME,
                    label = "HOME",
                    icon = {
                        Text(
                            text = "♟",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    onClick = onHomeClick,
                    modifier = Modifier.weight(1f)
                )

                RetroNavItem(
                    selected = selectedItem == BottomNavItem.HISTORY,
                    label = "GAME HISTORY",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Game History"
                        )
                    },
                    onClick = onHistoryClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RetroNavItem(
    selected: Boolean,
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) Color.White else Color.Black
    val fg = if (selected) Color.Black else Color.White

    Column(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides fg) {
            icon()
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}
