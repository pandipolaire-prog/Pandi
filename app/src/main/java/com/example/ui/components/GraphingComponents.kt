package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.MathEngine
import com.example.math.PointD
import kotlin.math.roundToInt

@Composable
fun FunctionGraphPlotter(
    modifier: Modifier = Modifier,
    expression1: String,
    expression2: String,
    xMin: Double,
    xMax: Double,
    yMin: Double,
    yMax: Double,
    line1Color: Color = Color(0xFF00E676), // Bright neon green
    line2Color: Color = Color(0xFF29B6F6), // Ocean sky blue
    gridColor: Color = Color(0xFF37474F), // Slate gray
    axesColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF101820)) // Dark obsidian background
            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width <= 0 || height <= 0 || xMin >= xMax || yMin >= yMax) return@Canvas

            // Helper for mapping math points (x, y) to screen pixels
            fun toScreenX(x: Double): Float {
                return ((x - xMin) / (xMax - xMin) * width).toFloat()
            }

            fun toScreenY(y: Double): Float {
                return (height - (y - yMin) / (yMax - yMin) * height).toFloat()
            }

            // Draw major grid lines (8 divisions)
            val divisions = 8
            for (i in 0..divisions) {
                // Vertical grid lines
                val pctX = i.toDouble() / divisions
                val gridXVal = xMin + pctX * (xMax - xMin)
                val pixelX = toScreenX(gridXVal)
                drawLine(
                    color = gridColor,
                    start = Offset(pixelX, 0f),
                    end = Offset(pixelX, height),
                    strokeWidth = 1f
                )

                // Horizontal grid lines
                val pctY = i.toDouble() / divisions
                val gridYVal = yMin + pctY * (yMax - yMin)
                val pixelY = toScreenY(gridYVal)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, pixelY),
                    end = Offset(width, pixelY),
                    strokeWidth = 1f
                )
            }

            // Draw primary coordinate axes
            val screenOriginX = toScreenX(0.0)
            val screenOriginY = toScreenY(0.0)

            // X-Axis line
            if (screenOriginY in 0.0f..height) {
                drawLine(
                    color = axesColor.copy(alpha = 0.8f),
                    start = Offset(0f, screenOriginY),
                    end = Offset(width, screenOriginY),
                    strokeWidth = 2.5f
                )
                // draw X arrow
                drawLine(
                    color = axesColor.copy(alpha = 0.8f),
                    start = Offset(width - 15f, screenOriginY - 8f),
                    end = Offset(width, screenOriginY),
                    strokeWidth = 2f
                )
                drawLine(
                    color = axesColor.copy(alpha = 0.8f),
                    start = Offset(width - 15f, screenOriginY + 8f),
                    end = Offset(width, screenOriginY),
                    strokeWidth = 2f
                )
            }

            // Y-Axis line
            if (screenOriginX in 0.0f..width) {
                drawLine(
                    color = axesColor.copy(alpha = 0.8f),
                    start = Offset(screenOriginX, 0f),
                    end = Offset(screenOriginX, height),
                    strokeWidth = 2.5f
                )
                // draw Y arrow
                drawLine(
                    color = axesColor.copy(alpha = 0.8f),
                    start = Offset(screenOriginX - 8f, 15f),
                    end = Offset(screenOriginX, 0f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = axesColor.copy(alpha = 0.8f),
                    start = Offset(screenOriginX + 8f, 15f),
                    end = Offset(screenOriginX, 0f),
                    strokeWidth = 2f
                )
            }

            // Plot Equation 1 (if not empty)
            if (expression1.trim().isNotEmpty()) {
                val path1 = Path()
                var isPathStarted = false
                val steps = 350
                val dx = (xMax - xMin) / steps

                for (s in 0..steps) {
                    val currX = xMin + s * dx
                    val currY = MathEngine.evaluate(expression1, xVal = currX)

                    if (currY.isNaN() || currY.isInfinite()) {
                        isPathStarted = false
                        continue
                    }

                    val px = toScreenX(currX)
                    val py = toScreenY(currY)

                    // Clip to drawing area + small tolerances
                    if (py < -50 || py > height + 50) {
                        isPathStarted = false
                        continue
                    }

                    if (!isPathStarted) {
                        path1.moveTo(px, py)
                        isPathStarted = true
                    } else {
                        path1.lineTo(px, py)
                    }
                }
                drawPath(
                    path = path1,
                    color = line1Color,
                    style = Stroke(width = 4.dp.toPx())
                )
            }

            // Plot Equation 2 (if not empty)
            if (expression2.trim().isNotEmpty()) {
                val path2 = Path()
                var isPathStarted = false
                val steps = 350
                val dx = (xMax - xMin) / steps

                for (s in 0..steps) {
                    val currX = xMin + s * dx
                    val currY = MathEngine.evaluate(expression2, xVal = currX)

                    if (currY.isNaN() || currY.isInfinite()) {
                        isPathStarted = false
                        continue
                    }

                    val px = toScreenX(currX)
                    val py = toScreenY(currY)

                    if (py < -50 || py > height + 50) {
                        isPathStarted = false
                        continue
                    }

                    if (!isPathStarted) {
                        path2.moveTo(px, py)
                        isPathStarted = true
                    } else {
                        path2.lineTo(px, py)
                    }
                }
                drawPath(
                    path = path2,
                    color = line2Color,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }

        // Draw axis endpoints label tags for better context
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Text(
                text = "${formatLabel(yMax)}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            Text(
                text = "${formatLabel(yMin)}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            Text(
                text = "${formatLabel(xMin)}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(
                text = "${formatLabel(xMax)}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
fun OdeSlopeFieldPlotter(
    modifier: Modifier = Modifier,
    odeExpr: String,
    initialX: Double,
    initialY: Double,
    odePoints: List<PointD>,
    xMin: Double,
    xMax: Double,
    yMin: Double,
    yMax: Double,
    curveColor: Color = Color(0xFFFF9100), // Rich blazing orange for solved curve
    fieldColor: Color = Color(0xFF455A64), // Accent color for slope ticks
    axesColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F151B)) // Deep scientific dark layout
            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width <= 0 || height <= 0 || xMin >= xMax || yMin >= yMax) return@Canvas

            fun toScreenX(x: Double): Float {
                return ((x - xMin) / (xMax - xMin) * width).toFloat()
            }

            fun toScreenY(y: Double): Float {
                return (height - (y - yMin) / (yMax - yMin) * height).toFloat()
            }

            // Draw basic math axes
            val sOriginX = toScreenX(0.0)
            val sOriginY = toScreenY(0.0)

            if (sOriginY in 0f..height) {
                drawLine(
                    color = axesColor.copy(alpha = 0.5f),
                    start = Offset(0f, sOriginY),
                    end = Offset(width, sOriginY),
                    strokeWidth = 2f
                )
            }
            if (sOriginX in 0f..width) {
                drawLine(
                    color = axesColor.copy(alpha = 0.5f),
                    start = Offset(sOriginX, 0f),
                    end = Offset(sOriginX, height),
                    strokeWidth = 2f
                )
            }

            // Draw slope field segments background
            val segments = MathEngine.generateSlopeField(
                odeExpr, xMin, xMax, yMin, yMax, gridSize = 14
            )
            for (segment in segments) {
                val startPx = Offset(toScreenX(segment.first.x), toScreenY(segment.first.y))
                val endPx = Offset(toScreenX(segment.second.x), toScreenY(segment.second.y))

                // draw a nice glowing slope arrow/line
                drawLine(
                    color = fieldColor,
                    start = startPx,
                    end = endPx,
                    strokeWidth = 2.5f
                )
            }

            // Draw initial condition dot
            val dotX = toScreenX(initialX)
            val dotY = toScreenY(initialY)
            if (dotX in 0f..width && dotY in 0f..height) {
                drawCircle(
                    color = curveColor,
                    radius = 7.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
                // concentric shadow/glow ring
                drawCircle(
                    color = curveColor.copy(alpha = 0.4f),
                    radius = 12.dp.toPx(),
                    center = Offset(dotX, dotY),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Draw integration curve path
            if (odePoints.isNotEmpty()) {
                val curvePath = Path()
                var isStarted = false

                for (pt in odePoints) {
                    val px = toScreenX(pt.x)
                    val py = toScreenY(pt.y)

                    if (px < -50 || px > width + 50 || py < -50 || py > height + 50) {
                        isStarted = false
                        continue
                    }

                    if (!isStarted) {
                        curvePath.moveTo(px, py)
                        isStarted = true
                    } else {
                        curvePath.lineTo(px, py)
                    }
                }
                drawPath(
                    path = curvePath,
                    color = curveColor,
                    style = Stroke(width = 5.dp.toPx())
                )
            }
        }

        // Draw HUD legends
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Text(
                text = "${formatLabel(yMax)}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            Text(
                text = "${formatLabel(yMin)}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            Text(
                text = "${formatLabel(xMin)}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(
                text = "${formatLabel(xMax)}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

private fun formatLabel(value: Double): String {
    return if (Math.abs(value) < 1e-5) {
        "0"
    } else if (Math.abs(value - value.roundToInt()) < 1e-9) {
        value.roundToInt().toString()
    } else {
        String.format("%.1f", value).replace(",", ".")
    }
}
