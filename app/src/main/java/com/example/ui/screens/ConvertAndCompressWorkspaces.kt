package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLang
import com.example.data.I18n
import com.example.data.SplitJob
import com.example.data.ToolId
import com.example.data.fmtBytes
import com.example.ui.BatchCompressItem
import com.example.ui.BatchStatus
import com.example.ui.PdfLabUiState
import com.example.ui.ResultOutput
import com.example.ui.WorkspaceStage
import com.example.ui.components.ActiveFileBar
import com.example.ui.components.RadioChoiceCard
import com.example.ui.components.ResultStageCard
import com.example.ui.components.ToolIconBadge
import com.example.ui.components.UploadDropzoneCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.max
import kotlin.math.roundToInt

/* =====================================================================
   3) COMPRESS PDF (BATCH) WORKSPACE
   ===================================================================== */
@Composable
fun CompressWorkView(
    state: PdfLabUiState,
    onAddMoreClick: () -> Unit,
    onClearAll: () -> Unit,
    onRemoveItem: (Long) -> Unit,
    onOpenSingleCompressed: (BatchCompressItem) -> Unit,
    onSelectPreset: (String) -> Unit,
    onDpiChange: (String) -> Unit,
    onQualityChange: (String) -> Unit,
    onStripChange: (Boolean) -> Unit,
    onRunBatch: (Boolean) -> Unit,
    onStopBatch: () -> Unit,
    onDownloadAllZip: () -> Unit
) {
    val lang = state.lang
    val scheme = MaterialTheme.colorScheme
    val accent = ToolId.COMPRESS.color

    val doneItems = state.compressItems.filter { it.status == BatchStatus.DONE }
    val pendingOrFailedCount = state.compressItems.count { it.status != BatchStatus.DONE }
    val totalOrigSize = state.compressItems.sumOf { it.info.size }

    val summaryText = if (doneItems.isNotEmpty()) {
        val doneOrig = doneItems.sumOf { it.info.size }
        val doneNew = doneItems.sumOf { (it.compressedBytes?.size ?: 0).toLong() }
        val pct = max(0, ((1.0 - doneNew.toDouble() / max(1L, doneOrig).toDouble()) * 100.0).roundToInt())
        I18n.t(
            lang,
            "batchDone",
            mapOf(
                "ok" to doneItems.size,
                "total" to state.compressItems.size,
                "orig" to fmtBytes(doneOrig),
                "now" to fmtBytes(doneNew),
                "pct" to pct
            )
        )
    } else {
        I18n.t(
            lang,
            "batchSummary",
            mapOf("n" to state.compressItems.size, "size" to fmtBytes(totalOrigSize))
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onAddMoreClick,
                enabled = !state.isBatchRunning,
                shape = RoundedCornerShape(11.dp),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(I18n.t(lang, "addMore"))
            }

            OutlinedButton(
                onClick = onClearAll,
                enabled = !state.isBatchRunning,
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error),
                border = BorderStroke(1.dp, scheme.error.copy(alpha = 0.4f)),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(I18n.t(lang, "clearAll"))
            }
        }

        Text(
            text = I18n.t(lang, "batchHint"),
            style = MaterialTheme.typography.bodySmall.copy(color = scheme.onSurfaceVariant)
        )

        // Batch File List
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.compressItems.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, scheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (item.info.thumbnails.isNotEmpty()) {
                                Image(
                                    bitmap = item.info.thumbnails.first().asImageBitmap(),
                                    contentDescription = item.info.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(38.dp)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                        .border(1.dp, scheme.outline, RoundedCornerShape(6.dp))
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.info.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val metaStr = if (item.status == BatchStatus.DONE && item.compressedBytes != null) {
                                    "${fmtBytes(item.info.size)} → ${fmtBytes(item.compressedBytes.size.toLong())} · ${item.info.pageCount} ${I18n.t(lang, "pagesWord")}"
                                } else {
                                    "${fmtBytes(item.info.size)} · ${item.info.pageCount} ${I18n.t(lang, "pagesWord")}"
                                }
                                Text(
                                    text = metaStr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = scheme.onSurfaceVariant,
                                        fontFamily = JetBrainsMonoFontFamily
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                when (item.status) {
                                    BatchStatus.PENDING -> {
                                        Text(
                                            text = I18n.t(lang, "stPending"),
                                            style = MaterialTheme.typography.labelSmall.copy(color = scheme.onSurfaceVariant)
                                        )
                                    }
                                    BatchStatus.COMPRESSING -> {
                                        LinearProgressIndicator(
                                            progress = { item.progress.coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    BatchStatus.DONE -> {
                                        val orig = item.info.size
                                        val now = (item.compressedBytes?.size ?: 0).toLong()
                                        val pct = max(0, ((1.0 - now.toDouble() / max(1L, orig).toDouble()) * 100.0).roundToInt())
                                        val badgeLabel = if (item.alreadyOptimized || pct == 0) {
                                            I18n.t(lang, "optAlready")
                                        } else {
                                            I18n.t(lang, "savedPct", mapOf("pct" to pct))
                                        }
                                        Text(
                                            text = badgeLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = accent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    BatchStatus.FAILED -> {
                                        Text(
                                            text = item.errorMsg ?: I18n.t(lang, "stFailed"),
                                            style = MaterialTheme.typography.labelSmall.copy(color = scheme.error)
                                        )
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.status == BatchStatus.DONE) {
                                IconButton(
                                    onClick = { onOpenSingleCompressed(item) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = I18n.t(lang, "download"),
                                        tint = accent
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onRemoveItem(item.id) },
                                enabled = !state.isBatchRunning,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = I18n.t(lang, "remove"),
                                    tint = scheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        // Compression Presets
        Text(
            text = I18n.t(lang, "cmpLevel"),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        val presets = listOf(
            Triple("recommended", "cmpRec", "cmpRecD"),
            Triple("extreme", "cmpExt", "cmpExtD"),
            Triple("low", "cmpLow", "cmpLowD"),
            Triple("custom", "cmpCustom", "cmpCustomD")
        )
        presets.forEach { (value, titleKey, descKey) ->
            RadioChoiceCard(
                selected = state.compressPreset == value,
                title = I18n.t(lang, titleKey),
                description = I18n.t(lang, descKey),
                accentColor = accent,
                onClick = { onSelectPreset(value) }
            )
        }

        // Manual DPI + Quality + Strip Metadata
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = scheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, scheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.compressDpiText,
                        onValueChange = onDpiChange,
                        label = { Text(I18n.t(lang, "dpiLabel")) },
                        suffix = { Text("DPI") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("compress_dpi_input")
                    )
                    OutlinedTextField(
                        value = state.compressQualityText,
                        onValueChange = onQualityChange,
                        label = { Text(I18n.t(lang, "qualityLabel")) },
                        suffix = { Text("%") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("compress_quality_input")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onStripChange(!state.compressStripMetadata) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = state.compressStripMetadata,
                        onCheckedChange = onStripChange
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = I18n.t(lang, "stripLabel"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = I18n.t(lang, "stripHint"),
                            style = MaterialTheme.typography.bodySmall.copy(color = scheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }

        Text(
            text = I18n.t(lang, "cmpNote"),
            style = MaterialTheme.typography.bodySmall.copy(color = scheme.onSurfaceVariant)
        )

        // Batch Overall Progress (if running)
        if (state.isBatchRunning) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = scheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.batchProgressLabel,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(state.batchOverallProgress.coerceIn(0f, 1f) * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    LinearProgressIndicator(
                        progress = { state.batchOverallProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        // Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = scheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, scheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (state.isBatchRunning) {
                    OutlinedButton(
                        onClick = onStopBatch,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(I18n.t(lang, "stopBtn"))
                    }
                } else {
                    if (pendingOrFailedCount > 0) {
                        Button(
                            onClick = { onRunBatch(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("run_compress_button")
                        ) {
                            Text(
                                text = I18n.t(lang, "compressAll"),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    if (doneItems.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onRunBatch(true) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(I18n.t(lang, "compressAgain"))
                            }

                            if (doneItems.size > 1) {
                                Button(
                                    onClick = onDownloadAllZip,
                                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(I18n.t(lang, "dlAllZip"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* =====================================================================
   4) IMAGES -> PDF WORKSPACE
   ===================================================================== */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImagesToPdfWorkView(
    state: PdfLabUiState,
    onAddMoreImages: () -> Unit,
    onClearAll: () -> Unit,
    onMoveImage: (Int, Int) -> Unit,
    onRemoveImage: (Int) -> Unit,
    onPageSizeChange: (String) -> Unit,
    onOrientationChange: (String) -> Unit,
    onMarginChange: (Int) -> Unit,
    onRunConvert: () -> Unit
) {
    val lang = state.lang
    val scheme = MaterialTheme.colorScheme
    val accent = ToolId.IMG2PDF.color

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onAddMoreImages,
                shape = RoundedCornerShape(11.dp),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(I18n.t(lang, "addMoreImg"))
            }

            OutlinedButton(
                onClick = onClearAll,
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error),
                border = BorderStroke(1.dp, scheme.error.copy(alpha = 0.4f)),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(I18n.t(lang, "clearAll"))
            }
        }

        Text(
            text = I18n.t(lang, "imgHint"),
            style = MaterialTheme.typography.bodySmall.copy(color = scheme.onSurfaceVariant)
        )

        // Image Thumbnail Grid
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cols = when {
                maxWidth >= 720.dp -> 4
                maxWidth >= 480.dp -> 3
                else -> 2
            }
            val spacing = 12.dp
            val cellWidth = (maxWidth - spacing * (cols - 1)) / cols

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
                maxItemsInEachRow = cols
            ) {
                state.imageItems.forEachIndexed { idx, img ->
                    Surface(
                        modifier = Modifier.width(cellWidth),
                        shape = RoundedCornerShape(14.dp),
                        color = scheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, scheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(145.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, scheme.outlineVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = img.previewBitmap.asImageBitmap(),
                                    contentDescription = img.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(accent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = img.name,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onMoveImage(idx, idx - 1) },
                                    enabled = idx > 0,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = I18n.t(lang, "moveLeft"),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onMoveImage(idx, idx + 1) },
                                    enabled = idx < state.imageItems.lastIndex,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = I18n.t(lang, "moveRight"),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onRemoveImage(idx) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = I18n.t(lang, "remove"),
                                        tint = scheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Page Layout Options Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = scheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, scheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = I18n.t(lang, "imgOptions"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                OptionChipsRow(
                    label = I18n.t(lang, "pageSize"),
                    options = listOf(
                        "fit" to I18n.t(lang, "optFit"),
                        "a4" to I18n.t(lang, "optA4"),
                        "letter" to I18n.t(lang, "optLetter")
                    ),
                    selectedValue = state.imgPageSize,
                    onSelect = onPageSizeChange
                )

                OptionChipsRow(
                    label = I18n.t(lang, "orientation"),
                    options = listOf(
                        "auto" to I18n.t(lang, "optAuto"),
                        "portrait" to I18n.t(lang, "optPortrait"),
                        "landscape" to I18n.t(lang, "optLandscape")
                    ),
                    selectedValue = state.imgOrientation,
                    onSelect = onOrientationChange
                )

                OptionChipsRow(
                    label = I18n.t(lang, "margins"),
                    options = listOf(
                        "0" to I18n.t(lang, "optM0"),
                        "10" to I18n.t(lang, "optM10"),
                        "20" to I18n.t(lang, "optM20")
                    ),
                    selectedValue = state.imgMarginMm.toString(),
                    onSelect = { onMarginChange(it.toIntOrNull() ?: 10) }
                )
            }
        }

        WorkspaceActionBar(
            summaryText = I18n.t(lang, "imgSummary", mapOf("n" to state.imageItems.size)),
            buttonText = I18n.t(lang, "imgNow"),
            accentColor = accent,
            onActionClick = onRunConvert,
            testTag = "run_img2pdf_button"
        )
    }
}

/* =====================================================================
   5) PDF -> IMAGES WORKSPACE
   ===================================================================== */
@Composable
fun PdfToImagesWorkView(
    state: PdfLabUiState,
    onChangeFile: () -> Unit,
    onFormatChange: (String) -> Unit,
    onDpiChange: (Int) -> Unit,
    onPagesTextChange: (String) -> Unit,
    onRunConvert: () -> Unit
) {
    val lang = state.lang
    val info = state.singlePdf ?: return
    val scheme = MaterialTheme.colorScheme
    val accent = ToolId.PDF2IMG.color

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ActiveFileBar(lang = lang, info = info, onChangeFile = onChangeFile)

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = scheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, scheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = I18n.t(lang, "imgOptions"),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                OptionChipsRow(
                    label = I18n.t(lang, "format"),
                    options = listOf(
                        "png" to "PNG",
                        "jpeg" to "JPEG"
                    ),
                    selectedValue = state.p2iFormat,
                    onSelect = onFormatChange
                )

                OptionChipsRow(
                    label = I18n.t(lang, "resolution"),
                    options = listOf(
                        "72" to "72 DPI",
                        "150" to "150 DPI",
                        "300" to "300 DPI"
                    ),
                    selectedValue = state.p2iDpi.toString(),
                    onSelect = { onDpiChange(it.toIntOrNull() ?: 150) }
                )

                OutlinedTextField(
                    value = state.p2iPagesText,
                    onValueChange = onPagesTextChange,
                    label = { Text(I18n.t(lang, "pagesOpt")) },
                    placeholder = { Text(I18n.t(lang, "allPagesPh")) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("p2i_pages_input")
                )
            }
        }

        Text(
            text = I18n.t(lang, "dpiNote"),
            style = MaterialTheme.typography.bodySmall.copy(color = scheme.onSurfaceVariant)
        )

        WorkspaceActionBar(
            summaryText = "",
            buttonText = I18n.t(lang, "p2iNow"),
            accentColor = accent,
            onActionClick = onRunConvert,
            testTag = "run_pdf2img_button"
        )
    }
}

@Composable
private fun OptionChipsRow(
    label: String,
    options: List<Pair<String, String>>,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = scheme.onSurfaceVariant
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (value, text) ->
                val selected = value == selectedValue
                FilterChip(
                    selected = selected,
                    onClick = { onSelect(value) },
                    label = {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = scheme.primaryContainer,
                        selectedLabelColor = scheme.primary
                    )
                )
            }
        }
    }
}

/* =====================================================================
   TOP-LEVEL WORKSPACE SHELL (Wraps Upload / Work / Result Stages)
   ===================================================================== */
@Composable
fun ToolWorkspaceScreen(
    state: PdfLabUiState,
    toolId: ToolId,
    splitPlanResult: Result<List<SplitJob>>,
    onBackToDashboard: () -> Unit,
    onLaunchFilePicker: () -> Unit,
    onLoadSampleFiles: () -> Unit,
    onStartOver: () -> Unit,
    onSaveOutput: (ResultOutput) -> Unit,
    onPreviewOutput: (ResultOutput) -> Unit,
    onShareOutput: (ResultOutput) -> Unit,
    // Merge callbacks
    onSortMergeAZ: () -> Unit,
    onMoveMergeItem: (Int, Int) -> Unit,
    onRemoveMergeItem: (Int) -> Unit,
    onRunMerge: () -> Unit,
    // Split callbacks
    onSplitModeChange: (String) -> Unit,
    onSplitRangesChange: (String) -> Unit,
    onSplitEveryNChange: (String) -> Unit,
    onRunSplit: () -> Unit,
    // Compress callbacks
    onRemoveCompressItem: (Long) -> Unit,
    onOpenSingleCompressed: (BatchCompressItem) -> Unit,
    onSelectCompressPreset: (String) -> Unit,
    onCompressDpiChange: (String) -> Unit,
    onCompressQualityChange: (String) -> Unit,
    onCompressStripChange: (Boolean) -> Unit,
    onRunCompressBatch: (Boolean) -> Unit,
    onStopCompressBatch: () -> Unit,
    onDownloadAllCompressedZip: () -> Unit,
    // Images -> PDF callbacks
    onMoveImageItem: (Int, Int) -> Unit,
    onRemoveImageItem: (Int) -> Unit,
    onImgPageSizeChange: (String) -> Unit,
    onImgOrientationChange: (String) -> Unit,
    onImgMarginChange: (Int) -> Unit,
    onRunImagesToPdf: () -> Unit,
    // PDF -> Images callbacks
    onP2iFormatChange: (String) -> Unit,
    onP2iDpiChange: (Int) -> Unit,
    onP2iPagesTextChange: (String) -> Unit,
    onRunPdfToImages: () -> Unit,
    // Rotate callbacks
    onRotatePage: (Int, Int) -> Unit,
    onRotateAllPages: (Int) -> Unit,
    onResetAllRotations: () -> Unit,
    onRunRotatePdf: () -> Unit,
    // Remove / Extract callbacks
    onTogglePageSelection: (Int) -> Unit,
    onSelectionRangeTextChange: (String) -> Unit,
    onSelectEvenPages: () -> Unit,
    onSelectOddPages: () -> Unit,
    onSelectAllPages: () -> Unit,
    onClearPageSelection: () -> Unit,
    onExtractModeChange: (String) -> Unit,
    onRunRemoveOrExtract: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.lang
    val scheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    BackHandler {
        onBackToDashboard()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Workspace Header (ws-head)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            border = BorderStroke(1.dp, scheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ToolIconBadge(toolId = toolId, size = 54.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = I18n.t(lang, "${toolId.key}_title"),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                        )
                        if (lang == AppLang.BN) {
                            Text(
                                text = " · ${toolId.enTitle}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = scheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = I18n.t(lang, "${toolId.key}_desc"),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = scheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // Stage Content (Upload / Work / Result)
        when (state.stage) {
            WorkspaceStage.UPLOAD -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                    border = BorderStroke(1.dp, scheme.outline)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        UploadDropzoneCard(
                            lang = lang,
                            toolId = toolId,
                            isMultiplePdf = toolId == ToolId.MERGE || toolId == ToolId.COMPRESS,
                            isImageMode = toolId == ToolId.IMG2PDF,
                            onSelectFilesClick = onLaunchFilePicker,
                            onLoadSampleClick = onLoadSampleFiles
                        )
                    }
                }
            }

            WorkspaceStage.WORK -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                    border = BorderStroke(1.dp, scheme.outline)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        when (toolId) {
                            ToolId.MERGE -> MergeWorkView(
                                state = state,
                                onAddMoreClick = onLaunchFilePicker,
                                onSortAZ = onSortMergeAZ,
                                onClearAll = onStartOver,
                                onMoveItem = onMoveMergeItem,
                                onRemoveItem = onRemoveMergeItem,
                                onRunMerge = onRunMerge
                            )

                            ToolId.SPLIT -> SplitWorkView(
                                state = state,
                                splitPlanResult = splitPlanResult,
                                onChangeFile = onStartOver,
                                onSelectMode = onSplitModeChange,
                                onRangesChange = onSplitRangesChange,
                                onEveryNChange = onSplitEveryNChange,
                                onRunSplit = onRunSplit
                            )

                            ToolId.COMPRESS -> CompressWorkView(
                                state = state,
                                onAddMoreClick = onLaunchFilePicker,
                                onClearAll = onStartOver,
                                onRemoveItem = onRemoveCompressItem,
                                onOpenSingleCompressed = onOpenSingleCompressed,
                                onSelectPreset = onSelectCompressPreset,
                                onDpiChange = onCompressDpiChange,
                                onQualityChange = onCompressQualityChange,
                                onStripChange = onCompressStripChange,
                                onRunBatch = onRunCompressBatch,
                                onStopBatch = onStopCompressBatch,
                                onDownloadAllZip = onDownloadAllCompressedZip
                            )

                            ToolId.IMG2PDF -> ImagesToPdfWorkView(
                                state = state,
                                onAddMoreImages = onLaunchFilePicker,
                                onClearAll = onStartOver,
                                onMoveImage = onMoveImageItem,
                                onRemoveImage = onRemoveImageItem,
                                onPageSizeChange = onImgPageSizeChange,
                                onOrientationChange = onImgOrientationChange,
                                onMarginChange = onImgMarginChange,
                                onRunConvert = onRunImagesToPdf
                            )

                            ToolId.PDF2IMG -> PdfToImagesWorkView(
                                state = state,
                                onChangeFile = onStartOver,
                                onFormatChange = onP2iFormatChange,
                                onDpiChange = onP2iDpiChange,
                                onPagesTextChange = onP2iPagesTextChange,
                                onRunConvert = onRunPdfToImages
                            )

                            ToolId.ROTATE -> RotateWorkView(
                                state = state,
                                onChangeFile = onStartOver,
                                onRotatePage = onRotatePage,
                                onRotateAll = onRotateAllPages,
                                onResetAll = onResetAllRotations,
                                onRunRotate = onRunRotatePdf
                            )

                            ToolId.REMOVE, ToolId.EXTRACT -> SelectionWorkView(
                                state = state,
                                isRemove = toolId == ToolId.REMOVE,
                                onChangeFile = onStartOver,
                                onTogglePage = onTogglePageSelection,
                                onRangeTextChange = onSelectionRangeTextChange,
                                onSelectEven = onSelectEvenPages,
                                onSelectOdd = onSelectOddPages,
                                onSelectAll = onSelectAllPages,
                                onClearSelection = onClearPageSelection,
                                onExtractModeChange = onExtractModeChange,
                                onRunAction = onRunRemoveOrExtract
                            )
                        }
                    }
                }
            }

            WorkspaceStage.RESULT -> {
                val res = state.result
                if (res != null) {
                    ResultStageCard(
                        lang = lang,
                        isDarkTheme = state.isDarkTheme,
                        result = res,
                        onSaveToDevice = { onSaveOutput(res) },
                        onPreviewOutput = { onPreviewOutput(res) },
                        onShareOutput = { onShareOutput(res) },
                        onStartOver = onStartOver
                    )
                }
            }
        }
    }
}
