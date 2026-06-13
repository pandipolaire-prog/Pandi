package com.example

import com.example.math.MathEngine
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testSimpleArithmeticParsing() {
    // Basic math operations
    val r1 = MathEngine.evaluate("2 + 3 * 4")
    assertEquals(14.0, r1, 1e-9)

    val r2 = MathEngine.evaluate("(2 + 3) * 4")
    assertEquals(20.0, r2, 1e-9)

    // Scientific calculations
    val r3 = MathEngine.evaluate("sin(pi / 2)")
    assertEquals(1.0, r3, 1e-9)

    val r4 = MathEngine.evaluate("sqrt(16) + ln(e)")
    assertEquals(5.0, r4, 1e-9)
  }

  @Test
  fun testImplicitMultiplicationSanitization() {
    // Tests: "2x" => "2*x"
    val val1 = MathEngine.evaluate("2x", xVal = 5.0)
    assertEquals(10.0, val1, 1e-9)

    // Tests: "x(x+1)" => "x*(x+1)" at x=3
    val val2 = MathEngine.evaluate("x(x+1)", xVal = 3.0)
    assertEquals(12.0, val2, 1e-9)

    // Tests: "2(3+5)" => "2*(3+5)"
    val val3 = MathEngine.evaluate("2(3+5)")
    assertEquals(16.0, val3, 1e-9)
  }

  @Test
  fun testOdeSolverRK4() {
    // Solve dy/dx = -x starting at (0, 4) up to x = 2
    // Exact analytical solution: y(x) = 4 - 0.5 * x^2
    // At x=2, y(2) = 4 - 2 = 2.0
    val points = MathEngine.solveODE(
      expression = "-x",
      x0 = 0.0,
      y0 = 4.0,
      xMax = 2.0,
      xMin = 0.0,
      steps = 100
    )

    assertTrue(points.isNotEmpty())
    val lastPoint = points.last()
    assertEquals(2.0, lastPoint.x, 1e-3)
    assertEquals(2.0, lastPoint.y, 1e-2) // RK4 is highly accurate, easily matches with generous delta
  }
}
