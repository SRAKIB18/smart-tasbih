package com.ruhutasbih.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ruhutasbih.app.data.local.dao.CountHistoryDao
import com.ruhutasbih.app.data.local.dao.DailyProgressDao
import com.ruhutasbih.app.data.local.dao.TasbihDao
import com.ruhutasbih.app.data.local.entity.CountHistoryEntity
import com.ruhutasbih.app.data.local.entity.DailyProgressEntity
import com.ruhutasbih.app.data.local.entity.TasbihEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TasbihEntity::class,
        DailyProgressEntity::class,
        CountHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tasbihDao(): TasbihDao
    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun countHistoryDao(): CountHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_tasbih_database"
                )
                .addCallback(DatabaseCallback(context.applicationContext))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialDatabase(database.tasbihDao())
                    }
                }
            }
        }

        suspend fun populateInitialDatabase(tasbihDao: TasbihDao) {
            val defaultTasbihs = listOf(
                TasbihEntity(
                    id = "TASBIH_001",
                    name = "Subhanallah",
                    arabicText = "سُبْحَانَ اللَّهِ",
                    transliteration = "Subhanallah",
                    meaning = "Glory be to Allah",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = true,
                    category = "Daily"
                ),
                TasbihEntity(
                    id = "TASBIH_002",
                    name = "Alhamdulillah",
                    arabicText = "الْحَمْدُ لِلَّهِ",
                    transliteration = "Alhamdulillah",
                    meaning = "Praise be to Allah",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = true,
                    category = "Daily"
                ),
                TasbihEntity(
                    id = "TASBIH_003",
                    name = "Allahu Akbar",
                    arabicText = "اللَّهُ أَكْبَرُ",
                    transliteration = "Allahu Akbar",
                    meaning = "Allah is the Greatest",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = true,
                    category = "Daily"
                ),
                TasbihEntity(
                    id = "TASBIH_004",
                    name = "Astaghfirullah",
                    arabicText = "أَسْتَغْفِرُ اللَّهَ",
                    transliteration = "Astaghfirullah",
                    meaning = "I seek forgiveness from Allah",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = false,
                    category = "Forgiveness"
                ),
                TasbihEntity(
                    id = "TASBIH_005",
                    name = "La ilaha illallah",
                    arabicText = "لَا إِلٰهَ إِلَّا اللَّهُ",
                    transliteration = "La ilaha illallah",
                    meaning = "There is no god but Allah",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = false,
                    category = "Tawheed"
                ),
                TasbihEntity(
                    id = "TASBIH_006",
                    name = "Subhanallahi wa bihamdihi",
                    arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ سُبْحَانَ اللَّهِ الْعَظِيمِ",
                    transliteration = "Subhanallahi wa bihamdihi, Subhanallahil Azeem",
                    meaning = "Glory be to Allah and His is the praise, Glory be to Allah the Supreme",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = false,
                    category = "Praise"
                ),
                TasbihEntity(
                    id = "TASBIH_007",
                    name = "Durood Sharif",
                    arabicText = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ",
                    transliteration = "Allahumma salli ala Muhammadin wa ala ali Muhammad",
                    meaning = "O Allah, send blessings upon Muhammad and the family of Muhammad",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = false,
                    category = "Salawat"
                )
            )
            tasbihDao.insertAll(defaultTasbihs)
        }
    }
}