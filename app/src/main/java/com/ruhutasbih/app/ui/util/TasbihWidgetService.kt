package com.ruhutasbih.app.ui.util

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.ruhutasbih.app.R
import com.ruhutasbih.app.data.local.AppDatabase
import com.ruhutasbih.app.data.local.entity.TasbihEntity
import kotlinx.coroutines.runBlocking

class TasbihWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TasbihWidgetFactory(applicationContext)
    }
}

class TasbihWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var tasbihList: List<TasbihEntity> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        try {
            runBlocking {
                val db = AppDatabase.getDatabase(context)
                val allTasbihs = db.tasbihDao().getAllTasbihsList()
                val pinned = allTasbihs.filter { it.isPinned && !it.isArchived }
                tasbihList = if (pinned.isNotEmpty()) pinned else allTasbihs.filter { !it.isArchived }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tasbihList = emptyList()
        }
    }

    override fun onDestroy() {
        tasbihList = emptyList()
    }

    override fun getCount(): Int = tasbihList.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= tasbihList.size) return null
        val item = tasbihList[position]

        val views = RemoteViews(context.packageName, R.layout.tasbih_widget_item)
        views.setTextViewText(R.id.widget_item_name, item.name)
        views.setTextViewText(R.id.widget_item_arabic, item.arabicText.ifBlank { item.name })
        views.setTextViewText(R.id.widget_item_count, "${item.todayReading} / ${item.dailyTarget}")

        val fillInIntent = Intent().apply {
            putExtra(TasbihWidgetProvider.EXTRA_TASBIH_ID, item.id)
        }
        views.setOnClickFillInIntent(R.id.widget_item_btn_increment, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}