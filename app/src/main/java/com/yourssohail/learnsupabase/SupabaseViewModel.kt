package com.yourssohail.learnsupabase

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssohail.learnsupabase.data.model.Note
import com.yourssohail.learnsupabase.data.model.UserState
import com.yourssohail.learnsupabase.data.network.SupabaseClient.client
import com.yourssohail.learnsupabase.utils.SharedPreferenceHelper
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class SupabaseViewModel : ViewModel() {
    private val _userState = mutableStateOf<UserState>(UserState.Loading)
    val userState: State<UserState> = _userState

    private fun saveToken(context: Context) {
        viewModelScope.launch {
            val accessToken = client.gotrue.currentAccessTokenOrNull()
            val sharedPref = SharedPreferenceHelper(context)
            sharedPref.saveStringData("accessToken", accessToken)
        }

    }

    private fun getToken(context: Context): String? {
        val sharedPref = SharedPreferenceHelper(context)
        return sharedPref.getStringData("accessToken")
    }

    fun logout(context: Context) {
        val sharedPref = SharedPreferenceHelper(context)
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                client.gotrue.logout()
                sharedPref.clearPreferences()
                _userState.value = UserState.Success("Logged out successfully!")
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "")
            }
        }
    }

    fun checkGoogleLoginStatus(context: Context, result: NativeSignInResult) {
        _userState.value = UserState.Loading
        when (result) {
            is NativeSignInResult.Success -> {
                saveToken(context)
                _userState.value = UserState.Success("Logged in via Google")
            }

            is NativeSignInResult.ClosedByUser -> {}
            is NativeSignInResult.Error -> {
                val message = result.message
                _userState.value = UserState.Error(message)
            }

            is NativeSignInResult.NetworkError -> {
                val message = result.message
                _userState.value = UserState.Error(message)
            }
        }
    }

    fun isUserLoggedIn(
        context: Context,
    ) {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _userState.value = UserState.Success("User is not logged in!")
                } else {
                    client.gotrue.retrieveUser(token)
                    client.gotrue.refreshCurrentSession()
                    saveToken(context)
                    _userState.value = UserState.Success("User is already logged in!")
                }
            } catch (e: RestException) {
                _userState.value = UserState.Error(e.error)
            }
        }
    }

    fun saveNote() {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                client.postgrest["test"].insert(
                    Note(
                        note = "This is my first note."
                    ),
                )
                _userState.value = UserState.Success("Added note successfully!")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }

    fun getNote() {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                val data = client.postgrest["test"]
                    .select().decodeSingle<Note>()
                _userState.value = UserState.Success("Data: ${data.note}")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }

    fun updateNote() {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                client.postgrest["test"]
                    .update(
                        {
                            Note::note setTo "This is the updated note."
                        }
                    ) {
                        Note::id eq 1
                    }
                _userState.value = UserState.Success("Note updated successfully!")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                client.postgrest["test"]
                    .delete {
                        Note::id eq 1
                    }
                _userState.value = UserState.Success("Note deleted successfully!")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }

}