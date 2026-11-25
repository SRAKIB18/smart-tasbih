package com.ruhutasbih.app.domain.engine

import com.ruhutasbih.app.data.local.entity.DailyProgressEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class StreakResult(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletedDays: Int = 0,
    val totalLifetimeCount: Long = 0L
)

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconStr: String,
    val requiredStreakDays: Int = 0,
    val requiredLifetimeCount: Long = 0L,
    val isUnlocked: Boolean = false,
    val progressPercent: Float = 0f
)

object StreakEngine {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun calculateStreak(
        progressList: List<DailyProgressEntity>,
        totalLifetimeCount: Long = 0L
    ): StreakResult {
        if (progressList.isEmpty()) {
            return StreakResult(0, 0, 0, totalLifetimeCount)
        }

        // Group by date and check if any tasbih target was met on that day
        val completedDates = progressList
            .groupBy { it.date }
            .filter { (_, items) -> items.any { it.isCompleted || (it.dailyTarget > 0 && it.todayReading >= it.dailyTarget) } }
            .keys
            .mapNotNull { dateStr ->
                try {
                    dateFormat.parse(dateStr)
                } catch (e: Exception) {
                    null
                }
            }
            .sortedDescending()

        if (completedDates.isEmpty()) {
            return StreakResult(0, 0, 0, totalLifetimeCount)
        }

        val totalCompletedDays = completedDates.size

        // Calculate Current Streak
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val today = cal.time

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.time

        var currentStreak = 0
        val latestCompleted = completedDates.first()

        // Streak is active if user completed target today or yesterday
        if (isSameDay(latestCompleted, today) || isSameDay(latestCompleted, yesterday)) {
            var checkDate = latestCompleted
            for (date in completedDates) {
                if (isSameDay(date, checkDate)) {
                    currentStreak++
                    val tempCal = Calendar.getInstance().apply { time = checkDate }
                    tempCal.add(Calendar.DAY_OF_YEAR, -1)
                    checkDate = tempCal.time
                } else if (date.before(checkDate)) {
                    break
                }
            }
        }

        // Calculate Best Streak
        var bestStreak = 0
        var tempStreak = 0
        var prevDate: Date? = null

        val sortedAsc = completedDates.sorted()
        for (date in sortedAsc) {
            if (prevDate == null) {
                tempStreak = 1
            } else {
                val tempCal = Calendar.getInstance().apply { time = prevDate }
                tempCal.add(Calendar.DAY_OF_YEAR, 1)
                val expectedNext = tempCal.time

                if (isSameDay(date, expectedNext)) {
                    tempStreak++
                } else {
                    tempStreak = 1
                }
            }
            prevDate = date
            if (tempStreak > bestStreak) {
                bestStreak = tempStreak
            }
        }

        return StreakResult(
            currentStreak = currentStreak,
            bestStreak = maxOf(bestStreak, currentStreak),
            totalCompletedDays = totalCompletedDays,
            totalLifetimeCount = totalLifetimeCount
        )
    }

    fun getBadges(streakResult: StreakResult): List<AchievementBadge> {
        val badges = listOf(
            AchievementBadge(
                id = "BADGE_01",
                title = "প্রথম পদক্ষেপ",
                description = "১ দিনের জিকির লক্ষ্য সম্পন্ন করেছেন",
                iconStr = "🌟",
                requiredStreakDays = 1,
                isUnlocked = streakResult.currentStreak >= 1 || streakResult.totalCompletedDays >= 1,
                progressPercent = if (streakResult.totalCompletedDays >= 1) 1.0f else (streakResult.currentStreak.toFloat() / 1f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = "BADGE_02",
                title = "৩ দিনের স্ট্রাইক",
                description = "টানা ৩ দিন জিকিরের লক্ষ্য অর্জন করেছেন",
                iconStr = "🔥",
                requiredStreakDays = 3,
                isUnlocked = streakResult.bestStreak >= 3 || streakResult.currentStreak >= 3,
                progressPercent = (streakResult.currentStreak.toFloat() / 3f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = "BADGE_03",
                title = "৭ দিনের মুরাকাবা",
                description = "টানা ১ সপ্তাহ জিকিরের ধারাবাহিকতা বজায় রেখেছেন",
                iconStr = "⚡",
                requiredStreakDays = 7,
                isUnlocked = streakResult.bestStreak >= 7 || streakResult.currentStreak >= 7,
                progressPercent = (streakResult.currentStreak.toFloat() / 7f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = "BADGE_04",
                title = "১৪ দিনের রিয়াদাত",
                description = "টানা ১৪ দিন দৈনিক জিকির সম্পন্ন করেছেন",
                iconStr = "🏆",
                requiredStreakDays = 14,
                isUnlocked = streakResult.bestStreak >= 14 || streakResult.currentStreak >= 14,
                progressPercent = (streakResult.currentStreak.toFloat() / 14f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = "BADGE_05",
                title = "৩০ দিনের ইস্তিকামাত",
                description = "১ পুরো মাস টানা জিকির ও ইবাদতে নিমগ্ন ছিলেন",
                iconStr = "💎",
                requiredStreakDays = 30,
                isUnlocked = streakResult.bestStreak >= 30 || streakResult.currentStreak >= 30,
                progressPercent = (streakResult.currentStreak.toFloat() / 30f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = "BADGE_06",
                title = "১,০০০ জিকির মাইলফলক",
                description = "সর্বমোট ১,০০০টি জিকির সম্পন্ন করেছেন",
                iconStr = "🕌",
                requiredLifetimeCount = 1000L,
                isUnlocked = streakResult.totalLifetimeCount >= 1000L,
                progressPercent = (streakResult.totalLifetimeCount.toFloat() / 1000f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = "BADGE_07",
                title = "১০,০০০ জিকির মাইলফলক",
                description = "সর্বমোট ১০,০০০টি জিকির সম্পন্ন করেছেন",
                iconStr = "👑",
                requiredLifetimeCount = 10000L,
                isUnlocked = streakResult.totalLifetimeCount >= 10000L,
                progressPercent = (streakResult.totalLifetimeCount.toFloat() / 10000f).coerceIn(0f, 1f)
            ),
            AchievementBadge(
                id = "BADGE_08",
                title = "৫০,০০০ জিকির রয়্যালিটি",
                description = "সর্বমোট ৫০,০০০টি জিকির সম্পন্ন করেছেন",
                iconStr = "🕋",
                requiredLifetimeCount = 50000L,
                isUnlocked = streakResult.totalLifetimeCount >= 50000L,
                progressPercent = (streakResult.totalLifetimeCount.toFloat() / 50000f).coerceIn(0f, 1f)
            )
        )
        return badges
    }

    private fun isSameDay(d1: Date, d2: Date): Boolean {
        val c1 = Calendar.getInstance().apply { time = d1 }
        val c2 = Calendar.getInstance().apply { time = d2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }
}