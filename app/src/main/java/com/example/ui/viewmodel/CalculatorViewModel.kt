package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.HistoryEntity
import com.example.data.repository.HistoryRepository
import com.example.math.MathEngine
import com.example.math.PointD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CalculatorUiState {
    data class Active(
        val calcInput: String = "",
        val calcResult: String = "",
        val calcErrorMessage: String? = null,
        
        // Graphing parameters
        val graphExpr1: String = "sin(x) * cos(x/2)",
        val graphExpr2: String = "cos(x)",
        val graphXMin: Double = -10.0,
        val graphXMax: Double = 10.0,
        val graphYMin: Double = -5.0,
        val graphYMax: Double = 5.0,
        
        // ODE parameters
        val odeExpr: String = "x - y", // representation of dy/dx = x - y
        val odeX0: Double = 0.0,
        val odeY0: Double = 1.0,
        val odeXMin: Double = -5.0,
        val odeXMax: Double = 5.0,
        val odeYMin: Double = -5.0,
        val odeYMax: Double = 5.0,
        val odePoints: List<PointD> = emptyList(),
        val odesError: String? = null
    ) : CalculatorUiState
}

class CalculatorViewModel(private val repository: HistoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState.Active())
    val uiState: StateFlow<CalculatorUiState.Active> = _uiState.asStateFlow()

    // Realtime Database history
    val historyList: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Compute initial ODE solution curves
        recomputeOde()
    }

    // --- Calculator Operations ---

    fun onCalcInputChanged(newInput: String) {
        _uiState.value = _uiState.value.copy(
            calcInput = newInput,
            calcErrorMessage = null
        )
    }

    fun appendCalcToken(token: String) {
        val current = _uiState.value.calcInput
        val updated = current + token
        _uiState.value = _uiState.value.copy(
            calcInput = updated,
            calcErrorMessage = null
        )
    }

    fun clearCalc() {
        _uiState.value = _uiState.value.copy(
            calcInput = "",
            calcResult = "",
            calcErrorMessage = null
        )
    }

    fun backspaceCalc() {
        val current = _uiState.value.calcInput
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                calcInput = current.substring(0, current.length - 1),
                calcErrorMessage = null
            )
        }
    }

    fun evaluateCalc() {
        val state = _uiState.value
        val expr = state.calcInput
        if (expr.isEmpty()) return

        val result = MathEngine.evaluate(expr)
        if (result.isNaN() || result.isInfinite()) {
            _uiState.value = state.copy(
                calcResult = "Erreur",
                calcErrorMessage = "Expression mathématique invalide"
            )
        } else {
            val formattedResult = formatDouble(result)
            _uiState.value = state.copy(
                calcResult = formattedResult,
                calcErrorMessage = null
            )
            // Auto save to database
            saveCalculationHistory(expr, formattedResult)
        }
    }

    private fun formatDouble(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            // Rounded to 6 decimal places for scientific precision
            String.format("%.6f", value)
                .replace(",", ".")
                .trimEnd('0')
                .trimEnd('.')
        }
    }

    // --- Graphing Operations ---

    fun updateGraphBounds(xMin: Double, xMax: Double, yMin: Double, yMax: Double) {
        if (xMin >= xMax || yMin >= yMax) return
        _uiState.value = _uiState.value.copy(
            graphXMin = xMin,
            graphXMax = xMax,
            graphYMin = yMin,
            graphYMax = yMax
        )
    }

    fun onGraphExpr1Changed(expr: String) {
        _uiState.value = _uiState.value.copy(graphExpr1 = expr)
    }

    fun onGraphExpr2Changed(expr: String) {
        _uiState.value = _uiState.value.copy(graphExpr2 = expr)
    }

    fun saveCurrentGraph() {
        val state = _uiState.value
        viewModelScope.launch {
            repository.insert(
                HistoryEntity(
                    type = "GRAPH",
                    expression = state.graphExpr1,
                    secondaryExpression = state.graphExpr2,
                    initialX = state.graphXMin,
                    initialY = state.graphXMax
                )
            )
        }
    }

    // --- ODE Operations ---

    fun onOdeExprChanged(expr: String) {
        _uiState.value = _uiState.value.copy(odeExpr = expr)
        recomputeOde()
    }

    fun onOdeInitialStatesChanged(x0: Double, y0: Double) {
        _uiState.value = _uiState.value.copy(odeX0 = x0, odeY0 = y0)
        recomputeOde()
    }

    fun updateOdeBounds(xMin: Double, xMax: Double, yMin: Double, yMax: Double) {
        if (xMin >= xMax || yMin >= yMax) return
        _uiState.value = _uiState.value.copy(
            odeXMin = xMin,
            odeXMax = xMax,
            odeYMin = yMin,
            odeYMax = yMax
        )
        recomputeOde()
    }

    fun recomputeOde() {
        val state = _uiState.value
        try {
            // Solve using math engine RK4 curves
            val pts = MathEngine.solveODE(
                expression = state.odeExpr,
                x0 = state.odeX0,
                y0 = state.odeY0,
                xMax = state.odeXMax,
                xMin = state.odeXMin,
                steps = 400
            )
            _uiState.value = state.copy(
                odePoints = pts,
                odesError = null
            )
        } catch (e: Exception) {
            _uiState.value = state.copy(
                odePoints = emptyList(),
                odesError = "Erreur de résolution"
            )
        }
    }

    fun saveCurrentOde() {
        val state = _uiState.value
        viewModelScope.launch {
            repository.insert(
                HistoryEntity(
                    type = "ODE",
                    expression = state.odeExpr,
                    secondaryExpression = null,
                    initialX = state.odeX0,
                    initialY = state.odeY0
                )
            )
        }
    }

    // --- Local Persistence / History Operations ---

    fun saveCalculationHistory(input: String, output: String) {
        viewModelScope.launch {
            repository.insert(
                HistoryEntity(
                    type = "CALC",
                    expression = input,
                    resultText = output
                )
            )
        }
    }

    fun loadHistoryItem(item: HistoryEntity) {
        val current = _uiState.value
        when (item.type) {
            "CALC" -> {
                _uiState.value = current.copy(
                    calcInput = item.expression,
                    calcResult = item.resultText ?: "",
                    calcErrorMessage = null
                )
            }
            "GRAPH" -> {
                _uiState.value = current.copy(
                    graphExpr1 = item.expression,
                    graphExpr2 = item.secondaryExpression ?: "",
                    graphXMin = item.initialX ?: -10.0,
                    graphXMax = item.initialY ?: 10.0
                )
            }
            "ODE" -> {
                _uiState.value = current.copy(
                    odeExpr = item.expression,
                    odeX0 = item.initialX ?: 0.0,
                    odeY0 = item.initialY ?: 1.0
                )
                recomputeOde()
            }
        }
    }

    fun deleteHistoryItem(item: HistoryEntity) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}

class CalculatorViewModelFactory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
