package com.yourssohail.learnsupabase

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import coil.compose.AsyncImage
import com.yourssohail.learnsupabase.data.model.UserState
import com.yourssohail.learnsupabase.data.network.SupabaseClient.client
import com.yourssohail.learnsupabase.ui.theme.LearnSupabaseTheme
import com.yourssohail.learnsupabase.utils.uriToByteArray
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
    viewModel: SupabaseViewModel = viewModel(),
) {
    val context = LocalContext.current
    val userState by viewModel.userState

    var currentUserState by remember { mutableStateOf("") }

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var imageUrl by remember {
        mutableStateOf("")
    }

    val launcher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

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
        Button(onClick = { viewModel.createBucket("photos") }) {
            Text(text = "Create bucket")
        }
        Button(onClick = {
            action.startFlow()
        }) {
            Text(text = "Login via Google")
        }
        OutlinedButton(
            onClick = { action.startFlow() },
            content = { ProviderButtonContent(provider = Google) })
        Button(onClick = { launcher.launch("image/*") }) {
            Text(text = "Select Image")
        }
        if(imageUri != null) {
            Button(onClick = {
                val imageByteArray = imageUri?.uriToByteArray(context)
                imageByteArray?.let {
                    viewModel.uploadFile("photos","newImage",it)
                }
            }) {
                Text(text = "Upload Image")
            }
        }
        Button(onClick = { viewModel.readFile("photos","newImage"){
            imageUrl = "${BuildConfig.supabaseUrl}/storage/v1/$it"
        } }) {
            Text(text = "Get Image")
        }
        Button(onClick = { viewModel.readPublicFile("photos","newImage"){
            imageUrl = it
        } }) {
            Text(text = "Get Public Image")
        }
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
        Text(text = if (imageUri != null) "Image is selected" else "")

        if(imageUrl.isNotEmpty()) {
            AsyncImage(model = imageUrl, contentDescription = "random image")
        }
    }
}