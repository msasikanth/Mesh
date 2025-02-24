package des.c5inco.mesh

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import des.c5inco.mesh.ui.GradientCanvas
import des.c5inco.mesh.ui.SidePanel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    Row(
        Modifier.fillMaxSize()
    ) {
        var selectedColorPoint: Pair<Int, Int>? by remember { mutableStateOf(null) }

        GradientCanvas(
            onPointDrag = { selectedColorPoint = it },
            modifier = Modifier.weight(1f))
        SidePanel(
            selectedColorPoint = selectedColorPoint,
            modifier = Modifier.width(250.dp)
        )
    }
}