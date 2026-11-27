package com.example.suararasa.item

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a209020Lab6.ui.mood.MoodEntryBody
import com.example.suararasa.MoodTrackerTopAppBar
import com.example.suararasa.R
import com.example.suararasa.AppViewModelProvider
import com.example.suararasa.navigation.NavigationDestination
import kotlinx.coroutines.launch

object MoodEditDestination : NavigationDestination {
    override val route = "mood_edit"
    override val titleRes = R.string.edit_mood_title
    const val moodIdArg = "moodId"
    val routeWithArgs = "$route/{$moodIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodEditScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MoodEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            MoodTrackerTopAppBar(
                title = stringResource(MoodEditDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        modifier = modifier
    ) { innerPadding ->
        MoodEntryBody(
            moodUiState = viewModel.moodUiState,
            onMoodValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.updateMood()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                )
                .verticalScroll(rememberScrollState())
        )
    }
}

