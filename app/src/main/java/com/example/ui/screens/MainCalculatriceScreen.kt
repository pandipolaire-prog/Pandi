package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.HistoryEntity
import com.example.data.repository.HistoryRepository
import com.example.math.MathEngine
import com.example.ui.components.FunctionGraphPlotter
import com.example.ui.components.OdeSlopeFieldPlotter
import com.example.ui.viewmodel.CalculatorUiState
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.CalculatorViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCalculatriceApp(
    repository: HistoryRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: CalculatorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CalculatorViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) }
    var showConfirmClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.ui.theme.SleekBg,
                    titleContentColor = com.example.ui.theme.SleekOnBg
                ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = null,
                            tint = com.example.ui.theme.SleekPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "GraphCalc Pro",
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = com.example.ui.theme.SleekOnBg,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "Calculs • Graphes • Équations Différentielles",
                                fontSize = 11.sp,
                                color = com.example.ui.theme.SleekTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Moteur de calcul RK4 / parser mathématique autonome v1.0",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.SleekDarkContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Informations de l'application",
                            tint = com.example.ui.theme.SleekPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = com.example.ui.theme.SleekBg,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculs") },
                    label = { Text("Calculs", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.SleekPrimary,
                        selectedTextColor = com.example.ui.theme.SleekPrimary,
                        indicatorColor = com.example.ui.theme.SleekDarkContainer,
                        unselectedIconColor = com.example.ui.theme.SleekTextSecondary,
                        unselectedTextColor = com.example.ui.theme.SleekTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Graphes") },
                    label = { Text("Graphes", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.SleekPrimary,
                        selectedTextColor = com.example.ui.theme.SleekPrimary,
                        indicatorColor = com.example.ui.theme.SleekDarkContainer,
                        unselectedIconColor = com.example.ui.theme.SleekTextSecondary,
                        unselectedTextColor = com.example.ui.theme.SleekTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Éq. Diff") },
                    label = { Text("Éq. Diff", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.SleekPrimary,
                        selectedTextColor = com.example.ui.theme.SleekPrimary,
                        indicatorColor = com.example.ui.theme.SleekDarkContainer,
                        unselectedIconColor = com.example.ui.theme.SleekTextSecondary,
                        unselectedTextColor = com.example.ui.theme.SleekTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historique") },
                    label = { Text("Historique", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.example.ui.theme.SleekPrimary,
                        selectedTextColor = com.example.ui.theme.SleekPrimary,
                        indicatorColor = com.example.ui.theme.SleekDarkContainer,
                        unselectedIconColor = com.example.ui.theme.SleekTextSecondary,
                        unselectedTextColor = com.example.ui.theme.SleekTextSecondary
                    )
                )
            }
        },
        containerColor = com.example.ui.theme.SleekBg // Rich dark aesthetic background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> CalculatorTabContent(
                    state = uiState,
                    viewModel = viewModel
                )
                1 -> GraphingTabContent(
                    state = uiState,
                    viewModel = viewModel
                )
                2 -> OdeTabContent(
                    state = uiState,
                    viewModel = viewModel
                )
                3 -> HistoryTabContent(
                    history = historyList,
                    onLoadItem = { item ->
                        viewModel.loadHistoryItem(item)
                        activeTab = when (item.type) {
                            "CALC" -> 0
                            "GRAPH" -> 1
                            "ODE" -> 2
                            else -> 0
                        }
                        Toast.makeText(context, "Élément chargé avec succès", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteItem = { viewModel.deleteHistoryItem(it) },
                    onClearAll = { showConfirmClearDialog = true }
                )
            }
        }
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = { Text("Effacer l'historique ?") },
            text = { Text("Voulez-vous supprimer définitivement toutes vos formules et calculs sauvegardés ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showConfirmClearDialog = false
                        Toast.makeText(context, "Historique vidé", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Oui, tout effacer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

// ======================== TAB CONTENT 1: CALCULATOR ========================

@Composable
fun CalculatorTabContent(
    state: CalculatorUiState.Active,
    viewModel: CalculatorViewModel
) {
    // Dynamic color pairing function according to the "Sleek Interface" design layout requirements
    fun getButtonColors(label: String): Pair<Color, Color> {
        return when (label) {
            "sin(", "cos(", "tan(", "log(", "ln(" -> Pair(com.example.ui.theme.SleekDarkContainer, com.example.ui.theme.SleekPrimary)
            "sqrt(", "(", ")", "^", "exp(", "π", "e" -> Pair(com.example.ui.theme.SleekMidContainer, com.example.ui.theme.SleekOnBg)
            "C", "⌫" -> Pair(com.example.ui.theme.SleekTertiary, com.example.ui.theme.SleekTextPink)
            "*", "/", "+", "-" -> Pair(com.example.ui.theme.SleekOperatorColor, com.example.ui.theme.SleekLightPurple)
            "=" -> Pair(com.example.ui.theme.SleekPrimary, com.example.ui.theme.SleekTextVibrantPurple)
            else -> Pair(com.example.ui.theme.SleekLightPurple, com.example.ui.theme.SleekTextDarkPurple) // Numbers 0-9 and .
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.SleekBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper screen displaying inputs and outputs
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f)
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.SleekSurface),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Live Input Row
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Saisie Mathématique",
                        color = com.example.ui.theme.SleekTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = state.calcInput.ifEmpty { "0" },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light,
                            color = com.example.ui.theme.SleekOnBg,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("calculator_screen_input")
                        )
                    }
                }

                // Divider line
                HorizontalDivider(color = com.example.ui.theme.SleekBg.copy(alpha = 0.6f))

                // Error Message or Evaluation Outcome
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    if (state.calcErrorMessage != null) {
                        Text(
                            text = state.calcErrorMessage,
                            color = com.example.ui.theme.SleekTertiary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.End,
                            fontFamily = FontFamily.Monospace
                        )
                    } else if (state.calcResult.isNotEmpty()) {
                        Text(
                            text = "= ${state.calcResult}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            color = com.example.ui.theme.SleekPrimary,
                            textAlign = TextAlign.End,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("calculator_screen_result")
                        )
                    }
                }
            }
        }

        // Lower Layout: Complete scientific custom keyboard using elegant rounded buttons
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(4.0f),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                val keySections = listOf(
                    listOf("sin(", "cos(", "tan(", "log(", "ln("),
                    listOf("sqrt(", "(", ")", "^", "exp("),
                    listOf("7", "8", "9", "⌫", "C"),
                    listOf("4", "5", "6", "*", "/"),
                    listOf("1", "2", "3", "+", "-"),
                    listOf("0", ".", "π", "e", "=")
                )

                for (row in keySections) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (label in row) {
                            val (btnBgColor, btnTextColor) = getButtonColors(label)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(btnBgColor)
                                    .clickable {
                                        when (label) {
                                            "C" -> viewModel.clearCalc()
                                            "⌫" -> viewModel.backspaceCalc()
                                            "=" -> viewModel.evaluateCalc()
                                            else -> viewModel.appendCalcToken(label)
                                        }
                                    }
                                    .testTag("calc_key_$label"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (label) {
                                        "sqrt(" -> "√"
                                        "sin(" -> "sin"
                                        "cos(" -> "cos"
                                        "tan(" -> "tan"
                                        "log(" -> "log"
                                        "ln(" -> "ln"
                                        "exp(" -> "exp"
                                        else -> label
                                    },
                                    fontSize = if (label.length > 2) 13.sp else 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = btnTextColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


data class CalculatorButtonModel(
    val label: String,
    val bgColor: Color,
    val isAction: Boolean = false,
    val isResult: Boolean = false
)

// ======================== TAB CONTENT 2: GRAPHING ========================

@Composable
fun GraphingTabContent(
    state: CalculatorUiState.Active,
    viewModel: CalculatorViewModel
) {
    var inputY1 by remember { mutableStateOf(state.graphExpr1) }
    var inputY2 by remember { mutableStateOf(state.graphExpr2) }

    // Synchronize inputs when overall viewModel State changes (loaded from history)
    LaunchedEffect(state.graphExpr1, state.graphExpr2) {
        inputY1 = state.graphExpr1
        inputY2 = state.graphExpr2
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Graphing Viewport
        FunctionGraphPlotter(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            expression1 = state.graphExpr1,
            expression2 = state.graphExpr2,
            xMin = state.graphXMin,
            xMax = state.graphXMax,
            yMin = state.graphYMin,
            yMax = state.graphYMax
        )

        // Configuration Control Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.SleekSurface),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Saisie des équations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputY1,
                        onValueChange = { inputY1 = it },
                        label = { Text("f(x)", color = com.example.ui.theme.SleekPrimary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.SleekPrimary,
                            unfocusedBorderColor = com.example.ui.theme.SleekPrimary.copy(alpha = 0.4f),
                            focusedLabelColor = com.example.ui.theme.SleekPrimary,
                            unfocusedContainerColor = com.example.ui.theme.SleekBg,
                            focusedContainerColor = com.example.ui.theme.SleekBg,
                            focusedTextColor = com.example.ui.theme.SleekOnBg,
                            unfocusedTextColor = com.example.ui.theme.SleekOnBg
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("graph_input_y1"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = inputY2,
                        onValueChange = { inputY2 = it },
                        label = { Text("g(x)", color = com.example.ui.theme.SleekSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.SleekSecondary,
                            unfocusedBorderColor = com.example.ui.theme.SleekSecondary.copy(alpha = 0.4f),
                            focusedLabelColor = com.example.ui.theme.SleekSecondary,
                            unfocusedContainerColor = com.example.ui.theme.SleekBg,
                            focusedContainerColor = com.example.ui.theme.SleekBg,
                            focusedTextColor = com.example.ui.theme.SleekOnBg,
                            unfocusedTextColor = com.example.ui.theme.SleekOnBg
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("graph_input_y2"),
                        singleLine = true
                    )
                }

                // Dynamic buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.onGraphExpr1Changed(inputY1)
                            viewModel.onGraphExpr2Changed(inputY2)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.ui.theme.SleekDarkContainer,
                            contentColor = com.example.ui.theme.SleekPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_graph")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tracer")
                    }

                    Button(
                        onClick = {
                            viewModel.onGraphExpr1Changed(inputY1)
                            viewModel.onGraphExpr2Changed(inputY2)
                            viewModel.saveCurrentGraph()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.ui.theme.SleekPrimary,
                            contentColor = com.example.ui.theme.SleekTextVibrantPurple
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("save_graph")
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            tint = com.example.ui.theme.SleekTextVibrantPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sauvegarder", color = com.example.ui.theme.SleekTextVibrantPurple, fontWeight = FontWeight.Bold)
                    }
                }

                // Range limit HUD tools
                Text(
                    text = "Ajustement du Zoom de l'axe X :",
                    color = com.example.ui.theme.SleekTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Zoom Standard [-10, 10]
                    OutlinedButton(
                        onClick = {
                            viewModel.updateGraphBounds(-10.0, 10.0, -10.0, 10.0)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.SleekPrimary)
                    ) {
                        Text("Standard", fontSize = 11.sp)
                    }

                    // Zoom Trig [-2pi, 2pi]
                    OutlinedButton(
                        onClick = {
                            viewModel.updateGraphBounds(-2 * PI, 2 * PI, -2.0, 2.0)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.SleekPrimary)
                    ) {
                        Text("Trigo", fontSize = 11.sp)
                    }

                    // Linear custom sliders or zoom out
                    OutlinedButton(
                        onClick = {
                            val midX = (state.graphXMin + state.graphXMax) / 2
                            val halfRadX = (state.graphXMax - state.graphXMin) * 0.75
                            val midY = (state.graphYMin + state.graphYMax) / 2
                            val halfRadY = (state.graphYMax - state.graphYMin) * 0.75
                            viewModel.updateGraphBounds(
                                midX - halfRadX,
                                midX + halfRadX,
                                midY - halfRadY,
                                midY + halfRadY
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.8f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.SleekPrimary)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(16.dp))
                    }

                    OutlinedButton(
                        onClick = {
                            val midX = (state.graphXMin + state.graphXMax) / 2
                            val halfRadX = (state.graphXMax - state.graphXMin) * 1.5
                            val midY = (state.graphYMin + state.graphYMax) / 2
                            val halfRadY = (state.graphYMax - state.graphYMin) * 1.5
                            viewModel.updateGraphBounds(
                                midX - halfRadX,
                                midX + halfRadX,
                                midY - halfRadY,
                                midY + halfRadY
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.8f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.SleekPrimary)
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ======================== TAB CONTENT 3: DIFF EQUATIONS (ODEs) ========================

@Composable
fun OdeTabContent(
    state: CalculatorUiState.Active,
    viewModel: CalculatorViewModel
) {
    var inputOde by remember { mutableStateOf(state.odeExpr) }
    var inputX0 by remember { mutableStateOf(state.odeX0.toString()) }
    var inputY0 by remember { mutableStateOf(state.odeY0.toString()) }

    LaunchedEffect(state.odeExpr, state.odeX0, state.odeY0) {
        inputOde = state.odeExpr
        inputX0 = state.odeX0.toString()
        inputY0 = state.odeY0.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.SleekBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header explanation card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.SleekSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = com.example.ui.theme.SleekPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Équations Différentielles du 1er Ordre",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = com.example.ui.theme.SleekOnBg
                    )
                    Text(
                        text = "Saisissez dy/dx = f(x,y). Exemple : 'x - y' ou 'sin(x) - y'. Résolution par intégration numérique Runge-Kutta 4.",
                        fontSize = 11.sp,
                        color = com.example.ui.theme.SleekTextSecondary
                    )
                }
            }
        }

        // Plot Field view Canvas
        OdeSlopeFieldPlotter(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            odeExpr = state.odeExpr,
            initialX = state.odeX0,
            initialY = state.odeY0,
            odePoints = state.odePoints,
            xMin = state.odeXMin,
            xMax = state.odeXMax,
            yMin = state.odeYMin,
            yMax = state.odeYMax
        )

        // Control Inputs Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.SleekSurface),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row for main EDO expression
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputOde,
                        onValueChange = { inputOde = it },
                        label = { Text("dy/dx = f(x, y)", color = com.example.ui.theme.SleekPrimary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.SleekPrimary,
                            unfocusedBorderColor = com.example.ui.theme.SleekPrimary.copy(alpha = 0.4f),
                            focusedLabelColor = com.example.ui.theme.SleekPrimary,
                            unfocusedContainerColor = com.example.ui.theme.SleekBg,
                            focusedContainerColor = com.example.ui.theme.SleekBg,
                            focusedTextColor = com.example.ui.theme.SleekOnBg,
                            unfocusedTextColor = com.example.ui.theme.SleekOnBg
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ode_input_expr"),
                        singleLine = true
                    )
                }

                // Initial Condition parameters (x0, y0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputX0,
                        onValueChange = { inputX0 = it },
                        label = { Text("x₀", color = com.example.ui.theme.SleekOnBg) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.SleekPrimary,
                            unfocusedBorderColor = com.example.ui.theme.SleekPrimary.copy(alpha = 0.4f),
                            focusedLabelColor = com.example.ui.theme.SleekPrimary,
                            unfocusedContainerColor = com.example.ui.theme.SleekBg,
                            focusedContainerColor = com.example.ui.theme.SleekBg,
                            focusedTextColor = com.example.ui.theme.SleekOnBg,
                            unfocusedTextColor = com.example.ui.theme.SleekOnBg
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ode_input_x0"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputY0,
                        onValueChange = { inputY0 = it },
                        label = { Text("y₀ = y(x₀)", color = com.example.ui.theme.SleekOnBg) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.SleekPrimary,
                            unfocusedBorderColor = com.example.ui.theme.SleekPrimary.copy(alpha = 0.4f),
                            focusedLabelColor = com.example.ui.theme.SleekPrimary,
                            unfocusedContainerColor = com.example.ui.theme.SleekBg,
                            focusedContainerColor = com.example.ui.theme.SleekBg,
                            focusedTextColor = com.example.ui.theme.SleekOnBg,
                            unfocusedTextColor = com.example.ui.theme.SleekOnBg
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ode_input_y0"),
                        singleLine = true
                    )
                }

                // Process inputs row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val vX0 = inputX0.toDoubleOrNull() ?: 0.0
                            val vY0 = inputY0.toDoubleOrNull() ?: 1.0
                            viewModel.onOdeExprChanged(inputOde)
                            viewModel.onOdeInitialStatesChanged(vX0, vY0)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.ui.theme.SleekDarkContainer,
                            contentColor = com.example.ui.theme.SleekPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_ode")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Actualiser")
                    }

                    Button(
                        onClick = {
                            val vX0 = inputX0.toDoubleOrNull() ?: 0.0
                            val vY0 = inputY0.toDoubleOrNull() ?: 1.0
                            viewModel.onOdeExprChanged(inputOde)
                            viewModel.onOdeInitialStatesChanged(vX0, vY0)
                            viewModel.saveCurrentOde()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.ui.theme.SleekPrimary,
                            contentColor = com.example.ui.theme.SleekTextVibrantPurple
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("save_ode")
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            tint = com.example.ui.theme.SleekTextVibrantPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sauvegarder", color = com.example.ui.theme.SleekTextVibrantPurple, fontWeight = FontWeight.Bold)
                    }
                }

                // Range limitations
                Text(
                    text = "Ajustement du Zoom EDO :",
                    color = com.example.ui.theme.SleekTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.updateOdeBounds(-5.0, 5.0, -5.0, 5.0) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.SleekPrimary)
                    ) {
                        Text("Zoom x5", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.updateOdeBounds(-2.0, 2.0, -2.0, 2.0) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.SleekPrimary)
                    ) {
                        Text("Zoom x2", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.updateOdeBounds(-10.0, 10.0, -10.0, 10.0) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.SleekPrimary)
                    ) {
                        Text("Zoom x10", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ======================== TAB CONTENT 4: HISTORY ========================

@Composable
fun HistoryTabContent(
    history: List<HistoryEntity>,
    onLoadItem: (HistoryEntity) -> Unit,
    onDeleteItem: (HistoryEntity) -> Unit,
    onClearAll: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.SleekBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Historique local",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = com.example.ui.theme.SleekOnBg
                )
                Text(
                    text = "Consultez, rechargez ou supprimez vos requêtes.",
                    fontSize = 12.sp,
                    color = com.example.ui.theme.SleekTextSecondary
                )
            }

            if (history.isNotEmpty()) {
                IconButton(
                    onClick = onClearAll,
                    modifier = Modifier.testTag("history_clear_all_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Tout supprimer de l'historique",
                        tint = com.example.ui.theme.SleekTertiary
                    )
                }
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = com.example.ui.theme.SleekDarkContainer,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Aucune formule enregistrée",
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.SleekTextSecondary,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Faites des calculs ou enregistrez des fonctions pour les retrouver ici.",
                        color = com.example.ui.theme.SleekTextSecondary.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("history_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLoadItem(item) }
                            .testTag("history_item_${item.id}"),
                        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.SleekSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val (chipText, chipColor) = when (item.type) {
                                        "CALC" -> Pair("Calcul", com.example.ui.theme.SleekPrimary)
                                        "GRAPH" -> Pair("Graphe", com.example.ui.theme.SleekSecondary)
                                        "ODE" -> Pair("Éq. Diff (EDO)", com.example.ui.theme.SleekTertiary)
                                        else -> Pair("Formule", Color.White)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(chipColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = chipText,
                                            color = chipColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = formatter.format(Date(item.timestamp)),
                                        color = com.example.ui.theme.SleekTextSecondary,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                when (item.type) {
                                    "CALC" -> {
                                        Text(
                                            text = item.expression,
                                            color = com.example.ui.theme.SleekOnBg,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (item.resultText != null) {
                                            Text(
                                                text = "= ${item.resultText}",
                                                color = com.example.ui.theme.SleekPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    "GRAPH" -> {
                                        Text(
                                            text = "f(x) = ${item.expression}",
                                            color = com.example.ui.theme.SleekOnBg,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        if (item.secondaryExpression != null && item.secondaryExpression.isNotEmpty()) {
                                            Text(
                                                text = "g(x) = ${item.secondaryExpression}",
                                                color = com.example.ui.theme.SleekSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    "ODE" -> {
                                        Text(
                                            text = "y' = ${item.expression}",
                                            color = com.example.ui.theme.SleekOnBg,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Cond. Initiale : y(${item.initialX}) = ${item.initialY}",
                                            color = com.example.ui.theme.SleekTertiary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onDeleteItem(item) },
                                modifier = Modifier.testTag("delete_history_item_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer de l'historique",
                                    tint = com.example.ui.theme.SleekTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
