package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppError
import com.example.data.AppLang
import com.example.data.I18n
import com.example.data.LoadedImageItem
import com.example.data.LoadedPdfInfo
import com.example.data.PdfEngine
import com.example.data.SplitJob
import com.example.data.ToolCategory
import com.example.data.ToolId
import com.example.data.ZipFileEntry
import com.example.data.compactRanges
import com.example.data.flattenGroups
import com.example.data.fmtBytes
import com.example.data.padNum
import com.example.data.parseRanges
import com.example.data.safeName
import com.example.data.stripExt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

private val Context.dataStore by preferencesDataStore(name = "pdflab_prefs")

enum class WorkspaceStage {
    UPLOAD,
    WORK,
    RESULT
}

enum class BatchStatus {
    PENDING,
    COMPRESSING,
    DONE,
    FAILED
}

data class BatchCompressItem(
    val id: Long,
    val info: LoadedPdfInfo,
    val status: BatchStatus = BatchStatus.PENDING,
    val progress: Float = 0f,
    val compressedBytes: ByteArray? = null,
    val compressedFileName: String? = null,
    val alreadyOptimized: Boolean = false,
    val errorMsg: String? = null
)

data class BusyState(
    val text: String,
    val progress: Float? = null
)

data class ToastMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isError: Boolean = false
)

data class ResultOutput(
    val id: Long = System.currentTimeMillis(),
    val toolId: ToolId,
    val filename: String,
    val mimeType: String,
    val bytes: ByteArray,
    val exportedFile: File,
    val rows: List<Pair<String, String>>,
    val notes: List<String> = emptyList()
)

data class PdfLabUiState(
    val lang: AppLang = AppLang.BN,
    val isDarkTheme: Boolean = false,
    val selectedCategory: ToolCategory = ToolCategory.ALL,
    val searchQuery: String = "",
    val activeTool: ToolId? = null,
    val stage: WorkspaceStage = WorkspaceStage.UPLOAD,
    val busy: BusyState? = null,
    val toast: ToastMessage? = null,
    val result: ResultOutput? = null,
    val recentOutputs: List<ResultOutput> = emptyList(),
    val previewTarget: ResultOutput? = null,
    val previewPdfThumbnails: List<Bitmap> = emptyList(),
    val previewZipEntries: List<Pair<String, Long>> = emptyList(),

    // Single PDF loaded for Split / PDF->Img / Rotate / Remove / Extract
    val singlePdf: LoadedPdfInfo? = null,

    // 1) Merge State
    val mergeItems: List<LoadedPdfInfo> = emptyList(),

    // 2) Split State
    val splitMode: String = "single", // "single", "ranges", "every"
    val splitRangesText: String = "",
    val splitEveryNText: String = "2",

    // 3) Compress State (Batch)
    val compressItems: List<BatchCompressItem> = emptyList(),
    val compressPreset: String = "recommended", // "recommended", "extreme", "low", "custom"
    val compressDpiText: String = "144",
    val compressQualityText: String = "75",
    val compressStripMetadata: Boolean = true,
    val isBatchRunning: Boolean = false,
    val stopBatchRequested: Boolean = false,
    val batchProgressLabel: String = "",
    val batchOverallProgress: Float = 0f,

    // 4) Images -> PDF State
    val imageItems: List<LoadedImageItem> = emptyList(),
    val imgPageSize: String = "a4",          // "fit", "a4", "letter"
    val imgOrientation: String = "auto",     // "auto", "portrait", "landscape"
    val imgMarginMm: Int = 10,               // 0, 10, 20

    // 5) PDF -> Images State
    val p2iFormat: String = "png",           // "png", "jpeg"
    val p2iDpi: Int = 150,                   // 72, 150, 300
    val p2iPagesText: String = "",

    // 6) Rotate State
    val pageRotations: List<Int> = emptyList(),

    // 7) Remove & 8) Extract State
    val selectedPages1Based: Set<Int> = emptySet(),
    val selectionRangeText: String = "",
    val selectionRangeError: String? = null,
    val extractMode: String = "single"       // "single", "separate"
)

class PdfLabViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PdfLabUiState())
    val uiState: StateFlow<PdfLabUiState> = _uiState.asStateFlow()

    private var uidCounter = 1L
    private fun nextId(): Long = ++uidCounter

    private val KEY_LANG = stringPreferencesKey("deskmate_lang")
    private val KEY_DARK = booleanPreferencesKey("deskmate_dark")

    init {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().dataStore.data.first()
                val savedLang = prefs[KEY_LANG]?.let { code ->
                    if (code == "en") AppLang.EN else AppLang.BN
                } ?: AppLang.BN
                val savedDark = prefs[KEY_DARK] ?: false
                _uiState.update { it.copy(lang = savedLang, isDarkTheme = savedDark) }
            } catch (_: Exception) {
            }
        }
    }

    fun t(key: String, vars: Map<String, Any> = emptyMap()): String {
        return I18n.t(_uiState.value.lang, key, vars)
    }

    fun setLanguage(lang: AppLang) {
        _uiState.update { it.copy(lang = lang) }
        viewModelScope.launch {
            try {
                getApplication<Application>().dataStore.edit { it[KEY_LANG] = lang.code }
            } catch (_: Exception) {
            }
        }
    }

    fun toggleTheme() {
        val next = !_uiState.value.isDarkTheme
        _uiState.update { it.copy(isDarkTheme = next) }
        viewModelScope.launch {
            try {
                getApplication<Application>().dataStore.edit { it[KEY_DARK] = next }
            } catch (_: Exception) {
            }
        }
    }

    fun setCategory(category: ToolCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun showToast(message: String, isError: Boolean = false) {
        _uiState.update {
            it.copy(toast = ToastMessage(text = message, isError = isError))
        }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toast = null) }
    }

    private fun reportError(e: Throwable) {
        _uiState.update { it.copy(busy = null) }
        val msg = if (e is AppError) {
            t(e.key, e.vars)
        } else {
            e.localizedMessage ?: t("errGeneric")
        }
        showToast(msg, isError = true)
    }

    fun openTool(toolId: ToolId) {
        resetToolState()
        _uiState.update {
            it.copy(
                activeTool = toolId,
                stage = WorkspaceStage.UPLOAD
            )
        }
    }

    fun navigateToDashboard() {
        resetToolState()
        _uiState.update {
            it.copy(
                activeTool = null,
                stage = WorkspaceStage.UPLOAD
            )
        }
    }

    fun startOverCurrentTool() {
        resetToolState()
        _uiState.update {
            it.copy(stage = WorkspaceStage.UPLOAD)
        }
    }

    private fun resetToolState() {
        _uiState.update {
            it.copy(
                stage = WorkspaceStage.UPLOAD,
                busy = null,
                result = null,
                singlePdf = null,
                mergeItems = emptyList(),
                splitMode = "single",
                splitRangesText = "",
                splitEveryNText = "2",
                compressItems = emptyList(),
                compressPreset = "recommended",
                compressDpiText = "144",
                compressQualityText = "75",
                compressStripMetadata = true,
                isBatchRunning = false,
                stopBatchRequested = false,
                batchProgressLabel = "",
                batchOverallProgress = 0f,
                imageItems = emptyList(),
                imgPageSize = "a4",
                imgOrientation = "auto",
                imgMarginMm = 10,
                p2iFormat = "png",
                p2iDpi = 150,
                p2iPagesText = "",
                pageRotations = emptyList(),
                selectedPages1Based = emptySet(),
                selectionRangeText = "",
                selectionRangeError = null,
                extractMode = "single"
            )
        }
    }

    private suspend fun saveExportFile(filename: String, bytes: ByteArray): File = withContext(Dispatchers.IO) {
        val dir = File(getApplication<Application>().cacheDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        val outFile = File(dir, filename)
        FileOutputStream(outFile).use { it.write(bytes) }
        outFile
    }

    private suspend fun publishResult(
        toolId: ToolId,
        filename: String,
        mimeType: String,
        bytes: ByteArray,
        rows: List<Pair<String, String>>,
        notes: List<String> = emptyList()
    ) {
        val exportedFile = saveExportFile(filename, bytes)
        val output = ResultOutput(
            toolId = toolId,
            filename = filename,
            mimeType = mimeType,
            bytes = bytes,
            exportedFile = exportedFile,
            rows = rows,
            notes = notes
        )
        _uiState.update { state ->
            state.copy(
                busy = null,
                result = output,
                stage = WorkspaceStage.RESULT,
                recentOutputs = (listOf(output) + state.recentOutputs).take(12)
            )
        }
    }

    fun clearRecentOutputs() {
        _uiState.update { it.copy(recentOutputs = emptyList()) }
    }

    fun writeOutputToDocumentUri(uri: Uri, output: ResultOutput) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(output.bytes)
                    os.flush()
                }
                withContext(Dispatchers.Main) {
                    showToast(t("savedToDevice"), isError = false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    reportError(e)
                }
            }
        }
    }

    fun shareOutputFile(context: Context, output: ResultOutput) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, output.exportedFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = output.mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, output.filename).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            reportError(e)
        }
    }

    fun openPreviewDialog(output: ResultOutput) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    previewTarget = output,
                    previewPdfThumbnails = emptyList(),
                    previewZipEntries = emptyList()
                )
            }
            if (output.mimeType == "application/pdf") {
                try {
                    val info = PdfEngine.loadPdfFromFile(
                        file = output.exportedFile,
                        displayName = output.filename,
                        id = nextId(),
                        loadThumbnails = true
                    )
                    _uiState.update { it.copy(previewPdfThumbnails = info.thumbnails) }
                } catch (_: Exception) {
                }
            } else {
                val entries = withContext(Dispatchers.IO) {
                    PdfEngine.inspectZipEntries(output.bytes)
                }
                _uiState.update { it.copy(previewZipEntries = entries) }
            }
        }
    }

    fun closePreviewDialog() {
        _uiState.update {
            it.copy(
                previewTarget = null,
                previewPdfThumbnails = emptyList(),
                previewZipEntries = emptyList()
            )
        }
    }

    /* =====================================================================
       SINGLE PDF LOADER (Split, PDF->Img, Rotate, Remove, Extract)
       ===================================================================== */
    fun loadSinglePdfFromUri(uri: Uri) {
        val tool = _uiState.value.activeTool ?: return
        val needThumbs = tool == ToolId.ROTATE || tool == ToolId.REMOVE || tool == ToolId.EXTRACT
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("reading"))) }
            try {
                val info = PdfEngine.loadPdfFromUri(
                    context = getApplication(),
                    uri = uri,
                    id = nextId(),
                    loadThumbnails = needThumbs
                )
                onSinglePdfLoaded(info)
            } catch (e: Exception) {
                reportError(e)
            }
        }
    }

    fun loadSampleSinglePdf() {
        val tool = _uiState.value.activeTool ?: return
        val needThumbs = tool == ToolId.ROTATE || tool == ToolId.REMOVE || tool == ToolId.EXTRACT
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("reading"))) }
            try {
                val (file, name) = PdfEngine.createSamplePdfFile(getApplication(), variantIndex = 1, pageCount = 6)
                val info = PdfEngine.loadPdfFromFile(file, name, nextId(), loadThumbnails = needThumbs)
                onSinglePdfLoaded(info)
            } catch (e: Exception) {
                reportError(e)
            }
        }
    }

    private fun onSinglePdfLoaded(info: LoadedPdfInfo) {
        _uiState.update {
            it.copy(
                busy = null,
                singlePdf = info,
                stage = WorkspaceStage.WORK,
                pageRotations = List(info.pageCount) { 0 },
                selectedPages1Based = emptySet(),
                selectionRangeText = "",
                selectionRangeError = null
            )
        }
    }

    /* =====================================================================
       1) MERGE PDF ACTIONS
       ===================================================================== */
    fun addMergePdfsFromUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("reading"))) }
            val loaded = mutableListOf<LoadedPdfInfo>()
            for ((idx, uri) in uris.withIndex()) {
                val fname = PdfEngine.getFileNameFromUri(getApplication(), uri)
                _uiState.update {
                    it.copy(
                        busy = BusyState(
                            text = t("readingN", mapOf("n" to (idx + 1), "total" to uris.size, "name" to fname)),
                            progress = idx.toFloat() / uris.size.toFloat()
                        )
                    )
                }
                try {
                    val info = PdfEngine.loadPdfFromUri(
                        context = getApplication(),
                        uri = uri,
                        id = nextId(),
                        loadThumbnails = true
                    )
                    loaded.add(info)
                } catch (e: Exception) {
                    val msg = if (e is AppError) t(e.key, e.vars) else e.localizedMessage ?: t("errBadPdf")
                    showToast("$fname: $msg", isError = true)
                }
            }
            _uiState.update { state ->
                val updated = state.mergeItems + loaded
                state.copy(
                    busy = null,
                    mergeItems = updated,
                    stage = if (updated.isNotEmpty()) WorkspaceStage.WORK else WorkspaceStage.UPLOAD
                )
            }
        }
    }

    fun loadSampleMergePdfs() {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("reading"))) }
            try {
                val loaded = mutableListOf<LoadedPdfInfo>()
                val pageCounts = listOf(3, 4, 2)
                for (i in 1..3) {
                    val (file, name) = PdfEngine.createSamplePdfFile(
                        getApplication(),
                        variantIndex = i,
                        pageCount = pageCounts[i - 1]
                    )
                    loaded.add(PdfEngine.loadPdfFromFile(file, name, nextId(), loadThumbnails = true))
                }
                _uiState.update { state ->
                    val updated = state.mergeItems + loaded
                    state.copy(
                        busy = null,
                        mergeItems = updated,
                        stage = WorkspaceStage.WORK
                    )
                }
            } catch (e: Exception) {
                reportError(e)
            }
        }
    }

    fun moveMergeItem(from: Int, to: Int) {
        _uiState.update { state ->
            val list = state.mergeItems.toMutableList()
            if (from !in list.indices || to !in list.indices) return@update state
            val item = list.removeAt(from)
            list.add(to, item)
            state.copy(mergeItems = list)
        }
    }

    fun sortMergeItemsAZ() {
        _uiState.update { state ->
            state.copy(
                mergeItems = state.mergeItems.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            )
        }
    }

    fun removeMergeItem(index: Int) {
        _uiState.update { state ->
            val list = state.mergeItems.toMutableList()
            if (index in list.indices) list.removeAt(index)
            state.copy(
                mergeItems = list,
                stage = if (list.isEmpty()) WorkspaceStage.UPLOAD else WorkspaceStage.WORK
            )
        }
    }

    fun clearMergeItems() {
        _uiState.update {
            it.copy(mergeItems = emptyList(), stage = WorkspaceStage.UPLOAD)
        }
    }

    fun runMergePdfs() {
        val items = _uiState.value.mergeItems
        if (items.size < 2) {
            showToast(t("errNeed2"), isError = true)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("merging"), 0f)) }
            try {
                val (bytes, totalPages) = PdfEngine.mergePdfs(items) { cur, total ->
                    _uiState.update {
                        it.copy(
                            busy = BusyState(
                                text = t("mergingN", mapOf("n" to cur, "total" to total)),
                                progress = (cur.toFloat() / total.toFloat()) * 0.9f
                            )
                        )
                    }
                }
                _uiState.update { it.copy(busy = BusyState(t("saving"), 0.96f)) }
                publishResult(
                    toolId = ToolId.MERGE,
                    filename = "merged.pdf",
                    mimeType = "application/pdf",
                    bytes = bytes,
                    rows = listOf(
                        "fileName" to "merged.pdf",
                        "fileSize" to fmtBytes(bytes.size.toLong()),
                        "totalPages" to totalPages.toString(),
                        "filesMerged" to items.size.toString()
                    )
                )
            } catch (e: Exception) {
                reportError(e)
            }
        }
    }

    /* =====================================================================
       2) SPLIT PDF ACTIONS
       ===================================================================== */
    fun setSplitMode(mode: String) {
        _uiState.update { it.copy(splitMode = mode) }
    }

    fun setSplitRangesText(text: String) {
        _uiState.update { it.copy(splitRangesText = text) }
    }

    fun setSplitEveryNText(text: String) {
        _uiState.update { it.copy(splitEveryNText = text) }
    }

    fun computeSplitPlan(state: PdfLabUiState = _uiState.value): Result<List<SplitJob>> {
        val info = state.singlePdf ?: return Result.success(emptyList())
        val base = safeName(stripExt(info.name))
        val n = info.pageCount
        val w = n.toString().length
        return when (state.splitMode) {
            "single" -> {
                val jobs = (1..n).map { p ->
                    SplitJob(name = "${base}_page_${padNum(p, w)}.pdf", pages1Based = listOf(p))
                }
                Result.success(jobs)
            }
            "ranges" -> {
                val parsed = parseRanges(state.splitRangesText, n)
                if (parsed.errorToken != null) {
                    return Result.failure(AppError("errRangeTok", mapOf("tok" to parsed.errorToken)))
                }
                if (parsed.groups.isEmpty()) {
                    return Result.failure(AppError("errEnterRanges"))
                }
                val jobs = parsed.groups.mapIndexed { k, g ->
                    val suffix = if (g.size > 1) "-${g.last()}" else ""
                    SplitJob(
                        name = "${base}_part${k + 1}_p${g.first()}$suffix.pdf",
                        pages1Based = g
                    )
                }
                Result.success(jobs)
            }
            else -> {
                val step = state.splitEveryNText.trim().toIntOrNull()
                if (step == null || step < 1) {
                    return Result.failure(AppError("errEveryN"))
                }
                val jobs = mutableListOf<SplitJob>()
                var s = 1
                var k = 1
                while (s <= n) {
                    val e = minOf(n, s + step - 1)
                    val pages = (s..e).toList()
                    val suffix = if (e > s) "-$e" else ""
                    jobs.add(
                        SplitJob(
                            name = "${base}_part${k}_p${s}$suffix.pdf",
                            pages1Based = pages
                        )
                    )
                    s += step
                    k++
                }
                Result.success(jobs)
            }
        }
    }

    fun runSplitPdf() {
        val info = _uiState.value.singlePdf ?: return
        val planRes = computeSplitPlan()
        val jobs = planRes.getOrElse { err ->
            reportError(err)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("splitting"), 0f)) }
            try {
                val zipBytes = PdfEngine.splitPdf(info, jobs) { cur, total ->
                    _uiState.update {
                        it.copy(
                            busy = BusyState(
                                text = t("splittingN", mapOf("n" to cur, "total" to total)),
                                progress = (cur.toFloat() / total.toFloat()) * 0.94f
                            )
                        )
                    }
                }
                val fname = "${safeName(stripExt(info.name))}_split.zip"
                publishResult(
                    toolId = ToolId.SPLIT,
                    filename = fname,
                    mimeType = "application/zip",
                    bytes = zipBytes,
                    rows = listOf(
                        "fileName" to fname,
                        "fileSize" to fmtBytes(zipBytes.size.toLong()),
                        "filesCreated" to jobs.size.toString()
                    )
                )
            } catch (e: Exception) {
                reportError(e)
            }
        }
    }

    /* =====================================================================
       3) COMPRESS PDF (BATCH) ACTIONS
       ===================================================================== */
    fun addCompressPdfsFromUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("reading"))) }
            val newItems = mutableListOf<BatchCompressItem>()
            for ((idx, uri) in uris.withIndex()) {
                val fname = PdfEngine.getFileNameFromUri(getApplication(), uri)
                _uiState.update {
                    it.copy(
                        busy = BusyState(
                            text = t("readingN", mapOf("n" to (idx + 1), "total" to uris.size, "name" to fname)),
                            progress = idx.toFloat() / uris.size.toFloat()
                        )
                    )
                }
                try {
                    val info = PdfEngine.loadPdfFromUri(
                        context = getApplication(),
                        uri = uri,
                        id = nextId(),
                        loadThumbnails = true
                    )
                    newItems.add(BatchCompressItem(id = info.id, info = info))
                } catch (e: Exception) {
                    val msg = if (e is AppError) t(e.key, e.vars) else e.localizedMessage ?: t("errBadPdf")
                    showToast("$fname: $msg", isError = true)
                }
            }
            _uiState.update { state ->
                val combined = state.compressItems + newItems
                state.copy(
                    busy = null,
                    compressItems = combined,
                    stage = if (combined.isNotEmpty()) WorkspaceStage.WORK else WorkspaceStage.UPLOAD
                )
            }
        }
    }

    fun loadSampleCompressPdfs() {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("reading"))) }
            try {
                val items = mutableListOf<BatchCompressItem>()
                for (i in 1..2) {
                    val (file, name) = PdfEngine.createSamplePdfFile(getApplication(), variantIndex = i, pageCount = 5)
                    val info = PdfEngine.loadPdfFromFile(file, name, nextId(), loadThumbnails = true)
                    items.add(BatchCompressItem(id = info.id, info = info))
                }
                _uiState.update { state ->
                    val combined = state.compressItems + items
                    state.copy(
                        busy = null,
                        compressItems = combined,
                        stage = WorkspaceStage.WORK
                    )
                }
            } catch (e: Exception) {
                reportError(e)
            }
        }
    }

    fun selectCompressPreset(preset: String) {
        val (dpi, q) = when (preset) {
            "recommended" -> "144" to "75"
            "extreme" -> "72" to "50"
            "low" -> "200" to "85"
            else -> _uiState.value.compressDpiText to _uiState.value.compressQualityText
        }
        _uiState.update {
            it.copy(
                compressPreset = preset,
                compressDpiText = dpi,
                compressQualityText = q
            )
        }
    }

    fun updateCompressDpi(text: String) {
        _uiState.update { it.copy(compressPreset = "custom", compressDpiText = text) }
    }

    fun updateCompressQuality(text: String) {
        _uiState.update { it.copy(compressPreset = "custom", compressQualityText = text) }
    }

    fun setCompressStripMetadata(strip: Boolean) {
        _uiState.update { it.copy(compressStripMetadata = strip) }
    }

    fun removeCompressItem(id: Long) {
        if (_uiState.value.isBatchRunning) return
        _uiState.update { state ->
            val updated = state.compressItems.filterNot { it.id == id }
            state.copy(
                compressItems = updated,
                stage = if (updated.isEmpty()) WorkspaceStage.UPLOAD else WorkspaceStage.WORK
            )
        }
    }

    fun clearCompressItems() {
        if (_uiState.value.isBatchRunning) return
        _uiState.update {
            it.copy(compressItems = emptyList(), stage = WorkspaceStage.UPLOAD)
        }
    }

    fun requestStopCompressBatch() {
        _uiState.update {
            it.copy(stopBatchRequested = true, batchProgressLabel = t("stopping"))
        }
    }

    fun runCompressBatch(recompressAll: Boolean) {
        val state = _uiState.value
        if (state.isBatchRunning) return
        val dpi = state.compressDpiText.trim().toIntOrNull()
        val q = state.compressQualityText.trim().toIntOrNull()
        if (dpi == null || dpi !in 50..300 || q == null || q !in 10..100) {
            showToast(
                t("errCmpValues", mapOf("dmin" to 50, "dmax" to 300, "qmin" to 10, "qmax" to 100)),
                isError = true
            )
            return
        }

        viewModelScope.launch {
            if (recompressAll) {
                _uiState.update { st ->
                    st.copy(
                        compressItems = st.compressItems.map {
                            it.copy(
                                status = BatchStatus.PENDING,
                                progress = 0f,
                                compressedBytes = null,
                                compressedFileName = null,
                                alreadyOptimized = false,
                                errorMsg = null
                            )
                        }
                    )
                }
            }
            val queueIds = _uiState.value.compressItems
                .filter { it.status != BatchStatus.DONE }
                .map { it.id }

            if (queueIds.isEmpty()) return@launch

            _uiState.update {
                it.copy(
                    isBatchRunning = true,
                    stopBatchRequested = false,
                    batchOverallProgress = 0f
                )
            }

            for ((idx, itemId) in queueIds.withIndex()) {
                if (_uiState.value.stopBatchRequested) break
                val currentItem = _uiState.value.compressItems.find { it.id == itemId } ?: continue

                _uiState.update { st ->
                    st.copy(
                        batchProgressLabel = t(
                            "batchProgress",
                            mapOf("n" to (idx + 1), "total" to queueIds.size, "name" to currentItem.info.name)
                        ),
                        compressItems = st.compressItems.map {
                            if (it.id == itemId) it.copy(status = BatchStatus.COMPRESSING, progress = 0.04f) else it
                        }
                    )
                }

                try {
                    val res = PdfEngine.compressPdf(
                        info = currentItem.info,
                        dpi = dpi,
                        quality = q,
                        stripMetadata = _uiState.value.compressStripMetadata
                    ) { frac, _, _ ->
                        _uiState.update { st ->
                            val overall = (idx.toFloat() + frac) / queueIds.size.toFloat()
                            st.copy(
                                batchOverallProgress = overall,
                                compressItems = st.compressItems.map {
                                    if (it.id == itemId) it.copy(progress = frac) else it
                                }
                            )
                        }
                    }

                    val outName = "${safeName(stripExt(currentItem.info.name))}_compressed.pdf"
                    _uiState.update { st ->
                        st.copy(
                            batchOverallProgress = (idx + 1).toFloat() / queueIds.size.toFloat(),
                            compressItems = st.compressItems.map {
                                if (it.id == itemId) {
                                    it.copy(
                                        status = BatchStatus.DONE,
                                        progress = 1f,
                                        compressedBytes = res.bytes,
                                        compressedFileName = outName,
                                        alreadyOptimized = res.alreadyOptimized
                                    )
                                } else it
                            }
                        )
                    }
                } catch (e: Exception) {
                    val errText = if (e is AppError) t(e.key, e.vars) else e.localizedMessage ?: t("errGeneric")
                    _uiState.update { st ->
                        st.copy(
                            compressItems = st.compressItems.map {
                                if (it.id == itemId) it.copy(status = BatchStatus.FAILED, errorMsg = errText) else it
                            }
                        )
                    }
                }
            }

            val finalState = _uiState.value
            val doneItems = finalState.compressItems.filter { it.status == BatchStatus.DONE }
            _uiState.update {
                it.copy(
                    isBatchRunning = false,
                    stopBatchRequested = false,
                    batchProgressLabel = ""
                )
            }

            // If exactly 1 item in the list and it succeeded, show the full Result screen just like single-file flow
            if (finalState.compressItems.size == 1 && doneItems.size == 1) {
                val single = doneItems.first()
                val bytes = single.compressedBytes ?: return@launch
                val fname = single.compressedFileName ?: "compressed.pdf"
                val origLen = single.info.size
                val newLen = bytes.size.toLong()
                val savedPct = max(0, ((1.0 - newLen.toDouble() / max(1L, origLen).toDouble()) * 100.0).roundToInt())
                publishResult(
                    toolId = ToolId.COMPRESS,
                    filename = fname,
                    mimeType = "application/pdf",
                    bytes = bytes,
                    rows = listOf(
                        "fileName" to fname,
                        "origSize" to fmtBytes(origLen),
                        "newSize" to fmtBytes(newLen),
                        "savedLabel" to "$savedPct%"
                    ),
                    notes = if (single.alreadyOptimized) listOf("alreadyOptimized") else emptyList()
                )
            } else {
                val msg = if (finalState.stopBatchRequested) {
                    t("batchStopped", mapOf("ok" to doneItems.size))
                } else {
                    t("batchFinished", mapOf("ok" to doneItems.size, "total" to finalState.compressItems.size))
                }
                showToast(msg, isError = doneItems.isEmpty())
            }
        }
    }

    fun openSingleCompressedResult(item: BatchCompressItem) {
        val bytes = item.compressedBytes ?: return
        val fname = item.compressedFileName ?: "compressed.pdf"
        val origLen = item.info.size
        val newLen = bytes.size.toLong()
        val savedPct = max(0, ((1.0 - newLen.toDouble() / max(1L, origLen).toDouble()) * 100.0).roundToInt())
        viewModelScope.launch {
            publishResult(
                toolId = ToolId.COMPRESS,
                filename = fname,
                mimeType = "application/pdf",
                bytes = bytes,
                rows = listOf(
                    "fileName" to fname,
                    "origSize" to fmtBytes(origLen),
                    "newSize" to fmtBytes(newLen),
                    "savedLabel" to "$savedPct%"
                ),
                notes = if (item.alreadyOptimized) listOf("alreadyOptimized") else emptyList()
            )
        }
    }

    fun exportAllCompressedAsZip() {
        val done = _uiState.value.compressItems.filter { it.status == BatchStatus.DONE && it.compressedBytes != null }
        if (done.isEmpty()) {
            showToast(t("noneToZip"), isError = true)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("zipBuilding"))) }
            try {
                val entries = done.map {
                    ZipFileEntry(
                        name = it.compressedFileName ?: "${safeName(stripExt(it.info.name))}_compressed.pdf",
                        data = it.compressedBytes!!
                    )
                }
                val zipBytes = withContext(Dispatchers.IO) { PdfEngine.buildZip(entries) }
                val totalOrig = done.sumOf { it.info.size }
                val totalNew = done.sumOf { it.compressedBytes!!.size.toLong() }
                val pct = max(0, ((1.0 - totalNew.toDouble() / max(1L, totalOrig).toDouble()) * 100.0).roundToInt())
                publishResult(
                    toolId = ToolId.COMPRESS,
                    filename = "compressed_pdfs.zip",
                    mimeType = "application/zip",
                    bytes = zipBytes,
                    rows = listOf(
                        "fileName" to "compressed_pdfs.zip",
                        "filesCreated" to done.size.toString(),
                        "origSize" to fmtBytes(totalOrig),
                        "newSize" to fmtBytes(totalNew),
                        "savedLabel" to "$pct%"
                    )
                )
            } catch (e: Exception) {
                reportError(e)
            }
        }
    }

    /* =====================================================================
       4) IMAGES -> PDF ACTIONS
       ===================================================================== */
    fun addImagesFromUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("reading"))) }
            val loaded = mutableListOf<LoadedImageItem>()
            for (uri in uris) {
                try {
                    loaded.add(PdfEngine.loadImageFromUri(getApplication(), uri, nextId()))
                } catch (e: Exception) {
                    reportError(e)
                }
            }
            _uiState.update { state ->
                val combined = state.imageItems + loaded
                state.copy(
                    busy = null,
                    imageItems = combined,
                    stage = if (combined.isNotEmpty()) WorkspaceStage.WORK else WorkspaceStage.UPLOAD
                )
            }
        }
    }

    fun loadSampleImagesForPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("reading"))) }
            try {
                val files = PdfEngine.createSampleImages(getApplication())
                val loaded = files.map { (file, name) ->
                    PdfEngine.loadImageFromFile(file, name, nextId())
                }
                _uiState.update { state ->
                    val combined = state.imageItems + loaded
                    state.copy(
                        busy = null,
                        imageItems = combined,
                        stage = WorkspaceStage.WORK
                    )
                }
            } catch (e: Exception) {
                reportError(e)
            }
        }
    }

    fun moveImageItem(from: Int, to: Int) {
        _uiState.update { state ->
            val list = state.imageItems.toMutableList()
            if (from !in list.indices || to !in list.indices) return@update state
            val item = list.removeAt(from)
            list.add(to, item)
            state.copy(imageItems = list)
        }
    }

    fun removeImageItem(index: Int) {
        _uiState.update { state ->
            val list = state.imageItems.toMutableList()
            if (index in list.indices) list.removeAt(index)
            state.copy(
                imageItems = list,
                stage = if (list.isEmpty()) WorkspaceStage.UPLOAD else WorkspaceStage.WORK
            )
        }
    }

    fun clearAllImages() {
        _uiState.update {
            it.copy(imageItems = emptyList(), stage = WorkspaceStage.UPLOAD)
        }
    }

    fun setImgPageSize(size: String) {
        _uiState.update { it.copy(imgPageSize = size) }
    }

    fun setImgOrientation(orientation: String) {
        _uiState.update { it.copy(imgOrientation = orientation) }
    }

    fun setImgMarginMm(marginMm: Int) {
        _uiState.update { it.copy(imgMarginMm = marginMm) }
    }

    fun runImagesToPdf() {
        val state = _uiState.value
        if (state.imageItems.isEmpty()) {
            showToast(t("errNoImages"), isError = true)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("converting"), 0f)) }
            try {
                val pdfBytes = PdfEngine.imagesToPdf(
                    items = state.imageItems,
                    sizeMode = state.imgPageSize,
                    orientationMode = state.imgOrientation,
                    marginMm = state.imgMarginMm
                ) { cur, total ->
                    _uiState.update {
                        it.copy(
                            busy = BusyState(
                                text = t("convertingN", mapOf("n" to cur, "total" to total)),
                                progress = (cur.toFloat() / total.toFloat()) * 0.92f
                            )
                        )
                    }
                }
                publishResult(
                    toolId = ToolId.IMG2PDF,
                    filename = "images.pdf",
                    mimeType = "application/pdf",
                    bytes = pdfBytes,
                    rows = listOf(
                        "fileName" to "images.pdf",
                        "fileSize" to fmtBytes(pdfBytes.size.toLong()),
                        "totalPages" to state.imageItems.size.toString()
                    )
                )
            } catch (e: Exception) {
                reportError(e)
            }
        }
    }

    /* =====================================================================
       5) PDF -> IMAGES ACTIONS
       ===================================================================== */
    fun setP2iFormat(format: String) {
        _uiState.update { it.copy(p2iFormat = format) }
    }

    fun setP2iDpi(dpi: Int) {
        _uiState.update { it.copy(p2iDpi = dpi) }
    }

    fun setP2iPagesText(text: String) {
        _uiState.update { it.copy(p2iPagesText = text) }
    }

    fun runPdfToImages() {
        val state = _uiState.value
        val info = state.singlePdf ?: return
        val pages1Based = if (state.p2iPagesText.isNotBlank()) {
            val parsed = parseRanges(state.p2iPagesText, info.pageCount)
            if (parsed.errorToken != null) {
                showToast(t("errRangeTok", mapOf("tok" to parsed.errorToken)), isError = true)
                return
            }
            flattenGroups(parsed.groups)
        } else {
            (1..info.pageCount).toList()
        }
        if (pages1Based.isEmpty()) {
            showToast(t("errEnterRanges"), isError = true)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("rendering"), 0f)) }
            try {
                val zipBytes = PdfEngine.pdfToImagesZip(
                    info = info,
                    pages1Based = pages1Based,
                    format = state.p2iFormat,
                    dpi = state.p2iDpi
                ) { cur, total ->
                    _uiState.update {
                        it.copy(
                            busy = BusyState(
                                text = t("renderingN", mapOf("n" to cur, "total" to total)),
                                progress = (cur.toFloat() / total.toFloat()) * 0.94f
                            )
                        )
                    }
                }
                val extLabel = if (state.p2iFormat == "png") "PNG" else "JPG"
                val fname = "${safeName(stripExt(info.name))}_images.zip"
                publishResult(
                    toolId = ToolId.PDF2IMG,
                    filename = fname,
                    mimeType = "application/zip",
                    bytes = zipBytes,
                    rows = listOf(
                        "fileName" to fname,
                        "fileSize" to fmtBytes(zipBytes.size.toLong()),
                        "imagesCreated" to pages1Based.size.toString(),
                        "resolution" to "${state.p2iDpi} DPI · $extLabel"
                    )
                )
            } catch (e: Exception) {
                reportError(e)
            }
        }
    }

    /* =====================================================================
       6) ROTATE PDF PAGES ACTIONS
       ===================================================================== */
    fun rotatePage(pageIndex0: Int, deltaDeg: Int) {
        _uiState.update { state ->
            val current = state.pageRotations.toMutableList()
            if (pageIndex0 !in current.indices) return@update state
            current[pageIndex0] = (((current[pageIndex0] + deltaDeg) % 360) + 360) % 360
            state.copy(pageRotations = current)
        }
    }

    fun rotateAllPages(deltaDeg: Int) {
        _uiState.update { state ->
            val updated = state.pageRotations.map { (((it + deltaDeg) % 360) + 360) % 360 }
            state.copy(pageRotations = updated)
        }
    }

    fun resetAllRotations() {
        _uiState.update { state ->
            state.copy(pageRotations = List(state.pageRotations.size) { 0 })
        }
    }

    fun runRotatePdf() {
        val state = _uiState.value
        val info = state.singlePdf ?: return
        if (state.pageRotations.none { it != 0 }) {
            showToast(t("errNoRotation"), isError = true)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(busy = BusyState(t("saving"))) }
            try {
                val outBytes = PdfEngine.rotatePdf(info, state.pageRotations)
                val fname = "${safeName(stripExt(info.name))}_rotated.pdf"
                publishResult(
                    toolId = ToolId.ROTATE,
                    filename = fname,
                    mimeType = "application/pdf",
                    bytes = outBytes,
                    rows = listOf(
                        "fileName" to fname,
                        "fileSize" to fmtBytes(outBytes.size.toLong()),
                        "totalPages" to info.pageCount.toString()
                    )
                )
            } catch (e: Exception) {
                reportError(e)
            }
        }
    }

    /* =====================================================================
       7) REMOVE PAGES & 8) EXTRACT PAGES ACTIONS
       ===================================================================== */
    fun togglePageSelection(page1Based: Int) {
        _uiState.update { state ->
            val next = state.selectedPages1Based.toMutableSet()
            if (!next.add(page1Based)) {
                next.remove(page1Based)
            }
            state.copy(
                selectedPages1Based = next,
                selectionRangeText = compactRanges(next),
                selectionRangeError = null
            )
        }
    }

    fun updateSelectionRangeText(text: String) {
        val info = _uiState.value.singlePdf ?: return
        if (text.isBlank()) {
            _uiState.update {
                it.copy(
                    selectionRangeText = text,
                    selectedPages1Based = emptySet(),
                    selectionRangeError = null
                )
            }
            return
        }
        val parsed = parseRanges(text, info.pageCount)
        if (parsed.errorToken != null) {
            _uiState.update {
                it.copy(
                    selectionRangeText = text,
                    selectionRangeError = t("errRangeTok", mapOf("tok" to parsed.errorToken))
                )
            }
        } else {
            val pages = flattenGroups(parsed.groups).toSet()
            _uiState.update {
                it.copy(
                    selectionRangeText = text,
                    selectedPages1Based = pages,
                    selectionRangeError = null
                )
            }
        }
    }

    fun selectPagesWhere(predicate: (Int) -> Boolean) {
        val info = _uiState.value.singlePdf ?: return
        val next = (1..info.pageCount).filter(predicate).toSet()
        _uiState.update {
            it.copy(
                selectedPages1Based = next,
                selectionRangeText = compactRanges(next),
                selectionRangeError = null
            )
        }
    }

    fun setExtractMode(mode: String) {
        _uiState.update { it.copy(extractMode = mode) }
    }

    fun runRemoveOrExtract() {
        val state = _uiState.value
        val info = state.singlePdf ?: return
        val isRemove = state.activeTool == ToolId.REMOVE
        if (state.selectionRangeError != null) {
            showToast(state.selectionRangeError, isError = true)
            return
        }
        val chosen = state.selectedPages1Based.sorted()
        if (chosen.isEmpty()) {
            showToast(t(if (isRemove) "errSelDel" else "errSelExt"), isError = true)
            return
        }
        val base = safeName(stripExt(info.name))

        viewModelScope.launch {
            try {
                if (isRemove) {
                    if (chosen.size >= info.pageCount) {
                        showToast(t("errDelAll"), isError = true)
                        return@launch
                    }
                    _uiState.update { it.copy(busy = BusyState(t("building"))) }
                    val keep = (1..info.pageCount).filterNot { state.selectedPages1Based.contains(it) }
                    val bytes = PdfEngine.buildSubsetPdf(info, keep)
                    val fname = "${base}_removed.pdf"
                    publishResult(
                        toolId = ToolId.REMOVE,
                        filename = fname,
                        mimeType = "application/pdf",
                        bytes = bytes,
                        rows = listOf(
                            "fileName" to fname,
                            "fileSize" to fmtBytes(bytes.size.toLong()),
                            "pagesRemoved" to chosen.size.toString(),
                            "totalPages" to keep.size.toString()
                        )
                    )
                } else if (state.extractMode == "single") {
                    _uiState.update { it.copy(busy = BusyState(t("building"))) }
                    val bytes = PdfEngine.buildSubsetPdf(info, chosen)
                    val fname = "${base}_extracted.pdf"
                    publishResult(
                        toolId = ToolId.EXTRACT,
                        filename = fname,
                        mimeType = "application/pdf",
                        bytes = bytes,
                        rows = listOf(
                            "fileName" to fname,
                            "fileSize" to fmtBytes(bytes.size.toLong()),
                            "totalPages" to chosen.size.toString()
                        )
                    )
                } else {
                    _uiState.update { it.copy(busy = BusyState(t("building"), 0f)) }
                    val zipBytes = PdfEngine.extractSeparateZip(info, chosen) { cur, total ->
                        _uiState.update {
                            it.copy(
                                busy = BusyState(
                                    text = t("splittingN", mapOf("n" to cur, "total" to total)),
                                    progress = (cur.toFloat() / total.toFloat()) * 0.94f
                                )
                            )
                        }
                    }
                    val fname = "${base}_extracted.zip"
                    publishResult(
                        toolId = ToolId.EXTRACT,
                        filename = fname,
                        mimeType = "application/zip",
                        bytes = zipBytes,
                        rows = listOf(
                            "fileName" to fname,
                            "fileSize" to fmtBytes(zipBytes.size.toLong()),
                            "filesCreated" to chosen.size.toString()
                        )
                    )
                }
            } catch (e: Exception) {
                reportError(e)
            }
        }
    }
}
