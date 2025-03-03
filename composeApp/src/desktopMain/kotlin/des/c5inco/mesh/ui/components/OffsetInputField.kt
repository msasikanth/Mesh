package des.c5inco.mesh.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import des.c5inco.mesh.common.formatFloat
import org.jetbrains.jewel.ui.component.TextField

@Composable
fun OffsetInputField(
    value: Float,
    enabled: Boolean = false,
    paramName: String,
    onUpdate: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val textFieldState = remember(value) { TextFieldState(formatFloat(value)) }

    fun reset() {
        textFieldState.edit { replace(0, textFieldState.text.length, formatFloat(value)) }
    }

    fun validate() {
        textFieldState.text.toString().toFloatOrNull()?.let { next ->
            onUpdate(next)
        } ?: run {
            reset()
        }
    }

    TextField(
        state = textFieldState,
        enabled = enabled,
        leadingIcon = {
            ParameterSwatch(text = paramName, modifier = Modifier.size(16.dp).padding(end = 6.dp))
        },
        modifier = modifier
            .onFocusChanged { validate() }
            .onKeyEvent {
                when (it.key) {
                    Key.Enter,
                    Key.NumPadEnter,
                    Key.Tab -> {
                        validate()
                        if (it.key == Key.Enter || it.key == Key.NumPadEnter) {
                            focusManager.clearFocus()
                        } else {
                            return@onKeyEvent false
                        }
                    }

                    Key.Escape -> {
                        reset()
                        focusManager.clearFocus()
                    }
                }
                return@onKeyEvent true
            }
//            .width(84.dp)
    )
}