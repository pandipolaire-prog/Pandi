package com.example.math

import kotlin.math.*

data class PointD(val x: Double, val y: Double) {
    fun isValid(): Boolean = !x.isNaN() && !x.isInfinite() && !y.isNaN() && !y.isInfinite()
}

/**
 * Robust mathematical parsing engine using a recursive descent parser.
 * It evaluates any valid alphanumeric mathematical expression involving variables x, y, and t.
 */
class MathParser(private val expression: String) {
    private var pos = -1
    private var ch = 0

    private fun nextChar() {
        pos++
        ch = if (pos < expression.length) expression[pos].code else -1
    }

    private fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    /**
     * Parses the current expression with specific values for x, y, and t parameters.
     */
    fun parse(xVal: Double = 0.0, yVal: Double = 0.0, tVal: Double = 0.0): Double {
        nextChar()
        val result = parseExpression(xVal, yVal, tVal)
        if (pos < expression.length) {
            throw IllegalArgumentException("Caractère inattendu : ${ch.toChar()} à la position $pos")
        }
        return result
    }

    // expression = term | expression `+` term | expression `-` term
    private fun parseExpression(xVal: Double, yVal: Double, tVal: Double): Double {
        var x = parseTerm(xVal, yVal, tVal)
        while (true) {
            if (eat('+'.code)) {
                x += parseTerm(xVal, yVal, tVal)
            } else if (eat('-'.code)) {
                x -= parseTerm(xVal, yVal, tVal)
            } else {
                return x
            }
        }
    }

    // term = factor | term `*` factor | term `/` factor
    private fun parseTerm(xVal: Double, yVal: Double, tVal: Double): Double {
        var x = parseFactor(xVal, yVal, tVal)
        while (true) {
            if (eat('*'.code)) {
                x *= parseFactor(xVal, yVal, tVal)
            } else if (eat('/'.code)) {
                val d = parseFactor(xVal, yVal, tVal)
                x = if (d != 0.0) x / d else Double.NaN
            } else {
                return x
            }
        }
    }

    // factor = `+` factor | `-` factor | `(` expression `)` | number | functionName `(` expression `)` | factor `^` factor
    private fun parseFactor(xVal: Double, yVal: Double, tVal: Double): Double {
        if (eat('+'.code)) return parseFactor(xVal, yVal, tVal) // unary plus
        if (eat('-'.code)) return -parseFactor(xVal, yVal, tVal) // unary minus

        var x: Double
        val startPos = this.pos
        if (eat('('.code)) { // parentheses
            x = parseExpression(xVal, yVal, tVal)
            if (!eat(')'.code)) throw IllegalArgumentException("Parenthèse fermante manquante")
        } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // numbers
            while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
            val numStr = expression.substring(startPos, this.pos)
            x = numStr.toDoubleOrNull() ?: Double.NaN
        } else if ((ch >= 'a'.code && ch <= 'z'.code) || ch == 'π'.code || ch == 'e'.code) { // variables, constants or functions
            while ((ch >= 'a'.code && ch <= 'z'.code) || ch == 'π'.code || ch == 'e'.code) nextChar()
            val name = expression.substring(startPos, this.pos)
            if (name == "x") {
                x = xVal
            } else if (name == "y") {
                x = yVal
            } else if (name == "t") {
                x = tVal
            } else if (name == "pi" || name == "π") {
                x = PI
            } else if (name == "e") {
                x = E
            } else if (eat('('.code)) {
                val arg = parseExpression(xVal, yVal, tVal)
                x = when (name) {
                    "sin" -> sin(arg)
                    "cos" -> cos(arg)
                    "tan" -> if (abs(cos(arg)) < 1e-10) Double.NaN else tan(arg)
                    "sqrt" -> if (arg >= 0.0) sqrt(arg) else Double.NaN
                    "ln" -> if (arg > 0.0) ln(arg) else Double.NaN
                    "log" -> if (arg > 0.0) log10(arg) else Double.NaN
                    "exp" -> exp(arg)
                    "abs" -> abs(arg)
                    "asin" -> asin(arg)
                    "acos" -> acos(arg)
                    "atan" -> atan(arg)
                    "sinh" -> sinh(arg)
                    "cosh" -> cosh(arg)
                    "tanh" -> tanh(arg)
                    else -> throw IllegalArgumentException("Fonction inconnue : $name")
                }
                if (!eat(')'.code)) throw IllegalArgumentException("Parenthèse fermante manquante après la fonction $name")
            } else {
                throw IllegalArgumentException("Variable ou constante inconnue : $name")
            }
        } else {
            throw IllegalArgumentException("Caractère inattendu : ${ch.toChar()} à la position $pos")
        }

        if (eat('^'.code)) {
            val power = parseFactor(xVal, yVal, tVal)
            x = if (x < 0.0 && power != round(power)) Double.NaN else x.pow(power)
        }

        return x
    }
}

object MathEngine {
    /**
     * Sanitizes and inserts multiplication characters into shorthand expressions
     * e.g., "2x" -> "2*x", "3(x)" -> "3*(x)", "x(x+1)" -> "x*(x+1)"
     */
    fun sanitizeExpression(expr: String): String {
        var formatted = expr.lowercase()
            .trim()
            .replace(" ", "")
            .replace("π", "pi")

        // 1. Digital character followed by variable or function name
        // e.g. "2x" -> "2*x", "3sin" -> "3*sin", "2pi" -> "2*pi"
        formatted = formatted.replace(Regex("(\\d+)([a-zπ])"), "$1*$2")

        // 2. Letter and then digit
        // e.g. "x2" -> "x*2" (but we allow normal numbers like log10, ln, etc. carefully)
        // This is safe if it is just a plain x, y or t being followed by digits:
        formatted = formatted.replace(Regex("([xyt])(\\d+)"), "$1*$2")

        // 3. Digit followed by opening parenthesis
        // e.g. "2(3+5)" -> "2*(3+5)"
        formatted = formatted.replace(Regex("(\\d+)(\\()"), "$1*$2")

        // 4. Closing parenthesis followed by letter, digit, or opening parenthesis
        // e.g. "(x)(y)" -> "(x)*(y)", "(x)2" -> "(x)*2", "(x)sin" -> "(x)*sin"
        formatted = formatted.replace(Regex("(\\))([a-z0-9π\\(])"), "$1*$2")

        // 5. Standalone variable followed by opening parenthesis
        // e.g. "x(x+1)" -> "x*(x+1)"
        formatted = formatted.replace(Regex("(?<=\\b)(x|y|t|e|pi)(\\()"), "$1*$2")

        return formatted
    }

    /**
     * Evaluates an expression with a given variable value. Returns Double.NaN if error occurs.
     */
    fun evaluate(expr: String, xVal: Double = 0.0, yVal: Double = 0.0, tVal: Double = 0.0): Double {
        return try {
            val sanitized = sanitizeExpression(expr)
            if (sanitized.isEmpty()) return Double.NaN
            MathParser(sanitized).parse(xVal, yVal, tVal)
        } catch (e: Exception) {
            Double.NaN
        }
    }

    /**
     * Solves the ordinary differential equation dy/dt = f(t, y) or dy/dx = f(x, y)
     * using the 4th Order Runge-Kutta numerical integration model.
     * Starts at (x0, y0), integrating forward to xMax dynamic step size.
     */
    fun solveODE(
        expression: String,
        x0: Double,
        y0: Double,
        xMax: Double,
        xMin: Double,
        steps: Int = 300
    ): List<PointD> {
        val points = mutableListOf<PointD>()
        val sanitized = sanitizeExpression(expression)

        // Evaluate RK4 forward
        if (xMax > x0) {
            val hForward = (xMax - x0) / steps
            var xCur = x0
            var yCur = y0
            points.add(PointD(xCur, yCur))
            for (i in 0 until steps) {
                val next = stepRK4(sanitized, xCur, yCur, hForward)
                xCur = next.x
                yCur = next.y
                if (!next.isValid()) break
                points.add(next)
            }
        }

        // Evaluate RK4 backward
        val backwardPoints = mutableListOf<PointD>()
        if (xMin < x0) {
            val hBackward = (xMin - x0) / steps
            var xCur = x0
            var yCur = y0
            for (i in 0 until steps) {
                val next = stepRK4(sanitized, xCur, yCur, hBackward)
                xCur = next.x
                yCur = next.y
                if (!next.isValid()) break
                backwardPoints.add(next)
            }
        }

        // Return sorted unified path
        return (backwardPoints.reversed() + points)
    }

    private fun stepRK4(expr: String, x: Double, y: Double, h: Double): PointD {
        val k1 = evaluate(expr, xVal = x, yVal = y, tVal = x) // we bind both x & t in case user uses either
        val k2 = evaluate(expr, xVal = x + 0.5 * h, yVal = y + 0.5 * h * k1, tVal = x + 0.5 * h)
        val k3 = evaluate(expr, xVal = x + 0.5 * h, yVal = y + 0.5 * h * k2, tVal = x + 0.5 * h)
        val k4 = evaluate(expr, xVal = x + h, yVal = y + h * k3, tVal = x + h)

        if (k1.isNaN() || k2.isNaN() || k3.isNaN() || k4.isNaN()) return PointD(Double.NaN, Double.NaN)

        val nextY = y + (h / 6.0) * (k1 + 2 * k2 + 2 * k3 + k4)
        val nextX = x + h
        return PointD(nextX, nextY)
    }

    /**
     * Generates slopes for standard 2D vector / slope field drawing.
     * Returns a grid of line segment vectors defined by their start and end points.
     */
    fun generateSlopeField(
        expression: String,
        xMin: Double,
        xMax: Double,
        yMin: Double,
        yMax: Double,
        gridSize: Int = 14
    ): List<Pair<PointD, PointD>> {
        val segments = mutableListOf<Pair<PointD, PointD>>()
        val sanitized = sanitizeExpression(expression)

        val xStep = (xMax - xMin) / (gridSize + 1)
        val yStep = (yMax - yMin) / (gridSize + 1)

        val lengthFactor = min(xStep, yStep) * 0.4 // scale slope segments so they don't overlap too much

        for (i in 1..gridSize) {
            val x = xMin + i * xStep
            for (j in 1..gridSize) {
                val y = yMin + j * yStep
                val slope = evaluate(sanitized, xVal = x, yVal = y, tVal = x)
                if (slope.isNaN() || slope.isInfinite()) continue

                // Slope angle theta = atan(slope)
                val theta = atan(slope)
                val dx = cos(theta) * lengthFactor
                val dy = sin(theta) * lengthFactor

                val start = PointD(x - dx, y - dy)
                val end = PointD(x + dx, y + dy)
                segments.add(Pair(start, end))
            }
        }
        return segments
    }
}
