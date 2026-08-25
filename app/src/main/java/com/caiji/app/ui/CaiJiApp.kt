package com.caiji.app.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.caiji.app.ui.chart.ChartScreen
import com.caiji.app.ui.chart.ChartViewModel
import com.caiji.app.ui.diary.DiaryEditScreen
import com.caiji.app.ui.diary.DiaryEditorViewModel
import com.caiji.app.ui.diary.DiaryListScreen
import com.caiji.app.ui.diary.DiaryViewModel
import com.caiji.app.ui.highlight.HighlightScreen
import com.caiji.app.ui.highlight.HighlightViewModel
import com.caiji.app.ui.home.HomeScreen
import com.caiji.app.ui.home.HomeViewModel
import com.caiji.app.ui.lottery.LotteryEditScreen
import com.caiji.app.ui.lottery.LotteryViewModel
import com.caiji.app.ui.random.RandomScreen
import com.caiji.app.ui.scratch.ScratchScreen
import com.caiji.app.ui.scratch.ScratchViewModel
import com.caiji.app.ui.settings.SettingsScreen
import com.caiji.app.ui.settings.SettingsViewModel

object Routes {
    const val HOME = "home"
    const val DIARY_LIST = "diary"
    const val DIARY_EDIT = "diary/edit"           // ?id=
    const val HIGHLIGHT = "highlight"
    const val CHART = "chart"
    const val BUDGET = "budget"
    const val RANDOM = "random"
    const val SCRATCH = "scratch"
    const val SETTINGS = "settings"
    const val LOTTERY_EDIT = "lottery/edit"       // ?id=

    const val DIARY_EDIT_ROUTE = "$DIARY_EDIT?id={id}"
    const val LOTTERY_EDIT_ROUTE = "$LOTTERY_EDIT?id={id}"

    const val ARG_ID = "id"
    const val NO_ID = -1L
}

@Composable
fun CaiJiApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
            HomeScreen(
                vm = vm,
                onOpenDiary = { navController.navigate(Routes.DIARY_LIST) },
                onOpenHighlight = { navController.navigate(Routes.HIGHLIGHT) },
                onOpenChart = { navController.navigate(Routes.CHART) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onAddLottery = { navController.navigate(Routes.LOTTERY_EDIT) },
                onOpenRandom = { navController.navigate(Routes.RANDOM) },
                onOpenScratch = { navController.navigate(Routes.SCRATCH) }
            )
        }

        composable(Routes.DIARY_LIST) {
            val vm: DiaryViewModel = viewModel(factory = DiaryViewModel.Factory)
            DiaryListScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onAdd = { navController.navigate(Routes.DIARY_EDIT) },
                onEdit = { id -> navController.navigate("${Routes.DIARY_EDIT}?id=$id") }
            )
        }

        composable(
            route = Routes.DIARY_EDIT_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_ID) {
                type = NavType.LongType; defaultValue = Routes.NO_ID
            })
        ) { backStack ->
            val vm: DiaryEditorViewModel = viewModel(factory = DiaryEditorViewModel.Factory)
            val idArg = backStack.arguments?.getLong(Routes.ARG_ID) ?: Routes.NO_ID
            val diaryId = if (idArg > 0) idArg else null
            DiaryEditScreen(
                vm = vm,
                diaryId = diaryId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.LOTTERY_EDIT_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_ID) {
                type = NavType.LongType; defaultValue = Routes.NO_ID
            })
        ) { backStack ->
            val vm: LotteryViewModel = viewModel(factory = LotteryViewModel.Factory)
            val idArg = backStack.arguments?.getLong(Routes.ARG_ID) ?: Routes.NO_ID
            val recordId = if (idArg > 0) idArg else null
            LotteryEditScreen(
                vm = vm,
                recordId = recordId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HIGHLIGHT) {
            val vm: HighlightViewModel = viewModel(factory = HighlightViewModel.Factory)
            HighlightScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onOpenDiary = { id -> navController.navigate("${Routes.DIARY_EDIT}?id=$id") }
            )
        }

        composable(Routes.CHART) {
            val vm: ChartViewModel = viewModel(factory = ChartViewModel.Factory)
            ChartScreen(vm = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.RANDOM) {
            RandomScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SCRATCH) {
            val vm: ScratchViewModel = viewModel(factory = ScratchViewModel.Factory)
            ScratchScreen(vm = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            SettingsScreen(vm = vm, onBack = { navController.popBackStack() })
        }
    }
}
