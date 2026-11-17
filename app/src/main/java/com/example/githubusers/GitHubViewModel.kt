package com.example.githubusers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GitHubViewModel : ViewModel() {

    private val service: GitHubService = ApiInstance.createService(GitHubService::class.java)

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

    private val _retryCount = MutableStateFlow(0)
    val retryCount: StateFlow<Int> = _retryCount.asStateFlow()

    fun load(isRetry: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = safeApiCall { service.users(perPage = 5) }) {
                is ApiResult.Success -> {
                    _retryCount.value = 0
                    _uiState.value = UiState.Success(result.data)
                }
                is ApiResult.NetworkError -> {
                    if (isRetry) _retryCount.value += 1
                    _uiState.value = UiState.Error("No network", isNetworkError = true)
                }
                is ApiResult.HttpError -> {
                    if (isRetry) _retryCount.value += 1
                    _uiState.value = UiState.Error("Server error: ${result.code}")
                }
                is ApiResult.UnknownError -> {
                    if (isRetry) _retryCount.value += 1
                    _uiState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun selectUser(login: String) {
        viewModelScope.launch {
            when (val result = safeApiCall { service.user(login) }) {
                is ApiResult.Success -> {
                    _selectedUser.value = result.data
                }
                is ApiResult.HttpError -> {
                    _uiState.value = UiState.Error("Server error: ${result.code}")
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = UiState.Error("No network", isNetworkError = true)
                }
                is ApiResult.UnknownError -> {
                    _uiState.value = UiState.Error(result.message)
                }
            }
        }
    }
}
