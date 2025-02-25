package des.c5inco.mesh.ui.views

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import org.jetbrains.jewel.ui.component.TextField

@Composable
fun DimensionInputField(
    value: Int,
    enabled: Boolean = false,
    paramName: String,
    onUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val textFieldState = remember(value) { TextFieldState(value.toString()) }

    fun reset() {
        textFieldState.edit { replace(0, textFieldState.text.length, value.toString()) }
    }

    fun validate() {
        textFieldState.text.toString().toIntOrNull()?.let { next ->
            onUpdate(next)
        } ?: run {
            reset()
        }
    }

    TextField(
        state = textFieldState,
        enabled = enabled,
        leadingIcon = {
            ParameterSwatch(
                text = paramName,
                modifier = Modifier
                    .height(16.dp)
                    .padding(end = 6.dp)
            )
        },
        modifier = modifier
            .onFocusChanged { validate() }
            .onKeyEvent {
                when (it.key) {
                    Key.Enter,
                    Key.Tab -> {
                        validate()
                        if (it.key == Key.Enter) {
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
    )
}