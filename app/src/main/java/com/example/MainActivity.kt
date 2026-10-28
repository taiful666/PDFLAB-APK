package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ToolId
import com.example.ui.PdfLabViewModel
import com.example.ui.ResultOutput
import com.example.ui.components.BusyModalOverlay
import com.example.ui.components.DeskMateTopBar
import com.example.ui.components.OutputPreviewDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ToolWorkspaceScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PdfLabViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = state.isDarkTheme) {
                PdfLabAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PdfLabAppRoot(
    viewModel: PdfLabViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingSaveOutput by remember { mutableStateOf<ResultOutput?>(null) }

    // 1. Single PDF Picker (for Split, PDF->Images, Rotate, Remove, Extract)
    val openSinglePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.loadSinglePdfFromUri(uri)
        }
    }

    // 2. Multiple PDF Picker (for Merge & Batch Compress)
    val openMultiplePdfsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            when (state.activeTool) {
                ToolId.MERGE -> viewModel.addMergePdfsFromUris(uris)
                ToolId.COMPRESS -> viewModel.addCompressPdfsFromUris(uris)
                else -> {}
            }
        }
    }

    // 3. Multiple Visual Media Picker (Zero-permission Android Photo Picker for Images -> PDF)
    val pickMultipleImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 50)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImagesFromUris(uris)
        }
    }

    // 4. CreateDocument SAF Launcher for saving output PDF / ZIP anywhere on device
    val saveDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        val target = pendingSaveOutput
        if (uri != null && target != null) {
            viewModel.writeOutputToDocumentUri(uri, target)
        }
        pendingSaveOutput = null
    }

    fun triggerSaveOutput(output: ResultOutput) {
        pendingSaveOutput = output
        saveDocumentLauncher.launch(output.filename)
    }

    fun launchPickerForTool(tool: ToolId) {
        when (tool) {
            ToolId.MERGE, ToolId.COMPRESS -> {
                openMultiplePdfsLauncher.launch(arrayOf("application/pdf"))
            }
            ToolId.IMG2PDF -> {
                pickMultipleImagesLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            else -> {
                openSinglePdfLauncher.launch(arrayOf("application/pdf"))
            }
        }
    }

    fun loadSampleForTool(tool: ToolId) {
        when (tool) {
            ToolId.MERGE -> viewModel.loadSampleMergePdfs()
            ToolId.COMPRESS -> viewModel.loadSampleCompressPdfs()
            ToolId.IMG2PDF -> viewModel.loadSampleImagesForPdf()
            else -> viewModel.loadSampleSinglePdf()
        }
    }

    // Show toast / snackbar messages
    LaunchedEffect(state.toast?.id) {
        val toast = state.toast ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(toast.text)
        viewModel.dismissToast()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            DeskMateTopBar(
                lang = state.lang,
                isDarkTheme = state.isDarkTheme,
                showBackButton = state.activeTool != null,
                onBackClick = { viewModel.navigateToDashboard() },
                onLogoClick = { viewModel.navigateToDashboard() },
                onLangChange = { viewModel.setLanguage(it) },
                onToggleTheme = { viewModel.toggleTheme() }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isErr = state.toast?.isError == true
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = if (isErr) Color(0xFF991B1B) else MaterialTheme.colorScheme.inverseSurface,
                    contentColor = if (isErr) Color.White else MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = state.activeTool,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenRouter"
            ) { activeTool ->
                if (activeTool == null) {
                    DashboardScreen(
                        state = state,
                        onOpenTool = { viewModel.openTool(it) },
                        onSelectCategory = { viewModel.setCategory(it) },
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onPreviewOutput = { viewModel.openPreviewDialog(it) },
                        onSaveOutput = { triggerSaveOutput(it) },
                        onShareOutput = { viewModel.shareOutputFile(context, it) },
                        onClearRecent = { viewModel.clearRecentOutputs() },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ToolWorkspaceScreen(
                        state = state,
                        toolId = activeTool,
                        splitPlanResult = viewModel.computeSplitPlan(state),
                        onBackToDashboard = { viewModel.navigateToDashboard() },
                        onLaunchFilePicker = { launchPickerForTool(activeTool) },
                        onLoadSampleFiles = { loadSampleForTool(activeTool) },
                        onStartOver = { viewModel.startOverCurrentTool() },
                        onSaveOutput = { triggerSaveOutput(it) },
                        onPreviewOutput = { viewModel.openPreviewDialog(it) },
                        onShareOutput = { viewModel.shareOutputFile(context, it) },
                        // Merge
                        onSortMergeAZ = { viewModel.sortMergeItemsAZ() },
                        onMoveMergeItem = { from, to -> viewModel.moveMergeItem(from, to) },
                        onRemoveMergeItem = { viewModel.removeMergeItem(it) },
                        onRunMerge = { viewModel.runMergePdfs() },
                        // Split
                        onSplitModeChange = { viewModel.setSplitMode(it) },
                        onSplitRangesChange = { viewModel.setSplitRangesText(it) },
                        onSplitEveryNChange = { viewModel.setSplitEveryNText(it) },
                        onRunSplit = { viewModel.runSplitPdf() },
                        // Compress
                        onRemoveCompressItem = { viewModel.removeCompressItem(it) },
                        onOpenSingleCompressed = { viewModel.openSingleCompressedResult(it) },
                        onSelectCompressPreset = { viewModel.selectCompressPreset(it) },
                        onCompressDpiChange = { viewModel.updateCompressDpi(it) },
                        onCompressQualityChange = { viewModel.updateCompressQuality(it) },
                        onCompressStripChange = { viewModel.setCompressStripMetadata(it) },
                        onRunCompressBatch = { recompress -> viewModel.runCompressBatch(recompress) },
                        onStopCompressBatch = { viewModel.requestStopCompressBatch() },
                        onDownloadAllCompressedZip = { viewModel.exportAllCompressedAsZip() },
                        // Images -> PDF
                        onMoveImageItem = { from, to -> viewModel.moveImageItem(from, to) },
                        onRemoveImageItem = { viewModel.removeImageItem(it) },
                        onImgPageSizeChange = { viewModel.setImgPageSize(it) },
                        onImgOrientationChange = { viewModel.setImgOrientation(it) },
                        onImgMarginChange = { viewModel.setImgMarginMm(it) },
                        onRunImagesToPdf = { viewModel.runImagesToPdf() },
                        // PDF -> Images
                        onP2iFormatChange = { viewModel.setP2iFormat(it) },
                        onP2iDpiChange = { viewModel.setP2iDpi(it) },
                        onP2iPagesTextChange = { viewModel.setP2iPagesText(it) },
                        onRunPdfToImages = { viewModel.runPdfToImages() },
                        // Rotate
                        onRotatePage = { idx, delta -> viewModel.rotatePage(idx, delta) },
                        onRotateAllPages = { delta -> viewModel.rotateAllPages(delta) },
                        onResetAllRotations = { viewModel.resetAllRotations() },
                        onRunRotatePdf = { viewModel.runRotatePdf() },
                        // Remove / Extract
                        onTogglePageSelection = { viewModel.togglePageSelection(it) },
                        onSelectionRangeTextChange = { viewModel.updateSelectionRangeText(it) },
                        onSelectEvenPages = { viewModel.selectPagesWhere { it % 2 == 0 } },
                        onSelectOddPages = { viewModel.selectPagesWhere { it % 2 == 1 } },
                        onSelectAllPages = { viewModel.selectPagesWhere { true } },
                        onClearPageSelection = { viewModel.selectPagesWhere { false } },
                        onExtractModeChange = { viewModel.setExtractMode(it) },
                        onRunRemoveOrExtract = { viewModel.runRemoveOrExtract() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Busy Modal Overlay
            val busy = state.busy
            if (busy != null) {
                BusyModalOverlay(
                    lang = state.lang,
                    busy = busy
                )
            }

            // Output Preview Dialog (PDF pages or ZIP entries)
            val previewTarget = state.previewTarget
            if (previewTarget != null) {
                OutputPreviewDialog(
                    lang = state.lang,
                    output = previewTarget,
                    pdfThumbnails = state.previewPdfThumbnails,
                    zipEntries = state.previewZipEntries,
                    onDismiss = { viewModel.closePreviewDialog() },
                    onSave = {
                        viewModel.closePreviewDialog()
                        triggerSaveOutput(previewTarget)
                    },
                    onShare = {
                        viewModel.shareOutputFile(context, previewTarget)
                    }
                )
            }
        }
    }
}
