package des.c5inco.mesh

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.ui.ComponentStyling

fun main() = application {
    val themeDefinition = JewelTheme.darkThemeDefinition()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Mesher",
    ) {
        IntUiTheme(
            theme = themeDefinition,
            styling = ComponentStyling.default()
        ) {
            App()
        }
    }
}