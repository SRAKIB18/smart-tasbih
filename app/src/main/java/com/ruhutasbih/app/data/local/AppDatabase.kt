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
    version = 2,
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

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (database.tasbihDao().getTasbihCount() == 0) {
                            populateInitialDatabase(database.tasbihDao())
                        }
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
                    meaning = "আল্লাহ পরম পবিত্র (Glory be to Allah)",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = true,
                    category = "দৈনিক জিকির",
                    icon = "📿"
                ),
                TasbihEntity(
                    id = "TASBIH_002",
                    name = "Alhamdulillah",
                    arabicText = "الْحَمْدُ لِلَّهِ",
                    transliteration = "Alhamdulillah",
                    meaning = "সকল প্রশংসা একমাত্র আল্লাহর (Praise be to Allah)",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = true,
                    category = "দৈনিক জিকির",
                    icon = "📿"
                ),
                TasbihEntity(
                    id = "TASBIH_003",
                    name = "Allahu Akbar",
                    arabicText = "اللَّهُ أَكْبَرُ",
                    transliteration = "Allahu Akbar",
                    meaning = "আল্লাহ সবচেয়ে মহান (Allah is the Greatest)",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = true,
                    category = "দৈনিক জিকির",
                    icon = "📿"
                ),
                TasbihEntity(
                    id = "TASBIH_004",
                    name = "Astaghfirullah",
                    arabicText = "أَسْتَغْفِرُ اللَّهَ",
                    transliteration = "Astaghfirullah",
                    meaning = "আমি আল্লাহর কাছে ক্ষমা প্রার্থনা করছি",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = true,
                    category = "ইস্তিগফার",
                    icon = "🤲"
                ),
                TasbihEntity(
                    id = "TASBIH_005",
                    name = "La ilaha illallah",
                    arabicText = "لَا إِلٰهَ إِلَّا اللَّهُ",
                    transliteration = "La ilaha illallah",
                    meaning = "আল্লাহ ছাড়া সত্য কোনো উপাস্য নেই",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = true,
                    category = "তাওহীদ",
                    icon = "🕌"
                ),
                TasbihEntity(
                    id = "TASBIH_006",
                    name = "Subhanallahi wa bihamdihi",
                    arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ سُبْحَانَ اللَّهِ الْعَظِيمِ",
                    transliteration = "Subhanallahi wa bihamdihi, Subhanallahil Azeem",
                    meaning = "আল্লাহর প্রশংসাসহ তাঁর পবিত্রতা ঘোষণা করছি",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = false,
                    category = "জিকির ও প্রশংসা",
                    icon = "✨"
                ),
                TasbihEntity(
                    id = "TASBIH_007",
                    name = "Durood Sharif",
                    arabicText = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ",
                    transliteration = "Allahumma salli ala Muhammadin wa ala ali Muhammad",
                    meaning = "হে আল্লাহ, মুহাম্মদ (সাঃ) ও তাঁর পরিবারের ওপর রহমত বর্ষণ করুন",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = false,
                    category = "দরূদ",
                    icon = "🌸"
                ),
                TasbihEntity(
                    id = "TASBIH_008",
                    name = "La hawla wa la quwwata illa billah",
                    arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                    transliteration = "La hawla wa la quwwata illa billah",
                    meaning = "আল্লাহর সাহায্য ছাড়া কোনো উপায় বা শক্তি নেই",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = false,
                    category = "জান্নাতের রত্ন",
                    icon = "👑"
                ),
                TasbihEntity(
                    id = "TASBIH_009",
                    name = "Ayatul Kursi",
                    arabicText = "اللَّهُ لَا إِلٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
                    transliteration = "Allahula ilaha illa huwal hayyul qayyum",
                    meaning = "আল্লাহ, যিনি ছাড়া কোনো উপাস্য নেই, তিনি চিরঞ্জীব",
                    dailyTarget = 33L,
                    previousPending = 0L,
                    isPinned = false,
                    category = "কুরআনের আয়াত",
                    icon = "📖"
                ),
                TasbihEntity(
                    id = "TASBIH_010",
                    name = "Hasbunallahu wa ni'mal wakeel",
                    arabicText = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
                    transliteration = "Hasbunallahu wa ni'mal wakeel",
                    meaning = "আমাদের জন্য আল্লাহই যথেষ্ট, এবং তিনি উত্তম অভিভাবক",
                    dailyTarget = 100L,
                    previousPending = 0L,
                    isPinned = false,
                    category = "ভরসা ও নিরাপত্তা",
                    icon = "🌿"
                )
            )
            tasbihDao.insertAll(defaultTasbihs)
        }
    }
}