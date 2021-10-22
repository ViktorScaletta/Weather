package com.gv.weather.core

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun <T> Flow<T>.collectWithLifecycle(lifecycleOwner: LifecycleOwner, onEachFlow: (T) -> Unit) =
    flowWithLifecycle(lifecycleOwner.lifecycle)
        .onEach { onEachFlow(it) }
        .launchIn(lifecycleOwner.lifecycleScope)

fun Context.getSharedPrefs(name: String): SharedPreferences =
    getSharedPreferences(name, Context.MODE_PRIVATE)

fun String.formatPhrase() = replace("Преимущественно", "Преим.")