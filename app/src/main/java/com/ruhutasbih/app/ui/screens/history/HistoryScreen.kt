package com.ruhutasbih.app.ui.screens.history

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhutasbih.app.R
import com.ruhutasbih.app.ui.components.StatCard
import com.ruhutasbih.app.ui.theme.ArabicGoldText
import com.ruhutasbih.app.ui.theme.PendingBadgeOrange
import com.ruhutasbih.app.ui.theme.TargetProgressGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePickerModal by remember { mutableStateOf(false) }

    var showMonthDropdown by remember { mutableStateOf(false) }
    var showYearDropdown by remember { mutableStateOf(false) }
    var isCurveChartStyle by remember { mutableStateOf(true) }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val tabs = listOf(
        stringResource(R.string.tab_daily),
        stringResource(R.string.tab_weekly),
        stringResource(R.string.tab_monthly),
        stringResource(R.string.tab_yearly)
    )

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = uiState.selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }

            // Custom Selector Bar per Tab
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                if (uiState.selectedTab == 0 || uiState.selectedTab == 1) {
                    // Daily & Weekly Date Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.selectPreviousDay() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Day"
                            )
                        }

                        OutlinedButton(
                            onClick = { showDatePickerModal = true },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Select Date",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.selectedDailyDate,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!uiState.isTodaySelected) {
                                IconButton(onClick = { viewModel.selectToday() }) {
                                    Icon(
                                        imageVector = Icons.Default.Today,
                                        contentDescription = "Jump to Today",
                                        tint = ArabicGoldText
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.selectNextDay() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Day"
                                )
                            }
                        }
                    }
                } else if (uiState.selectedTab == 2) {
                    // Monthly Custom Month & Year Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Month Dropdown
                        Box {
                            OutlinedButton(
                                onClick = { showMonthDropdown = true },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(monthNames.getOrElse(uiState.selectedMonthlyMonth - 1) { "Month" })
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Month")
                            }
                            DropdownMenu(
                                expanded = showMonthDropdown,
                                onDismissRequest = { showMonthDropdown = false }
                            ) {
                                monthNames.forEachIndexed { index, mName ->
                                    DropdownMenuItem(
                                        text = { Text(mName) },
                                        onClick = {
                                            showMonthDropdown = false
                                            viewModel.onMonthlyMonthYearSelected(uiState.selectedMonthlyYear, index + 1)
                                        }
                                    )
                                }
                            }
                        }

                        // Year Dropdown
                        Box {
                            OutlinedButton(
                                onClick = { showYearDropdown = true },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("${uiState.selectedMonthlyYear}")
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Year")
                            }
                            DropdownMenu(
                                expanded = showYearDropdown,
                                onDismissRequest = { showYearDropdown = false }
                            ) {
                                uiState.availableYears.forEach { yr ->
                                    DropdownMenuItem(
                                        text = { Text("$yr") },
                                        onClick = {
                                            showYearDropdown = false
                                            viewModel.onMonthlyMonthYearSelected(yr, uiState.selectedMonthlyMonth)
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Yearly Custom Year Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Year: ",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box {
                            OutlinedButton(
                                onClick = { showYearDropdown = true },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("${uiState.selectedYearlyYear}")
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Year")
                            }
                            DropdownMenu(
                                expanded = showYearDropdown,
                                onDismissRequest = { showYearDropdown = false }
                            ) {
                                uiState.availableYears.forEach { yr ->
                                    DropdownMenuItem(
                                        text = { Text("$yr") },
                                        onClick = {
                                            showYearDropdown = false
                                            viewModel.onYearlyYearSelected(yr)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Metric Overview Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = stringResource(R.string.stat_today_count),
                            value = uiState.totalReading.toString(),
                            icon = Icons.Default.Analytics,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = stringResource(R.string.completion_rate),
                            value = "${(uiState.completionRate * 100).toInt()}%",
                            icon = Icons.Default.CheckCircle,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Professional Interactive Chart Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Chart",
                                        tint = ArabicGoldText
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Reading Activity Chart",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { isCurveChartStyle = !isCurveChartStyle }) {
                                        Icon(
                                            imageVector = if (isCurveChartStyle) Icons.Default.BarChart else Icons.AutoMirrored.Filled.ShowChart,
                                            contentDescription = "Toggle Chart Style",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isCurveChartStyle) {
                                ProfessionalCurveAreaChart(
                                    data = uiState.chartData,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(230.dp)
                                )
                            } else {
                                ProfessionalBarChart(
                                    data = uiState.chartData,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(230.dp)
                                )
                            }
                        }
                    }
                }

                // History Search & Date Filter Bar
                item {
                    OutlinedTextField(
                        value = uiState.historySearchQuery,
                        onValueChange = { viewModel.onHistorySearchQueryChanged(it) },
                        placeholder = { Text("Filter history by Tasbih or Date...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search History")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // History List Items
                if (uiState.historyItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_history_records),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                } else {
                    items(uiState.historyItems) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.tasbihName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (item.arabicText.isNotBlank()) {
                                            Text(
                                                text = item.arabicText,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = ArabicGoldText,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                        Text(
                                            text = "${item.date} | Target: ${item.dailyTarget}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${item.todayReading}",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isCompleted) TargetProgressGreen else MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        if (item.pendingRemainingAtEnd > 0) {
                                            Text(
                                                text = "Pending: ${item.pendingRemainingAtEnd}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = PendingBadgeOrange,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePickerModal) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerModal = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val formattedDate = sdf.format(Date(selectedMillis))
                            viewModel.onDailyDateSelected(formattedDate)
                        }
                        showDatePickerModal = false
                    }
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerModal = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ProfessionalCurveAreaChart(
    data: List<Pair<String, Long>>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = ArabicGoldText
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val dynamicFontSize = if (data.size > 8) 18f else 24f

    val textPaint = remember(textColor, dynamicFontSize) {
        Paint().apply {
            color = textColor
            textSize = dynamicFontSize
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    val goldPaint = remember(goldColor, dynamicFontSize) {
        Paint().apply {
            color = goldColor.toArgb()
            textSize = dynamicFontSize
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        if (data.isEmpty()) return@detectTapGestures
                        val count = data.size
                        val availableWidth = size.width
                        val stepX = availableWidth / (count - 1f).coerceAtLeast(1f)

                        data.forEachIndexed { idx, _ ->
                            val x = if (count == 1) availableWidth / 2f else idx * stepX
                            if (offset.x in (x - stepX / 2f)..(x + stepX / 2f)) {
                                selectedIndex = if (selectedIndex == idx) null else idx
                            }
                        }
                    }
                }
        ) {
            if (data.isEmpty()) return@Canvas

            val maxVal = maxOf(1L, data.maxOf { it.second })
            val count = data.size
            val availableWidth = size.width
            val topPadding = 35.dp.toPx()
            val bottomPadding = 32.dp.toPx()
            val usableHeight = size.height - topPadding - bottomPadding

            // Horizontal reference grid lines
            val lineCount = 3
            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            for (i in 0..lineCount) {
                val y = topPadding + (usableHeight / lineCount) * i
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(availableWidth, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashPathEffect
                )
            }

            val points = data.mapIndexed { idx, pair ->
                val x = if (count == 1) availableWidth / 2f else (availableWidth / (count - 1f)) * idx
                val heightRatio = (pair.second.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                val y = topPadding + (usableHeight * (1f - heightRatio))
                Offset(x, y)
            }

            // Build smooth Bezier Curve Path
            val strokePath = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points[0].x, points[0].y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val controlX1 = p1.x + (p2.x - p1.x) / 2f
                        val controlY1 = p1.y
                        val controlX2 = p1.x + (p2.x - p1.x) / 2f
                        val controlY2 = p2.y
                        cubicTo(controlX1, controlY1, controlX2, controlY2, p2.x, p2.y)
                    }
                }
            }

            // Build Fill Path under curve
            val fillPath = Path().apply {
                addPath(strokePath)
                if (points.isNotEmpty()) {
                    lineTo(points.last().x, topPadding + usableHeight)
                    lineTo(points.first().x, topPadding + usableHeight)
                    close()
                }
            }

            // Draw glowing gradient fill under curve
            val areaGradient = Brush.verticalGradient(
                colors = listOf(goldColor.copy(alpha = 0.45f), primaryColor.copy(alpha = 0.05f)),
                startY = topPadding,
                endY = topPadding + usableHeight
            )
            drawPath(path = fillPath, brush = areaGradient)

            // Draw smooth glowing curve stroke
            drawPath(
                path = strokePath,
                brush = Brush.horizontalGradient(listOf(primaryColor, goldColor, primaryColor)),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            val peakVal = data.maxOf { it.second }

            // Draw interactive touch nodes and labels
            points.forEachIndexed { index, point ->
                val pair = data[index]
                val fullLabel = pair.first
                val shortLabel = if (fullLabel.length > 5) fullLabel.take(5) else fullLabel
                val valNum = pair.second

                val isPeak = valNum > 0 && valNum == peakVal
                val isSelected = selectedIndex == index

                // Outer aura ring for peak/selected nodes
                if (isPeak || isSelected) {
                    drawCircle(
                        color = goldColor.copy(alpha = 0.35f),
                        radius = 12.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = goldColor,
                        radius = 6.dp.toPx(),
                        center = point
                    )
                } else {
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }

                // Value label on top
                if (valNum > 0) {
                    drawContext.canvas.nativeCanvas.drawText(
                        valNum.toString(),
                        point.x,
                        point.y - 12.dp.toPx(),
                        if (isPeak || isSelected) goldPaint else textPaint
                    )
                }

                // Date/Tasbih label on bottom
                drawContext.canvas.nativeCanvas.drawText(
                    shortLabel,
                    point.x,
                    size.height - 6.dp.toPx(),
                    if (isSelected) goldPaint else textPaint
                )
            }
        }

        // Sleek Compact Bottom Inspect Info Badge
        if (selectedIndex != null && selectedIndex!! in data.indices) {
            val selectedItem = data[selectedIndex!!]
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${selectedItem.first}: ${selectedItem.second} counts",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.clickable { selectedIndex = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfessionalBarChart(
    data: List<Pair<String, Long>>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = ArabicGoldText
    val trackColor = MaterialTheme.colorScheme.surface
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val dynamicFontSize = if (data.size > 8) 18f else 24f

    val textPaint = remember(textColor, dynamicFontSize) {
        Paint().apply {
            color = textColor
            textSize = dynamicFontSize
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    val goldPaint = remember(goldColor, dynamicFontSize) {
        Paint().apply {
            color = goldColor.toArgb()
            textSize = dynamicFontSize
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        if (data.isEmpty()) return@detectTapGestures
                        val availableWidth = size.width
                        val count = data.size
                        val barSpacing = availableWidth / (count * 2f + 1f)
                        val barWidth = barSpacing

                        data.forEachIndexed { idx, _ ->
                            val xStart = barSpacing + idx * (barWidth + barSpacing)
                            val xEnd = xStart + barWidth
                            if (offset.x in (xStart - barSpacing / 2f)..(xEnd + barSpacing / 2f)) {
                                selectedIndex = if (selectedIndex == idx) null else idx
                            }
                        }
                    }
                }
        ) {
            if (data.isEmpty()) return@Canvas

            val maxVal = maxOf(1L, data.maxOf { it.second })
            val count = data.size
            val availableWidth = size.width
            val topPadding = 35.dp.toPx()
            val bottomPadding = 32.dp.toPx()
            val usableHeight = size.height - topPadding - bottomPadding

            val barSpacing = availableWidth / (count * 2f + 1f)
            val barWidth = barSpacing

            // Draw horizontal grid lines
            val lineCount = 3
            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            for (i in 0..lineCount) {
                val y = topPadding + (usableHeight / lineCount) * i
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(availableWidth, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashPathEffect
                )
            }

            val peakVal = data.maxOf { it.second }

            data.forEachIndexed { index, pair ->
                val fullLabel = pair.first
                val shortLabel = if (fullLabel.length > 5) fullLabel.take(5) else fullLabel
                val valNum = pair.second

                val heightRatio = (valNum.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                val barHeight = usableHeight * heightRatio

                val x = barSpacing + index * (barWidth + barSpacing)
                val yTop = topPadding + (usableHeight - barHeight)
                val yBottom = topPadding + usableHeight

                val isPeak = valNum > 0 && valNum == peakVal
                val isSelected = selectedIndex == index

                // Draw Background Bar Track
                drawRoundRect(
                    color = trackColor.copy(alpha = 0.6f),
                    topLeft = Offset(x, topPadding),
                    size = Size(barWidth, usableHeight),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )

                // Draw Active Bar with Gradient
                if (barHeight > 0) {
                    val barGradient = if (isPeak) {
                        Brush.verticalGradient(
                            colors = listOf(goldColor, primaryColor),
                            startY = yTop,
                            endY = yBottom
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(primaryColor, primaryColor.copy(alpha = 0.6f)),
                            startY = yTop,
                            endY = yBottom
                        )
                    }

                    drawRoundRect(
                        brush = barGradient,
                        topLeft = Offset(x, yTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )

                    // Draw Selected Ring Outline
                    if (isSelected) {
                        drawRoundRect(
                            color = goldColor,
                            topLeft = Offset(x - 2.dp.toPx(), yTop - 2.dp.toPx()),
                            size = Size(barWidth + 4.dp.toPx(), barHeight + 4.dp.toPx()),
                            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }

                // Draw Value Badge on top of bar
                if (valNum > 0) {
                    drawContext.canvas.nativeCanvas.drawText(
                        valNum.toString(),
                        x + barWidth / 2f,
                        yTop - 10.dp.toPx(),
                        if (isPeak || isSelected) goldPaint else textPaint
                    )
                }

                // Draw Bottom Axis Label
                drawContext.canvas.nativeCanvas.drawText(
                    shortLabel,
                    x + barWidth / 2f,
                    size.height - 6.dp.toPx(),
                    if (isSelected) goldPaint else textPaint
                )
            }
        }

        // Sleek Compact Bottom Inspect Info Badge
        if (selectedIndex != null && selectedIndex!! in data.indices) {
            val selectedItem = data[selectedIndex!!]
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${selectedItem.first}: ${selectedItem.second} counts",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.clickable { selectedIndex = null }
                    )
                }
            }
        }
    }
}