package com.ruhutasbih.app.ui.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.RemoteViews
import com.ruhutasbih.app.R
import com.ruhutasbih.app.data.local.AppDatabase
import com.ruhutasbih.app.data.repository.TasbihRepositoryImpl
import com.ruhutasbih.app.domain.engine.StreakEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TasbihWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_INCREMENT) {
            val tasbihId = intent.getStringExtra(EXTRA_TASBIH_ID) ?: return
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    val repository = TasbihRepositoryImpl(
                        tasbihDao = db.tasbihDao(),
                        dailyProgressDao = db.dailyProgressDao(),
                        countHistoryDao = db.countHistoryDao()
                    )
                    repository.checkAndApplyDateRollover()
                    repository.incrementCount(tasbihId, 1L)
                    updateAllWidgets(context.applicationContext)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (appWidgetId in appWidgetIds) {
                    updateSingleWidget(context, appWidgetManager, appWidgetId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_INCREMENT = "com.ruhutasbih.app.WIDGET_INCREMENT"
        const val EXTRA_TASBIH_ID = "com.ruhutasbih.app.EXTRA_TASBIH_ID"

        fun updateAllWidgets(context: Context) {
            val appContext = context.applicationContext
            val appWidgetManager = AppWidgetManager.getInstance(appContext)
            val componentName = ComponentName(appContext, TasbihWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view)

            val intent = Intent(appContext, TasbihWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            appContext.sendBroadcast(intent)
        }


        private suspend fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val db = AppDatabase.getDatabase(context.applicationContext)
            val allProgress = db.dailyProgressDao().getAllProgressList()
            val allTasbihs = db.tasbihDao().getAllTasbihsList()
            val totalLifetime = allTasbihs.sumOf { it.lifetimeCount }
            val streakResult = StreakEngine.calculateStreak(allProgress, totalLifetime)

            val serviceIntent = Intent(context, TasbihWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            val bgBitmap = WidgetBitmapUtil.loadDownsampledBitmap(context, R.drawable.widget_islamic_bg, 400, 250)

            val views = RemoteViews(context.packageName, R.layout.tasbih_widget_layout).apply {
                if (bgBitmap != null) {
                    setImageViewBitmap(R.id.widget_bg_image, bgBitmap)
                } else {
                    setImageViewResource(R.id.widget_bg_image, R.drawable.widget_bg_vector)
                }
                setRemoteAdapter(R.id.widget_list_view, serviceIntent)
                setEmptyView(R.id.widget_list_view, R.id.widget_empty_view)
                setTextViewText(R.id.widget_streak_text, "🔥 ${streakResult.currentStreak} দিন")
            }

            // PendingIntent Template for Item Increment & Click
            val incrementIntent = Intent(context, TasbihWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_INCREMENT
            }
            val incrementPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                incrementIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list_view, incrementPendingIntent)

            // Refresh Button PendingIntent
            val refreshIntent = Intent(context, TasbihWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}