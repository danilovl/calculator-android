package danilovl.calculator

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import danilovl.calculator.data.PreferencesRepository
import danilovl.calculator.navigation.AppNavigation
import danilovl.calculator.ui.theme.BackgroundLight
import danilovl.calculator.ui.theme.CalculatorTheme
import danilovl.calculator.util.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private lateinit var prefsRepo: PreferencesRepository

    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferencesRepository(newBase)
        val lang = runBlocking { prefs.languageFlow.first() }
        val localeContext = LocaleHelper.applyLocale(newBase, lang)
        super.attachBaseContext(localeContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefsRepo = PreferencesRepository(this)
        enableEdgeToEdge()

        setContent {
            CalculatorTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = BackgroundLight
                ) {
                    AppNavigation(
                        onLanguageChanged = { langCode ->
                            recreate()
                        }
                    )
                }
            }
        }
    }
}
