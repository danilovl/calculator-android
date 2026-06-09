package danilovl.calculator.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import danilovl.calculator.ui.calculator.CalculatorScreen
import danilovl.calculator.ui.converter.ConverterListScreen
import danilovl.calculator.ui.converter.ConverterType
import danilovl.calculator.ui.converter.CurrencyConverterScreen
import danilovl.calculator.ui.converter.UnitConverterScreen
import danilovl.calculator.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Calculator : Screen("calculator")
    object ConverterList : Screen("converter_list")
    object CurrencyConverter : Screen("currency_converter")
    object UnitConverter : Screen("unit_converter/{type}") {
        fun createRoute(type: ConverterType) = "unit_converter/${type.name}"
    }
    object Settings : Screen("settings")
}

private val enterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(tween(300))
}

private val exitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut(tween(300))
}

private val popEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn(tween(300))
}

private val popExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut(tween(300))
}

@Composable
fun AppNavigation(onLanguageChanged: (String) -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Calculator.route
    ) {
        composable(
            route = Screen.Calculator.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) {
            CalculatorScreen(
                onNavigateToConverter = { navController.navigate(Screen.ConverterList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.ConverterList.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) {
            ConverterListScreen(
                onNavigateToCalculator = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onConverterSelected = { type ->
                    if (type == ConverterType.CURRENCY) {
                        navController.navigate(Screen.CurrencyConverter.route)
                    } else {
                        navController.navigate(Screen.UnitConverter.createRoute(type))
                    }
                }
            )
        }

        composable(
            route = Screen.CurrencyConverter.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) {
            CurrencyConverterScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.UnitConverter.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType }),
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { backStackEntry ->
            val typeName = backStackEntry.arguments?.getString("type") ?: ConverterType.LENGTH.name
            val converterType = try { ConverterType.valueOf(typeName) } catch (e: Exception) { ConverterType.LENGTH }
            UnitConverterScreen(
                converterType = converterType,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLanguageChanged = onLanguageChanged
            )
        }
    }
}
