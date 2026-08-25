package com.caiji.app.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.caiji.app.data.ServiceLocator
import com.caiji.app.data.db.entity.Diary
import com.caiji.app.data.db.entity.HighlightPhoto
import com.caiji.app.data.db.entity.LotteryRecord
import com.caiji.app.data.db.entity.MonthlyBudget
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 数据备份管理 - 将 Room 数据导出为 JSON。
 * - 本地导出：写到应用私有目录 backups/
 * - 云盘导出：本地导出后用系统分享 Intent 发送给云盘 App
 */
object BackupManager {

    private const val BACKUP_DIR = "backups"

    suspend fun exportToLocal(context: Context): String {
        val db = ServiceLocator.provideDatabase(context)
        val data = collectAll(db)

        val json = buildJson(data)
        val dir = File(context.filesDir, BACKUP_DIR).apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
        val file = File(dir, "caiji_backup_$timestamp.json")
        file.writeText(json, Charsets.UTF_8)
        return file.absolutePath
    }

    /**
     * 云盘导出：先本地导出，再通过 Intent 分享给系统（用户选择云盘 App）。
     * 返回 true 表示成功唤起分享 Intent。
     */
    suspend fun exportToCloud(context: Context): Boolean {
        val path = exportToLocal(context)
        val file = File(path)
        val uri: Uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            return false
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "备份到云盘").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            false
        }
    }

    private data class AllData(
        val lottery: List<LotteryRecord>,
        val diaries: List<Diary>,
        val highlights: List<HighlightPhoto>,
        val budgets: List<MonthlyBudget>
    )

    private suspend fun collectAll(db: com.caiji.app.data.db.CaiJiDatabase): AllData =
        AllData(
            lottery = db.lotteryDao().observeAll().first(),
            diaries = db.diaryDao().observeAll().first(),
            highlights = db.highlightDao().observeAll().first(),
            budgets = db.budgetDao().observeAll().first()
        )

    private fun buildJson(data: AllData): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())

        val lottery = JSONArray()
        data.lottery.forEach { r ->
            lottery.put(JSONObject().apply {
                put("id", r.id)
                put("amount", r.amount)
                put("winAmount", r.winAmount)
                put("note", r.note)
                put("imagePath", r.imagePath)
                put("isBigPrize", r.isBigPrize)
                put("createdAt", r.createdAt)
            })
        }
        root.put("lotteryRecords", lottery)

        val diaries = JSONArray()
        data.diaries.forEach { d ->
            diaries.put(JSONObject().apply {
                put("id", d.id)
                put("recordTime", d.recordTime)
                put("title", d.title)
                put("content", d.content)
                put("mood", d.mood)
                put("lotteryRecordId", d.lotteryRecordId)
            })
        }
        root.put("diaries", diaries)

        val highlights = JSONArray()
        data.highlights.forEach { h ->
            highlights.put(JSONObject().apply {
                put("id", h.id)
                put("prizeAmount", h.prizeAmount)
                put("description", h.description)
                put("takenAt", h.takenAt)
            })
        }
        root.put("highlightPhotos", highlights)

        val budgets = JSONArray()
        data.budgets.forEach { b ->
            budgets.put(JSONObject().apply {
                put("month", b.month)
                put("budgetAmount", b.budgetAmount)
            })
        }
        root.put("monthlyBudgets", budgets)

        return root.toString(2)
    }
}
