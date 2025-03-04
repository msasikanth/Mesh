package des.c5inco.mesh.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
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
    val textFieldState = remember { TextFieldState(value.toString()) }

    LaunchedEffect(Unit) {
        snapshotFlow { textFieldState.text }
            .collectLatest {
                val filteredValue = it.filter { char -> char.isDigit() }
                textFieldState.edit { replace(0, textFieldState.text.length, filteredValue) }
            }
    }

    fun reset() {
        textFieldState.edit { replace(0, textFieldState.text.length, value.toString()) }
    }

    fun validate() {
        try {
            textFieldState.text.toString().toIntOrNull()?.let { next ->
                val nextValue = next.coerceIn(2, 10)
                onUpdate(nextValue)
                textFieldState.edit { replace(0, textFieldState.text.length, nextValue.toString()) }
            } ?: run {
                reset()
            }
        } catch (e: Exception) {
            println(e.message)
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
    )
}