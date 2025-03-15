package des.c5inco.mesh

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import des.c5inco.mesh.data.AppConfiguration
import des.c5inco.mesh.data.AppDataRepository
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.ui.ComponentStyling

fun main() = application {
    val appDataRepository = AppDataRepository()
    val presetColors by appDataRepository.getPresetColors().collectAsState(initial = emptyList())
    val customColors by appDataRepository.getCustomColors().collectAsState(initial = emptyList())

    val configuration = AppConfiguration(
        availableColors = presetColors + customColors,
    )
    val themeDefinition = JewelTheme.darkThemeDefinition()

    Window(
        state = rememberWindowState(
            width = 1024.dp,
            height = 768.dp
        ),
        onCloseRequest = ::exitApplication,
        title = "Mesh",
    ) {
        IntUiTheme(
            theme = themeDefinition,
            styling = ComponentStyling.default()
        ) {
            App(
                repository = appDataRepository,
                configuration = configuration,
                presetColors = presetColors,
                customColors = customColors,
            )
        }
    }
}