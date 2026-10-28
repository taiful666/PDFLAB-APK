package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppLang
import com.example.data.I18n
import com.example.data.LoadedPdfInfo
import com.example.data.ToolId
import com.example.data.fmtBytes
import com.example.ui.BusyState
import com.example.ui.ResultOutput
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SuccessGreenDark
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.SuccessGreenSoftDark
import com.example.ui.theme.SuccessGreenSoftLight

fun toolVectorIcon(toolId: ToolId): ImageVector = when (toolId) {
    ToolId.MERGE -> Icons.Default.MergeType
    ToolId.SPLIT -> Icons.AutoMirrored.Filled.CallSplit
    ToolId.COMPRESS -> Icons.Default.Compress
    ToolId.IMG2PDF -> Icons.Default.AddPhotoAlternate
    ToolId.PDF2IMG -> Icons.Default.Collections
    ToolId.ROTATE -> Icons.Default.Rotate90DegreesCcw
    ToolId.REMOVE -> Icons.Default.DeleteSweep
    ToolId.EXTRACT -> Icons.Default.FileOpen
}

@Composable
fun ToolIconBadge(
    toolId: ToolId,
    size: Dp = 52.dp,
    modifier: Modifier = Modifier
) {
    val color = toolId.color
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        color.copy(alpha = 0.18f),
                        color.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.32f),
                shape = RoundedCornerShape(size * 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = toolVectorIcon(toolId),
            contentDescription = toolId.enTitle,
            tint = color,
            modifier = Modifier.size(size * 0.54f)
        )
    }
}

@Composable
fun DeskMateTopBar(
    lang: AppLang,
    isDarkTheme: Boolean,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onLogoClick: () -> Unit,
    onLangChange: (AppLang) -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val okColor = if (isDarkTheme) SuccessGreenDark else SuccessGreenLight
    val okBg = if (isDarkTheme) SuccessGreenSoftDark else SuccessGreenSoftLight

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = scheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand or Back + Brand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (showBackButton) onBackClick() else onLogoClick()
                        }
                        .padding(vertical = 2.dp, horizontal = 4.dp)
                        .testTag("brand_header_button")
                ) {
                    if (showBackButton) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(scheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = I18n.t(lang, "back"),
                                tint = scheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(scheme.primary, scheme.tertiary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDFLab Logo",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PDFLab",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.primary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Offline status pill
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(okBg)
                                    .border(1.dp, okColor.copy(alpha = 0.35f), CircleShape)
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(okColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = I18n.t(lang, "offlineBadge"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = okColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Text(
                            text = "DeskMate Tools",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = scheme.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right actions: Language Segmented Toggle + Theme Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LanguageTogglePill(
                        currentLang = lang,
                        onSelectLang = onLangChange
                    )

                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .border(1.dp, scheme.outline, RoundedCornerShape(11.dp))
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = I18n.t(lang, "themeTitle"),
                            tint = scheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            HorizontalDivider(thickness = 1.dp, color = scheme.outlineVariant)
        }
    }
}

@Composable
fun LanguageTogglePill(
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .border(1.dp, scheme.outline, RoundedCornerShape(11.dp))
            .background(scheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppLang.entries.forEach { item ->
            val selected = item == currentLang
            val bg by animateColorAsState(
                targetValue = if (selected) scheme.primary else Color.Transparent,
                label = "langBg"
            )
            val fg by animateColorAsState(
                targetValue = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                label = "langFg"
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(bg)
                    .clickable { onSelectLang(item) }
                    .padding(horizontal = 9.dp, vertical = 6.dp)
                    .testTag("lang_btn_${item.code}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                FlagBadge(isBangladesh = item == AppLang.BN)
                Text(
                    text = if (item == AppLang.BN) "বাংলা" else "EN",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = fg
                    )
                )
            }
        }
    }
}

@Composable
private fun FlagBadge(isBangladesh: Boolean) {
    Canvas(
        modifier = Modifier
            .width(16.dp)
            .height(11.dp)
            .clip(RoundedCornerShape(2.dp))
    ) {
        if (isBangladesh) {
            drawRect(color = Color(0xFF006A4E))
            drawCircle(
                color = Color(0xFFF42A41),
                radius = size.minDimension * 0.32f,
                center = Offset(size.width * 0.45f, size.height * 0.5f)
            )
        } else {
            drawRect(color = Color(0xFF012169))
            drawLine(
                color = Color.White,
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
                strokeWidth = 2.2f
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 2.2f
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 3.5f
            )
            drawLine(
                color = Color.White,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 3.5f
            )
            drawLine(
                color = Color(0xFFC8102E),
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 2f
            )
            drawLine(
                color = Color(0xFFC8102E),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun UploadDropzoneCard(
    lang: AppLang,
    toolId: ToolId,
    isMultiplePdf: Boolean = false,
    isImageMode: Boolean = false,
    onSelectFilesClick: () -> Unit,
    onLoadSampleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val accent = toolId.color
    val titleKey = when {
        isImageMode -> "dzTitleImg"
        isMultiplePdf -> "dzTitleMulti"
        else -> "dzTitle"
    }
    val btnKey = when {
        isImageMode -> "selectImages"
        isMultiplePdf -> "selectPdfs"
        else -> "selectPdf"
    }
    val hintKey = if (isImageMode) "dzHintImg" else "dzHintPdf"
    val sampleKey = when {
        isImageMode -> "trySampleImages"
        isMultiplePdf -> "trySampleMultiPdf"
        else -> "trySamplePdf"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(accent.copy(alpha = 0.05f))
            .drawBehind {
                drawRoundRect(
                    color = accent.copy(alpha = 0.45f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                    ),
                    cornerRadius = CornerRadius(22.dp.toPx())
                )
            }
            .clickable { onSelectFilesClick() }
            .padding(horizontal = 20.dp, vertical = 32.dp)
            .testTag("upload_dropzone_card"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isImageMode) Icons.Default.AddPhotoAlternate else Icons.Default.UploadFile,
                    contentDescription = I18n.t(lang, btnKey),
                    tint = accent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = I18n.t(lang, titleKey),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                ),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onSelectFilesClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("select_files_button")
            ) {
                Icon(
                    imageVector = if (isImageMode) Icons.Default.Image else Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = I18n.t(lang, btnKey),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                HorizontalDivider(modifier = Modifier.width(40.dp), color = scheme.outline)
                Text(
                    text = I18n.t(lang, "dzOr"),
                    style = MaterialTheme.typography.bodySmall.copy(color = scheme.onSurfaceVariant)
                )
                HorizontalDivider(modifier = Modifier.width(40.dp), color = scheme.outline)
            }

            FilledTonalButton(
                onClick = onLoadSampleClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("load_sample_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = I18n.t(lang, sampleKey),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Text(
                text = I18n.t(lang, hintKey),
                style = MaterialTheme.typography.bodySmall.copy(color = scheme.onSurfaceVariant),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActiveFileBar(
    lang: AppLang,
    info: LoadedPdfInfo,
    onChangeFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = scheme.primaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = info.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${fmtBytes(info.size)} · ${info.pageCount} ${I18n.t(lang, "pagesWord")}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = scheme.onSurfaceVariant,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onChangeFile,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("change_file_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = I18n.t(lang, "changeFile"),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
fun RadioChoiceCard(
    selected: Boolean,
    title: String,
    description: String,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extraContent: (@Composable () -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val borderColor = if (selected) accentColor else scheme.outline
    val bgColor = if (selected) accentColor.copy(alpha = 0.08f) else scheme.surface

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(if (selected) 1.8.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = accentColor),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = scheme.onSurfaceVariant
                    )
                )
                if (extraContent != null && selected) {
                    Spacer(modifier = Modifier.height(10.dp))
                    extraContent()
                }
            }
        }
    }
}

@Composable
fun ResultStageCard(
    lang: AppLang,
    isDarkTheme: Boolean,
    result: ResultOutput,
    onSaveToDevice: () -> Unit,
    onPreviewOutput: () -> Unit,
    onShareOutput: () -> Unit,
    onStartOver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val okColor = if (isDarkTheme) SuccessGreenDark else SuccessGreenLight
    val okBg = if (isDarkTheme) SuccessGreenSoftDark else SuccessGreenSoftLight

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("result_stage_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(okBg)
                    .border(2.dp, okColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = I18n.t(lang, "doneTitle"),
                    tint = okColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = I18n.t(lang, "doneTitle"),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Metrics Table (res-rows)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, scheme.outline),
                color = scheme.surface
            ) {
                Column {
                    result.rows.forEachIndexed { index, (labelKey, valueText) ->
                        val rowBg = if (index % 2 == 0) {
                            scheme.surfaceVariant.copy(alpha = 0.4f)
                        } else {
                            scheme.surface
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .padding(horizontal = 16.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = I18n.t(lang, labelKey),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = scheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = valueText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = scheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFontFamily
                                ),
                                textAlign = TextAlign.End
                            )
                        }
                        if (index < result.rows.lastIndex) {
                            HorizontalDivider(color = scheme.outline)
                        }
                    }
                }
            }

            // Optional Notes (e.g., alreadyOptimized)
            result.notes.forEach { noteKey ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = scheme.primaryContainer
                ) {
                    Text(
                        text = I18n.t(lang, noteKey),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = scheme.primary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = I18n.t(lang, "dlHint"),
                style = MaterialTheme.typography.bodySmall.copy(color = scheme.onSurfaceVariant),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Save Button
            Button(
                onClick = onSaveToDevice,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("save_output_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = I18n.t(lang, "download"),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Row: Preview & Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onPreviewOutput,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("preview_output_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = I18n.t(lang, "previewBtn"),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FilledTonalButton(
                    onClick = onShareOutput,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("share_output_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = I18n.t(lang, "shareBtn"),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onStartOver,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("start_over_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = I18n.t(lang, "startOver"),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
fun BusyModalOverlay(
    lang: AppLang,
    busy: BusyState
) {
    val scheme = MaterialTheme.colorScheme
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = scheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = scheme.primary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(46.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = busy.text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                if (busy.progress != null && busy.progress > 0f) {
                    LinearProgressIndicator(
                        progress = { busy.progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${(busy.progress.coerceIn(0f, 1f) * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(color = scheme.onSurfaceVariant)
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = I18n.t(lang, "pleaseWait"),
                    style = MaterialTheme.typography.bodySmall.copy(color = scheme.onSurfaceVariant),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun OutputPreviewDialog(
    lang: AppLang,
    output: ResultOutput,
    pdfThumbnails: List<Bitmap>,
    zipEntries: List<Pair<String, Long>>,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp),
            color = scheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (output.mimeType == "application/pdf") {
                                Icons.Default.PictureAsPdf
                            } else {
                                Icons.Default.FolderZip
                            },
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = output.filename,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = fmtBytes(output.bytes.size.toLong()),
                                style = MaterialTheme.typography.labelSmall.copy(color = scheme.onSurfaceVariant)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                ) {
                    if (output.mimeType == "application/pdf") {
                        if (pdfThumbnails.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 135.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.heightIn(max = 460.dp)
                            ) {
                                itemsIndexed(pdfThumbnails) { idx, bmp ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, scheme.outline),
                                        color = scheme.surfaceVariant.copy(alpha = 0.4f)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Image(
                                                bitmap = bmp.asImageBitmap(),
                                                contentDescription = "Page ${idx + 1}",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(165.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.White)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "${I18n.t(lang, "page")} ${idx + 1}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // ZIP Archive Inspector
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 440.dp)
                        ) {
                            itemsIndexed(zipEntries) { idx, (entryName, entrySize) ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = scheme.surfaceVariant.copy(alpha = 0.45f),
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
                                            Icon(
                                                imageVector = if (entryName.endsWith(".pdf")) {
                                                    Icons.Default.Description
                                                } else {
                                                    Icons.AutoMirrored.Filled.InsertDriveFile
                                                },
                                                contentDescription = null,
                                                tint = scheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${idx + 1}. $entryName",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = fmtBytes(entrySize),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = scheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(I18n.t(lang, "shareBtn"))
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(I18n.t(lang, "download"))
                    }
                }
            }
        }
    }
}
