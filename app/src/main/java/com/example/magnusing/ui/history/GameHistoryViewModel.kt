package com.example.magnusing.ui.history

import ChessRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GameHistoryUiState {
    data object Loading : GameHistoryUiState
    data class Loaded(val items: List<GameHistoryItem>) : GameHistoryUiState
    data class Error(val message: String) : GameHistoryUiState
}

class GameHistoryViewModel(
    private val repo: ChessRepository = ChessRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<GameHistoryUiState>(GameHistoryUiState.Loading)
    val state: StateFlow<GameHistoryUiState> = _state.asStateFlow()

    fun load() {
        _state.value = GameHistoryUiState.Loading
        viewModelScope.launch {
            try {
                val items = repo.fetchRecentGames(limit = 100)
                _state.value = GameHistoryUiState.Loaded(items)
            } catch (t: Throwable) {
                _state.value = GameHistoryUiState.Error(t.message ?: "Failed to load history.")
            }
        }
    }
}
