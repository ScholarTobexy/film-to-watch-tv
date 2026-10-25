package com.filmtowatch.tv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.filmtowatch.tv.ui.AppViewModel
import com.filmtowatch.tv.ui.DetailsScreen
import com.filmtowatch.tv.ui.FilmTheme
import com.filmtowatch.tv.ui.HomeScreen
import com.filmtowatch.tv.ui.Screen
import com.filmtowatch.tv.ui.SupportScreen
import com.filmtowatch.tv.ui.SwipeScreen
import com.filmtowatch.tv.ui.Palette

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) openLink(intent)
        setContent {
            FilmTheme {
                App(vm)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openLink(intent)
    }

    /** filmtowatch://film/<imdb id> (Free My TV's "My List"): that watchlist movie's page, over Home. */
    private fun openLink(intent: Intent?) {
        val link = intent?.data ?: return
        if (link.scheme != "filmtowatch" || link.host != "film") return
        val id = link.lastPathSegment?.takeIf { it.matches(Regex("tt\\d+")) } ?: return
        if (vm.film(id) == null) return
        while (vm.backStack.size > 1) vm.back()
        vm.navigate(Screen.Details(id, fromSwipe = false))
    }
}

@Composable
private fun App(vm: AppViewModel = viewModel()) {
    BackHandler(enabled = vm.backStack.size > 1) { vm.back() }
    Box(Modifier.fillMaxSize().background(Palette.Background)) {
        when (val screen = vm.backStack[vm.backStack.lastIndex]) {
            Screen.Home -> HomeScreen(vm)
            Screen.Swipe -> SwipeScreen(vm)
            Screen.Support -> SupportScreen(vm)
            is Screen.Details -> DetailsScreen(vm, screen)
        }
    }
}
