package com.yourssohail.learnsupabase.data.network

import com.yourssohail.learnsupabase.BuildConfig
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.supabaseUrl,
        supabaseKey = BuildConfig.supabaseKey
    ) {
        install(GoTrue)
        install(ComposeAuth) {
            googleNativeLogin(serverClientId = BuildConfig.googleClientId)
        }
    }
}