package com.example.magnusing.ui.newgame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.magnusing.ui.game.model.PieceColor
import com.example.magnusing.ui.theme.MagnusingTheme
import kotlin.random.Random

data class Opponent(
    val id: String,
    val name: String,
    val elo: Int,
    val category: Category
)

enum class Category { Beginner, Intermediate, Hard }

enum class SideChoice { White, Random, Black }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGameScreen(
    onBackClick: () -> Unit,
    onPlayClick: (selected: Opponent, selectedSide: PieceColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val bots = remember {
        listOf(
            Opponent("rookie", "Rookie Bot", 400, Category.Beginner),
            Opponent("pawnpal", "Pawn Pal", 600, Category.Beginner),

            Opponent("tactics", "Tactics Bot", 1000, Category.Intermediate),
            Opponent("solid", "Solid Bot", 1200, Category.Intermediate),

            Opponent("crusher", "Crusher Bot", 1600, Category.Hard),
            Opponent("endgame", "Endgame Bot", 1900, Category.Hard),
        )
    }

    val beginner = bots.filter { it.category == Category.Beginner }
    val intermediate = bots.filter { it.category == Category.Intermediate }
    val hard = bots.filter { it.category == Category.Hard }

    var selectedBot by remember { mutableStateOf(bots.first()) }
    var sideChoice by remember { mutableStateOf(SideChoice.Random) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bots") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomControls(
                sideChoice = sideChoice,
                onSideChange = { sideChoice = it },
                onPlay = {
                    val chosenColor = when (sideChoice) {
                        SideChoice.White -> PieceColor.White
                        SideChoice.Black -> PieceColor.Black
                        SideChoice.Random ->
                            if (Random.nextBoolean()) PieceColor.White else PieceColor.Black
                    }
                    onPlayClick(selectedBot, chosenColor)
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SelectedBotHeader(bot = selectedBot)
            }

            item {
                BotSection(
                    title = "Beginner",
                    bots = beginner,
                    selectedId = selectedBot.id,
                    onSelect = { selectedBot = it }
                )
            }

            item {
                BotSection(
                    title = "Intermediate",
                    bots = intermediate,
                    selectedId = selectedBot.id,
                    onSelect = { selectedBot = it }
                )
            }

            item {
                BotSection(
                    title = "Hard",
                    bots = hard,
                    selectedId = selectedBot.id,
                    onSelect = { selectedBot = it }
                )
            }

            // Extra space so last row isn't hidden behind bottom controls
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun SelectedBotHeader(bot: Opponent) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Default avatar
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Bot avatar",
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bot.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${bot.elo} ELO",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = bot.category.name,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun BotSection(
    title: String,
    bots: List<Opponent>,
    selectedId: String,
    onSelect: (Opponent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(bots) { bot ->
                BotCard(
                    bot = bot,
                    selected = bot.id == selectedId,
                    onClick = { onSelect(bot) }
                )
            }
        }
    }
}

@Composable
private fun BotCard(
    bot: Opponent,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.size(width = 92.dp, height = 110.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Bot",
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = bot.name,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
            Text(
                text = "${bot.elo}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun BottomControls(
    sideChoice: SideChoice,
    onSideChange: (SideChoice) -> Unit,
    onPlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = sideChoice == SideChoice.White,
                onClick = { onSideChange(SideChoice.White) },
                shape = RoundedCornerShape(12.dp)
            ) { Text("White") }

            SegmentedButton(
                selected = sideChoice == SideChoice.Random,
                onClick = { onSideChange(SideChoice.Random) },
                shape = RoundedCornerShape(12.dp)
            ) { Text("?") }

            SegmentedButton(
                selected = sideChoice == SideChoice.Black,
                onClick = { onSideChange(SideChoice.Black) },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Black") }
        }

        androidx.compose.material3.Button(
            onClick = onPlay,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Play")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewGameScreenPreview() {
    MagnusingTheme {
        NewGameScreen(
            onBackClick = {},
            onPlayClick = { _, _ -> }
        )
    }
}
