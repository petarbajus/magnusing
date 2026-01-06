package com.example.magnusing.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu

enum class BottomNavItem {
    HOME,
    MORE
}

@Composable
fun BottomNavBar(
    selectedItem: BottomNavItem,
    onHomeClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.height(64.dp)
    ) {
        NavigationBarItem(
            selected = selectedItem == BottomNavItem.HOME,
            onClick = onHomeClick,
            icon = {
                Text("♟", style = MaterialTheme.typography.titleLarge)
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = selectedItem == BottomNavItem.MORE,
            onClick = onMoreClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "More"
                )
            },
            label = { Text("More") }
        )
    }
}
