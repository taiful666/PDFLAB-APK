package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLang
import com.example.data.I18n
import com.example.data.ToolCategory
import com.example.data.ToolId
import com.example.data.fmtBytes
import com.example.ui.PdfLabUiState
import com.example.ui.ResultOutput
import com.example.ui.components.ToolIconBadge
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SuccessGreenDark
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.SuccessGreenSoftDark
import com.example.ui.theme.SuccessGreenSoftLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: PdfLabUiState,
    onOpenTool: (ToolId) -> Unit,
    onSelectCategory: (ToolCategory) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPreviewOutput: (ResultOutput) -> Unit,
    onSaveOutput: (ResultOutput) -> Unit,
    onShareOutput: (ResultOutput) -> Unit,
    onClearRecent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.lang
    val scheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    val filteredTools = ToolId.entries.filter { tool ->
        val matchesCat = state.selectedCategory == ToolCategory.ALL || tool.category == state.selectedCategory
        if (!matchesCat) return@filter false
        val q = state.searchQuery.trim().lowercase()
        if (q.isEmpty()) return@filter true
        val titleBn = I18n.t(AppLang.BN, "${tool.key}_title").lowercase()
        val descBn = I18n.t(AppLang.BN, "${tool.key}_desc").lowercase()
        val titleEn = tool.enTitle.lowercase()
        val descEn = I18n.t(AppLang.EN, "${tool.key}_desc").lowercase()
        titleBn.contains(q) || descBn.contains(q) || titleEn.contains(q) || descEn.contains(q)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Hero Banner Section
        HeroBannerSection(
            lang = lang,
            isDarkTheme = state.isDarkTheme
        )

        // 2. Search & Category Filter Row
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = I18n.t(lang, "searchPh"),
                        style = MaterialTheme.typography.bodyMedium.copy(color = scheme.onSurfaceVariant)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = scheme.primary
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = scheme.surface,
                    unfocusedContainerColor = scheme.surface,
                    unfocusedBorderColor = scheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_search_input")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf(
                    ToolCategory.ALL to "catAll",
                    ToolCategory.ORGANIZE to "catOrganize",
                    ToolCategory.CONVERT to "catConvert",
                    ToolCategory.OPTIMIZE to "catOptimize"
                )
                categories.forEach { (cat, key) ->
                    val selected = state.selectedCategory == cat
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectCategory(cat) },
                        label = {
                            Text(
                                text = I18n.t(lang, key),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = scheme.primaryContainer,
                            selectedLabelColor = scheme.primary
                        ),
                        modifier = Modifier.testTag("cat_chip_${cat.name.lowercase()}")
                    )
                }
            }
        }

        // 3. Adaptive Grid of 8 PDF Tools
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cols = when {
                maxWidth >= 840.dp -> 4
                maxWidth >= 520.dp -> 2
                else -> 1
            }
            val spacing = 14.dp
            val cardWidth = (maxWidth - spacing * (cols - 1)) / cols

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
                maxItemsInEachRow = cols
            ) {
                filteredTools.forEach { tool ->
                    ToolDashboardCard(
                        lang = lang,
                        tool = tool,
                        onClick = { onOpenTool(tool) },
                        modifier = Modifier.width(cardWidth)
                    )
                }
            }
        }

        // 4. Recent Processed Files Section (if any)
        if (state.recentOutputs.isNotEmpty()) {
            RecentOutputsSection(
                lang = lang,
                outputs = state.recentOutputs,
                onPreview = onPreviewOutput,
                onSave = onSaveOutput,
                onShare = onShareOutput,
                onClear = onClearRecent
            )
        }

        // 5. Footer Note
        Text(
            text = I18n.t(lang, "footerNote"),
            style = MaterialTheme.typography.bodySmall.copy(
                color = scheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun HeroBannerSection(
    lang: AppLang,
    isDarkTheme: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    val okColor = if (isDarkTheme) SuccessGreenDark else SuccessGreenLight
    val okBg = if (isDarkTheme) SuccessGreenSoftDark else SuccessGreenSoftLight

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outline)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                Color(0xFF16243A),
                                Color(0xFF182231),
                                Color(0xFF1E1B38)
                            )
                        } else {
                            listOf(
                                Color(0xFFEAF2FF),
                                Color(0xFFFFFFFF),
                                Color(0xFFF3E8FF)
                            )
                        }
                    )
                )
                .drawBehind {
                    // Decorative asymmetric geometric PDF sheets in background
                    rotate(-12f, pivot = Offset(size.width * 0.88f, size.height * 0.28f)) {
                        drawRoundRect(
                            color = scheme.primary.copy(alpha = 0.07f),
                            topLeft = Offset(size.width * 0.76f, -20f),
                            size = Size(130.dp.toPx(), 160.dp.toPx()),
                            cornerRadius = CornerRadius(18.dp.toPx())
                        )
                    }
                    rotate(8f, pivot = Offset(size.width * 0.92f, size.height * 0.45f)) {
                        drawRoundRect(
                            color = scheme.tertiary.copy(alpha = 0.07f),
                            topLeft = Offset(size.width * 0.82f, 20f),
                            size = Size(110.dp.toPx(), 140.dp.toPx()),
                            cornerRadius = CornerRadius(16.dp.toPx())
                        )
                    }
                }
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = I18n.t(lang, "heroTitle"),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = I18n.t(lang, "heroSub"),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = scheme.onSurfaceVariant,
                        lineHeight = 21.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Privacy Shield Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(okBg)
                        .border(1.dp, okColor.copy(alpha = 0.38f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = okColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = I18n.t(lang, "privacy"),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = okColor,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolDashboardCard(
    lang: AppLang,
    tool: ToolId,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val accent = tool.color

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("tool_card_${tool.key}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ToolIconBadge(toolId = tool, size = 54.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = I18n.t(lang, "${tool.key}_title"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                        )
                        if (lang == AppLang.BN) {
                            Text(
                                text = tool.enTitle,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = scheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = I18n.t(lang, "${tool.key}_desc"),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = scheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    ),
                    minLines = 2
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = I18n.t(lang, "start"),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentOutputsSection(
    lang: AppLang,
    outputs: List<ResultOutput>,
    onPreview: (ResultOutput) -> Unit,
    onSave: (ResultOutput) -> Unit,
    onShare: (ResultOutput) -> Unit,
    onClear: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = I18n.t(lang, "recentTitle"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onClear) {
                    Text(
                        text = I18n.t(lang, "clearRecent"),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            outputs.forEach { out ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = scheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, scheme.outlineVariant)
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
                                imageVector = if (out.mimeType == "application/pdf") {
                                    Icons.Default.PictureAsPdf
                                } else {
                                    Icons.Default.FolderZip
                                },
                                contentDescription = null,
                                tint = out.toolId.color,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = out.filename,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = fmtBytes(out.bytes.size.toLong()),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = scheme.onSurfaceVariant,
                                        fontFamily = JetBrainsMonoFontFamily
                                    )
                                )
                            }
                        }
                        Row {
                            IconButton(
                                onClick = { onPreview(out) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = I18n.t(lang, "previewBtn"),
                                    tint = scheme.primary
                                )
                            }
                            IconButton(
                                onClick = { onSave(out) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = I18n.t(lang, "download"),
                                    tint = scheme.primary
                                )
                            }
                            IconButton(
                                onClick = { onShare(out) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = I18n.t(lang, "shareBtn"),
                                    tint = scheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
