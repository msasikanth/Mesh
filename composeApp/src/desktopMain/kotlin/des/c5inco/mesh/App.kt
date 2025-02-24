package des.c5inco.mesh

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
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
        GradientCanvas(modifier = Modifier.weight(1f))
        SidePanel(modifier = Modifier.width(250.dp))
    }
}