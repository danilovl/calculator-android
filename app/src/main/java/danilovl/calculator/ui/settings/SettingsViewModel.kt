package danilovl.calculator.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import danilovl.calculator.data.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Language(val code: String, val nativeName: String, val englishName: String)

val SUPPORTED_LANGUAGES = listOf(
    Language("en", "English", "English"),
    Language("de", "Deutsch", "German"),
    Language("fr", "Français", "French"),
    Language("es", "Español", "Spanish"),
    Language("it", "Italiano", "Italian"),
    Language("pt", "Português", "Portuguese"),
    Language("pl", "Polski", "Polish"),
    Language("cs", "Čeština", "Czech"),
    Language("ru", "Русский", "Russian"),
    Language("zh", "中文", "Chinese")
)

data class SettingsState(
    val selectedLanguage: String = "en"
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = PreferencesRepository(application)

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepo.languageFlow.collect { lang ->
                _state.update { it.copy(selectedLanguage = lang) }
            }
        }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch {
            prefsRepo.saveLanguage(code)
            _state.update { it.copy(selectedLanguage = code) }
        }
    }
}
