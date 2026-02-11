package com.example.magnusing.ui.profile

import ChessRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserEloViewModel(
    private val repo: ChessRepository = ChessRepository()
) : ViewModel() {

    private val _elo = MutableStateFlow<Int?>(null) // null = loading
    val elo: StateFlow<Int?> = _elo

    fun load() {
        // avoid refetching if already loaded
        if (_elo.value != null) return

        viewModelScope.launch {
            _elo.value = try {
                repo.fetchCurrentElo()
            } catch (_: Throwable) {
                1200
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _elo.value = null
            _elo.value = try {
                repo.fetchCurrentElo()
            } catch (_: Throwable) {
                1200
            }
        }
    }
}
