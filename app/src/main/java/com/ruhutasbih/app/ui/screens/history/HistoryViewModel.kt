package com.ruhutasbih.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ruhutasbih.app.domain.engine.AchievementBadge
import com.ruhutasbih.app.domain.engine.DateRolloverEngine
import com.ruhutasbih.app.domain.engine.StreakEngine
import com.ruhutasbih.app.domain.engine.StreakResult
import com.ruhutasbih.app.domain.repository.HistoryRepository
import com.ruhutasbih.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HistoryItemUiModel(
    val id: Long,
    val tasbihId: String,
    val tasbihName: String,
    val arabicText: String,
    val date: String,
    val dailyTarget: Long,
    val todayReading: Long,
    val dailyCount: Long,
    val pendingAtStart: Long,
    val pendingRemainingAtEnd: Long,
    val nextDayPending: Long,
    val isCompleted: Boolean
)

data class HistoryUiState(
    val selectedTab: Int = 0, // 0: Daily, 1: Weekly, 2: Monthly, 3: Yearly
    val selectedDailyDate: String = DateRolloverEngine.getTodayDateString(),
    val isTodaySelected: Boolean = true,
    val selectedMonthlyYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonthlyMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1, // 1..12
    val selectedYearlyYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val historySearchQuery: String = "",
    val availableYears: List<Int> = listOf(Calendar.getInstance().get(Calendar.YEAR)),
    val historyItems: List<HistoryItemUiModel> = emptyList(),
    val totalReading: Long = 0L,
    val targetsCompleted: Int = 0,
    val completionRate: Float = 0f,
    val chartData: List<Pair<String, Long>> = emptyList()
)

private data class SelectionState(
    val tab: Int,
    val dailyDate: String,
    val mYear: Int,
    val mMonth: Int,
    val yYear: Int,
    val query: String
)

class HistoryViewModel(
    historyRepository: HistoryRepository,
    tasbihRepository: TasbihRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    private val _selectedDailyDate = MutableStateFlow(DateRolloverEngine.getTodayDateString())
    private val currentCal = Calendar.getInstance()
    private val _selectedMonthlyYear = MutableStateFlow(currentCal.get(Calendar.YEAR))
    private val _selectedMonthlyMonth = MutableStateFlow(currentCal.get(Calendar.MONTH) + 1)
    private val _selectedYearlyYear = MutableStateFlow(currentCal.get(Calendar.YEAR))

    private val selectionsFlow = combine(
        _selectedTab,
        _selectedDailyDate,
        _selectedMonthlyYear,
        _selectedMonthlyMonth,
        _selectedYearlyYear
    ) { args: Array<Any> ->
        SelectionState(
            tab = args[0] as Int,
            dailyDate = args[1] as String,
            mYear = args[2] as Int,
            mMonth = args[3] as Int,
            yYear = args[4] as Int,
            query = ""
        )
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        historyRepository.getAllProgressFlow(),
        tasbihRepository.getAllTasbihs(),
        selectionsFlow
    ) { allProgress, allTasbihs, sel ->
        val tasbihMap = allTasbihs.associateBy { it.id }

        val mappedList = allProgress.map { progress ->
            val tasbih = tasbihMap[progress.tasbihId]
            HistoryItemUiModel(
                id = progress.id,
                tasbihId = progress.tasbihId,
                tasbihName = tasbih?.name ?: "Tasbih",
                arabicText = tasbih?.arabicText ?: "",
                date = progress.date,
                dailyTarget = progress.dailyTarget,
                todayReading = progress.todayReading,
                dailyCount = progress.dailyCount,
                pendingAtStart = progress.pendingAtStart,
                pendingRemainingAtEnd = progress.pendingRemainingAtEnd,
                nextDayPending = progress.nextDayPending,
                isCompleted = progress.isCompleted
            )
        }

        val todayStr = DateRolloverEngine.getTodayDateString()

        // Generate available years: Default range (past 5 years to current year + 1) + dataset years
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val defaultYears = ((currentYear - 5)..(currentYear + 1)).toSet()
        val datasetYears = allProgress.asSequence().mapNotNull {
            it.date.take(4).toIntOrNull()
        }.toSet()
        val sortedYears = (defaultYears + datasetYears).sortedDescending()

        // Filter items based on active tab and custom selectors
        val tabFiltered = when (sel.tab) {
            0 -> mappedList.filter { it.date == sel.dailyDate }
            1 -> {
                val dates = getPastDatesFrom(sel.dailyDate, 7)
                mappedList.filter { it.date in dates }
            }
            2 -> {
                val monthPrefix = String.format(Locale.US, "%04d-%02d", sel.mYear, sel.mMonth)
                mappedList.filter { it.date.startsWith(monthPrefix) }
            }
            else -> {
                val yearPrefix = String.format(Locale.US, "%04d", sel.yYear)
                mappedList.filter { it.date.startsWith(yearPrefix) }
            }
        }

        // Apply history search query filter (searches Tasbih name or Date)
        val query = sel.query.trim()
        val finalFiltered = if (query.isBlank()) {
            tabFiltered
        } else {
            tabFiltered.filter {
                it.tasbihName.contains(query, ignoreCase = true) ||
                it.date.contains(query, ignoreCase = true)
            }
        }

        val totalReading = tabFiltered.sumOf { it.todayReading }
        val completedCount = tabFiltered.count { it.isCompleted }
        val completionRate = if (tabFiltered.isNotEmpty()) {
            completedCount.toFloat() / tabFiltered.size.toFloat()
        } else 0f

        // Generate precise chart data with FULL Tasbih names
        val chartData = when (sel.tab) {
            0 -> {
                tabFiltered.map { it.tasbihName to it.todayReading }
            }
            1 -> {
                val past7Dates = getPastDatesFrom(sel.dailyDate, 7).reversed()
                val groupedByDate = mappedList.groupBy { it.date }
                past7Dates.map { d ->
                    formatShortDateLabel(d) to (groupedByDate[d]?.sumOf { it.todayReading } ?: 0L)
                }
            }
            2 -> {
                // Show reading across days of selected custom month
                val monthPrefix = String.format(Locale.US, "%04d-%02d", sel.mYear, sel.mMonth)
                val monthItems = mappedList.filter { it.date.startsWith(monthPrefix) }
                val groupedByDate = monthItems.groupBy { it.date }

                val maxDaysInMonth = getMaxDaysInMonth(sel.mYear, sel.mMonth)
                val dayChunks = (1..maxDaysInMonth).chunked(maxOf(1, maxDaysInMonth / 6))
                dayChunks.map { chunk ->
                    val label = "${chunk.first()}-${chunk.last()}"
                    val sum = chunk.sumOf { day ->
                        val dateKey = String.format(Locale.US, "%04d-%02d-%02d", sel.mYear, sel.mMonth, day)
                        groupedByDate[dateKey]?.sumOf { it.todayReading } ?: 0L
                    }
                    label to sum
                }
            }
            else -> {
                // Show 12 months for selected custom year
                val yearItems = mappedList.filter { it.date.startsWith(String.format(Locale.US, "%04d", sel.yYear)) }
                val groupedByMonth = yearItems.groupBy { it.date.take(7) }
                val monthsList = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                monthsList.mapIndexed { index, mLabel ->
                    val mKey = String.format(Locale.US, "%04d-%02d", sel.yYear, index + 1)
                    mLabel to (groupedByMonth[mKey]?.sumOf { it.todayReading } ?: 0L)
                }
            }
        }

        HistoryUiState(
            selectedTab = sel.tab,
            selectedDailyDate = sel.dailyDate,
            isTodaySelected = sel.dailyDate == todayStr,
            selectedMonthlyYear = sel.mYear,
            selectedMonthlyMonth = sel.mMonth,
            selectedYearlyYear = sel.yYear,
            historySearchQuery = sel.query,
            availableYears = sortedYears,
            historyItems = finalFiltered,
            totalReading = totalReading,
            targetsCompleted = completedCount,
            completionRate = completionRate,
            chartData = chartData
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    val streakResult: StateFlow<StreakResult> = combine(
        historyRepository.getAllProgressFlow(),
        tasbihRepository.getAllTasbihs()
    ) { allProgress, allTasbihs ->
        val totalLifetime = allTasbihs.sumOf { it.lifetimeCount }
        StreakEngine.calculateStreak(allProgress, totalLifetime)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StreakResult()
    )

    val badges: StateFlow<List<AchievementBadge>> = streakResult
        .map { result -> StreakEngine.getBadges(result) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onTabSelected(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun onDailyDateSelected(dateStr: String) {
        _selectedDailyDate.value = dateStr
    }

    fun onMonthlyMonthYearSelected(year: Int, month: Int) {
        _selectedMonthlyYear.value = year
        _selectedMonthlyMonth.value = month
    }

    fun onYearlyYearSelected(year: Int) {
        _selectedYearlyYear.value = year
    }


    fun selectPreviousDay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            val date = sdf.parse(_selectedDailyDate.value) ?: return
            val cal = Calendar.getInstance().apply { time = date }
            cal.add(Calendar.DAY_OF_YEAR, -1)
            _selectedDailyDate.value = sdf.format(cal.time)
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    fun selectNextDay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            val date = sdf.parse(_selectedDailyDate.value) ?: return
            val cal = Calendar.getInstance().apply { time = date }
            cal.add(Calendar.DAY_OF_YEAR, 1)
            _selectedDailyDate.value = sdf.format(cal.time)
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    fun selectToday() {
        _selectedDailyDate.value = DateRolloverEngine.getTodayDateString()
    }

    private fun getPastDatesFrom(startDateStr: String, count: Int = 7): List<String> {
        val list = mutableListOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        try {
            val startDate = sdf.parse(startDateStr)
            if (startDate != null) {
                cal.time = startDate
            }
        } catch (_: Exception) {
            // Use current date
        }

        repeat(count) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return list
    }

    private fun formatShortDateLabel(dateStr: String): String {
        return try {
            val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputSdf = SimpleDateFormat("dd/MM", Locale.US)
            val date = inputSdf.parse(dateStr)
            date?.let { outputSdf.format(it) } ?: dateStr.takeLast(5)
        } catch (_: Exception) {
            dateStr.takeLast(5)
        }
    }

    private fun getMaxDaysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    class Factory(
        private val historyRepository: HistoryRepository,
        private val tasbihRepository: TasbihRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(historyRepository, tasbihRepository) as T
        }
    }
}