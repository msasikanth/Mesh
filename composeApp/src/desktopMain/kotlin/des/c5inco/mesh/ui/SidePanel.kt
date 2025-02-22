package des.c5inco.mesh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import des.c5inco.mesh.common.formatFloat
import des.c5inco.mesh.common.toHexString
import des.c5inco.mesh.ui.viewmodel.MainViewModel
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CheckboxRow
import org.jetbrains.jewel.ui.component.Link
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Typography

@Composable
fun SidePanel(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .width(250.dp)
        .fillMaxHeight()
        .background(JewelTheme.globalColors.panelBackground)
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Configuration",
                style = Typography.h2TextStyle(),
            )
            Link(
                text = "Reset",
                onClick = MainViewModel::resetColorPoints,
            )
        }
        CheckboxRow(
            text = "Show points",
            checked = MainViewModel.showPoints,
            onCheckedChange = { MainViewModel.showPoints = it },
        )
        CheckboxRow(
            text = "Constrain edge points",
            checked = MainViewModel.constrainEdgePoints,
            onCheckedChange = { MainViewModel.constrainEdgePoints = it },
        )

        LazyColumn {
            itemsIndexed(MainViewModel.colorPoints) { index, colorPoints ->
                Text("Row $index", style = Typography.h3TextStyle())
                colorPoints.forEachIndexed { index, point ->
                    Text(
                        "x: ${formatFloat(point.first.x)}, y: ${formatFloat(point.first.y)} // ${point.second.toHexString()}",
                        style = JewelTheme.editorTextStyle
                    )
                }
            }
        }
    }
}

