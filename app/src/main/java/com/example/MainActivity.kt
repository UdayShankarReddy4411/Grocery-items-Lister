package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.GroceryViewModel
import com.example.ui.ListItemsScreen
import com.example.ui.ListSelectionScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GroceryViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full Edge-to-Edge immersion and transparent system navigation gesture insets
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentListId by viewModel.selectedListId.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    // Transition slide/fade animations for a professional feel
                    AnimatedContent(
                        targetState = currentListId,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(200))
                        },
                        label = "MainScreenNavigation"
                    ) { listId ->
                        if (listId == null) {
                            ListSelectionScreen(
                                viewModel = viewModel,
                                onListSelected = { id -> viewModel.selectList(id) }
                            )
                        } else {
                            ListItemsScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.selectList(null) }
                            )
                        }
                    }
                }
            }
        }
    }
}
