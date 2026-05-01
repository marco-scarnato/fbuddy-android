package com.oxygen.finance_buddy.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxygen.finance_buddy.core.security.SecurityPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val securityPreferences: SecurityPreferences
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkInitialState()
    }

    private fun checkInitialState() {
        viewModelScope.launch {
            val isFirstContext = securityPreferences.isFirstAccess.first()
            if (isFirstContext) {
                _authState.value = AuthState.SetupPin
            } else {
                _authState.value = AuthState.EnterPin
            }
        }
    }

    fun setupPin(pin: String) {
        viewModelScope.launch {
            securityPreferences.savePin(pin)
            _authState.value = AuthState.Success
        }
    }

    fun verifyPin(pin: String) {
        viewModelScope.launch {
            val savedPin = securityPreferences.userPin.first()
            if (pin == savedPin) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("PIN non corretto")
            }
        }
    }
    
    fun bypassAuth() {
        // mock bypass just in case 
        _authState.value = AuthState.Success
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securityPreferences.setBiometricEnabled(enabled)
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object SetupPin : AuthState()
    object EnterPin : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

