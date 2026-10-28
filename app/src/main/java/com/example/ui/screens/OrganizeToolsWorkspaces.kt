package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppError
import com.example.data.I18n
import com.example.data.SplitJob
import com.example.data.ToolId
import com.example.data.fmtBytes
import com.example.ui.PdfLabUiState
import com.example.ui.components.ActiveFileBar
import com.example.ui.components.RadioChoiceCard
import com.example.ui.theme.JetBrainsMonoFontFamily

/* =====================================================================
   1) MERGE PDF WORKSPACE
   ===================================================================== */
@Composable
fun MergeWorkView(
    state: PdfLabUiState,
    onAddMoreClick: () -> Unit,
    onSortAZ: () -> Unit,
    onClearAll: () -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onRunMerge: () -> Unit
) {
    val lang = state.lang
    val scheme = MaterialTheme.colorScheme
    val totalPages = state.mergeItems.sumOf { it.pageCount }

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
                shape = RoundedCornerShape(11.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("merge_add_more_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(I18n.t(lang, "addMore"))
            }

            OutlinedButton(
                onClick = onSortAZ,
                shape = RoundedCornerShape(11.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("merge_sort_az_btn")
            ) {
                Icon(Icons.Default.SortByAlpha, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(I18n.t(lang, "sortAZ"))
            }

            OutlinedButton(
                onClick = onClearAll,
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.error),
                border = BorderStroke(1.dp, scheme.error.copy(alpha = 0.4f)),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("merge_clear_all_btn")
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(I18n.t(lang, "clearAll"))
            }
        }

        Text(
            text = I18n.t(lang, "mergeHint"),
            style = MaterialTheme.typography.bodySmall.copy(color = scheme.onSurfaceVariant)
        )

        // File List
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.mergeItems.forEachIndexed { idx, item ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, scheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(scheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.primary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            if (item.thumbnails.isNotEmpty()) {
                                Image(
                                    bitmap = item.thumbnails.first().asImageBitmap(),
                                    contentDescription = item.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                        .border(1.dp, scheme.outline, RoundedCornerShape(6.dp))
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${fmtBytes(item.size)} · ${item.pageCount} ${I18n.t(lang, "pagesWord")}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = scheme.onSurfaceVariant,
                                        fontFamily = JetBrainsMonoFontFamily
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onMoveItem(idx, idx - 1) },
                                enabled = idx > 0,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = I18n.t(lang, "moveUp"),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onMoveItem(idx, idx + 1) },
                                enabled = idx < state.mergeItems.lastIndex,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = I18n.t(lang, "moveDown"),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onRemoveItem(idx) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = I18n.t(lang, "remove"),
                                    tint = scheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Bar
        WorkspaceActionBar(
            summaryText = I18n.t(
                lang,
                "mergeSummary",
                mapOf("files" to state.mergeItems.size, "pages" to totalPages)
            ),
            buttonText = I18n.t(lang, "mergeNow"),
            accentColor = ToolId.MERGE.color,
            onActionClick = onRunMerge,
            testTag = "run_merge_button"
        )
    }
}

/* =====================================================================
   2) SPLIT PDF WORKSPACE
   ===================================================================== */
@Composable
fun SplitWorkView(
    state: PdfLabUiState,
    splitPlanResult: Result<List<SplitJob>>,
    onChangeFile: () -> Unit,
    onSelectMode: (String) -> Unit,
    onRangesChange: (String) -> Unit,
    onEveryNChange: (String) -> Unit,
    onRunSplit: () -> Unit
) {
    val lang = state.lang
    val info = state.singlePdf ?: return
    val scheme = MaterialTheme.colorScheme
    val accent = ToolId.SPLIT.color

    val previewText = splitPlanResult.fold(
        onSuccess = { jobs -> I18n.t(lang, "willCreate", mapOf("n" to jobs.size)) },
        onFailure = { err ->
            if (err is AppError) I18n.t(lang, err.key, err.vars)
            else err.localizedMessage ?: ""
        }
    )
    val isPreviewError = splitPlanResult.isFailure

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ActiveFileBar(lang = lang, info = info, onChangeFile = onChangeFile)

        Text(
            text = I18n.t(lang, "splitMode"),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        RadioChoiceCard(
            selected = state.splitMode == "single",
            title = I18n.t(lang, "splitSingle"),
            description = I18n.t(lang, "splitSingleD"),
            accentColor = accent,
            onClick = { onSelectMode("single") }
        )

        RadioChoiceCard(
            selected = state.splitMode == "ranges",
            title = I18n.t(lang, "splitRanges"),
            description = I18n.t(lang, "splitRangesD"),
            accentColor = accent,
            onClick = { onSelectMode("ranges") },
            extraContent = {
                OutlinedTextField(
                    value = state.splitRangesText,
                    onValueChange = onRangesChange,
                    placeholder = { Text(I18n.t(lang, "rangesPh")) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("split_ranges_input")
                )
            }
        )

        RadioChoiceCard(
            selected = state.splitMode == "every",
            title = I18n.t(lang, "splitEvery"),
            description = I18n.t(lang, "splitEveryD"),
            accentColor = accent,
            onClick = { onSelectMode("every") },
            extraContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = I18n.t(lang, "every"),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    OutlinedTextField(
                        value = state.splitEveryNText,
                        onValueChange = onEveryNChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(96.dp)
                            .testTag("split_every_input")
                    )
                    Text(
                        text = I18n.t(lang, "pagesWord"),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        )

        WorkspaceActionBar(
            summaryText = previewText,
            isSummaryError = isPreviewError,
            buttonText = I18n.t(lang, "splitNow"),
            accentColor = accent,
            onActionClick = onRunSplit,
            testTag = "run_split_button"
        )
    }
}

/* =====================================================================
   6) ROTATE PDF PAGES WORKSPACE
   ===================================================================== */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RotateWorkView(
    state: PdfLabUiState,
    onChangeFile: () -> Unit,
    onRotatePage: (Int, Int) -> Unit,
    onRotateAll: (Int) -> Unit,
    onResetAll: () -> Unit,
    onRunRotate: () -> Unit
) {
    val lang = state.lang
    val info = state.singlePdf ?: return
    val scheme = MaterialTheme.colorScheme
    val accent = ToolId.ROTATE.color
    val rotatedCount = state.pageRotations.count { it != 0 }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ActiveFileBar(lang = lang, info = info, onChangeFile = onChangeFile)

        // Bulk Rotation Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { onRotateAll(90) },
                shape = RoundedCornerShape(11.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("rot_all_cw_btn")
            ) {
                Icon(Icons.Default.Rotate90DegreesCw, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(I18n.t(lang, "rotAllCW"))
            }

            OutlinedButton(
                onClick = { onRotateAll(270) },
                shape = RoundedCornerShape(11.dp),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Default.Rotate90DegreesCcw, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(I18n.t(lang, "rotAllCCW"))
            }

            OutlinedButton(
                onClick = { onRotateAll(180) },
                shape = RoundedCornerShape(11.dp),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text(I18n.t(lang, "rotAll180"))
            }

            OutlinedButton(
                onClick = onResetAll,
                shape = RoundedCornerShape(11.dp),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(I18n.t(lang, "resetAll"))
            }
        }

        // Page Grid
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
                for (i in 0 until info.pageCount) {
                    val rot = state.pageRotations.getOrElse(i) { 0 }
                    val changed = rot != 0
                    val bmp = info.thumbnails.getOrNull(i)

                    Surface(
                        modifier = Modifier.width(cellWidth),
                        shape = RoundedCornerShape(14.dp),
                        color = if (changed) accent.copy(alpha = 0.08f) else scheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(if (changed) 2.dp else 1.dp, if (changed) accent else scheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(155.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, scheme.outlineVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "Page ${i + 1}",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                            .graphicsLayer { rotationZ = rot.toFloat() }
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .graphicsLayer { rotationZ = rot.toFloat() }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${I18n.t(lang, "page")} ${i + 1} · ${rot}°",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (changed) accent else scheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onRotatePage(i, 270) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Rotate90DegreesCcw,
                                        contentDescription = I18n.t(lang, "rotCCW"),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onRotatePage(i, 90) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Rotate90DegreesCw,
                                        contentDescription = I18n.t(lang, "rotCW"),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        WorkspaceActionBar(
            summaryText = I18n.t(lang, "rotSummary", mapOf("n" to rotatedCount, "total" to info.pageCount)),
            buttonText = I18n.t(lang, "rotNow"),
            accentColor = accent,
            onActionClick = onRunRotate,
            testTag = "run_rotate_button"
        )
    }
}

/* =====================================================================
   7) REMOVE PAGES & 8) EXTRACT PAGES WORKSPACE
   ===================================================================== */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectionWorkView(
    state: PdfLabUiState,
    isRemove: Boolean,
    onChangeFile: () -> Unit,
    onTogglePage: (Int) -> Unit,
    onRangeTextChange: (String) -> Unit,
    onSelectEven: () -> Unit,
    onSelectOdd: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onExtractModeChange: (String) -> Unit,
    onRunAction: () -> Unit
) {
    val lang = state.lang
    val info = state.singlePdf ?: return
    val scheme = MaterialTheme.colorScheme
    val accent = if (isRemove) ToolId.REMOVE.color else ToolId.EXTRACT.color

    val summaryText = if (isRemove) {
        I18n.t(
            lang,
            "delSummary",
            mapOf("n" to state.selectedPages1Based.size, "left" to (info.pageCount - state.selectedPages1Based.size))
        )
    } else {
        I18n.t(
            lang,
            "extSummary",
            mapOf("n" to state.selectedPages1Based.size, "total" to info.pageCount)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ActiveFileBar(lang = lang, info = info, onChangeFile = onChangeFile)

        // Range Input + Quick Selection Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.selectionRangeText,
                onValueChange = onRangeTextChange,
                label = { Text(I18n.t(lang, if (isRemove) "delLabel" else "extLabel")) },
                placeholder = { Text(I18n.t(lang, if (isRemove) "delPh" else "extPh")) },
                isError = state.selectionRangeError != null,
                supportingText = state.selectionRangeError?.let { err ->
                    { Text(err, color = scheme.error) }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selection_range_input")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSelectEven,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(I18n.t(lang, "selEven"))
                }
                OutlinedButton(
                    onClick = onSelectOdd,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(I18n.t(lang, "selOdd"))
                }
                if (!isRemove) {
                    OutlinedButton(
                        onClick = onSelectAll,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(I18n.t(lang, "selAll"))
                    }
                }
                OutlinedButton(
                    onClick = onClearSelection,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(I18n.t(lang, "selClear"))
                }
            }
        }

        // Extract Mode Radio Cards (when in Extract tool)
        if (!isRemove) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RadioChoiceCard(
                    selected = state.extractMode == "single",
                    title = I18n.t(lang, "extOne"),
                    description = I18n.t(lang, "extOneD"),
                    accentColor = accent,
                    onClick = { onExtractModeChange("single") }
                )
                RadioChoiceCard(
                    selected = state.extractMode == "separate",
                    title = I18n.t(lang, "extSep"),
                    description = I18n.t(lang, "extSepD"),
                    accentColor = accent,
                    onClick = { onExtractModeChange("separate") }
                )
            }
        }

        Text(
            text = I18n.t(lang, if (isRemove) "delHint" else "extHint"),
            style = MaterialTheme.typography.bodySmall.copy(color = scheme.onSurfaceVariant)
        )

        // Interactive Page Thumbnail Grid
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
                for (i in 0 until info.pageCount) {
                    val page1 = i + 1
                    val selected = state.selectedPages1Based.contains(page1)
                    val bmp = info.thumbnails.getOrNull(i)

                    Surface(
                        modifier = Modifier
                            .width(cellWidth)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onTogglePage(page1) }
                            .testTag("page_cell_$page1"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) accent.copy(alpha = 0.1f) else scheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) accent else scheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(155.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, scheme.outlineVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "Page $page1",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                            .graphicsLayer { alpha = if (selected && isRemove) 0.38f else 1f }
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }

                                // Top-right badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) accent else scheme.surface.copy(alpha = 0.85f))
                                        .border(1.dp, if (selected) accent else scheme.outline, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selected) {
                                        Icon(
                                            imageVector = if (isRemove) Icons.Default.Delete else Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                // DELETED overlay pill in Remove mode
                                if (selected && isRemove) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(accent)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = I18n.t(lang, "deleted"),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${I18n.t(lang, "page")} $page1",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) accent else scheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        WorkspaceActionBar(
            summaryText = summaryText,
            buttonText = I18n.t(lang, if (isRemove) "delNow" else "extNow"),
            accentColor = accent,
            onActionClick = onRunAction,
            testTag = if (isRemove) "run_remove_button" else "run_extract_button"
        )
    }
}

@Composable
fun WorkspaceActionBar(
    summaryText: String,
    buttonText: String,
    accentColor: Color,
    onActionClick: () -> Unit,
    testTag: String,
    isSummaryError: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
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
            if (summaryText.isNotBlank()) {
                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSummaryError) scheme.error else scheme.onSurface
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag(testTag)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
