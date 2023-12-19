package com.yourssohail.learnsupabase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourssohail.learnsupabase.data.model.UserState
import com.yourssohail.learnsupabase.data.network.SupabaseClient.client
import com.yourssohail.learnsupabase.ui.theme.LearnSupabaseTheme
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.compose.auth.composable.rememberLoginWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.ui.ProviderButtonContent
import io.github.jan.supabase.gotrue.providers.Google

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LearnSupabaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(SupabaseExperimental::class)
@Composable
fun MainScreen(
    viewModel: SupabaseAuthViewModel = viewModel(),
) {
    val context = LocalContext.current
    val userState by viewModel.userState

    var currentUserState by remember { mutableStateOf("") }

    val action = client.composeAuth.rememberLoginWithGoogle(
        onResult = { result -> viewModel.checkGoogleLoginStatus(context, result) },
        fallback = {}
    )

    LaunchedEffect(Unit) {
        viewModel.isUserLoggedIn(
            context,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Button(onClick = {
            action.startFlow()
        }) {
            Text(text = "Login via Google")
        }
        OutlinedButton(
            onClick = { action.startFlow() },
            content = { ProviderButtonContent(provider = Google) })
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            onClick = {
                viewModel.logout(context)
            }) {
            Text(text = "Logout")
        }

        when (userState) {
            is UserState.Loading -> {
                LoadingComponent()
            }

            is UserState.Success -> {
                val message = (userState as UserState.Success).message
                currentUserState = message
            }

            is UserState.Error -> {
                val message = (userState as UserState.Error).message
                currentUserState = message
            }
        }

        Text(text = currentUserState)
    }
}