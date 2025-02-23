package des.c5inco.mesh.common

import androidx.compose.ui.graphics.Color
import java.math.RoundingMode

fun Color.toHexString(includeAlpha: Boolean = false): String {
    return if (includeAlpha) {
        String.format(
            "#%02X%02X%02X%02X",
            (alpha * 255).toInt(),
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt()
        )
    } else {
        String.format(
            "#%02X%02X%02X",
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt()
        )
    }
}

fun formatFloat(number: Float): String {
    return number.toBigDecimal().setScale(4, RoundingMode.UP).stripTrailingZeros().toPlainString()
}