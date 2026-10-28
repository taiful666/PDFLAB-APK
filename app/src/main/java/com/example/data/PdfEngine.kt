package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.coroutines.coroutineContext
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class AppError(val key: String, val vars: Map<String, Any> = emptyMap()) : Exception(key)

data class LoadedPdfInfo(
    val id: Long,
    val name: String,
    val size: Long,
    val file: File,
    val pageCount: Int,
    val thumbnails: List<Bitmap> = emptyList()
)

data class LoadedImageItem(
    val id: Long,
    val name: String,
    val size: Long,
    val file: File,
    val previewBitmap: Bitmap,
    val width: Int,
    val height: Int
)

data class ZipFileEntry(
    val name: String,
    val data: ByteArray
)

data class SplitJob(
    val name: String,
    val pages1Based: List<Int>
)

data class ImageLayoutPlacement(
    val pw: Float,
    val ph: Float,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float
)

data class CompressResultData(
    val bytes: ByteArray,
    val alreadyOptimized: Boolean
)

object PdfEngine {

    /** Compute page size and image placement for Images -> PDF. All sizes in PDF points (1/72 inch). */
    fun layoutImagePage(
        iw: Int,
        ih: Int,
        sizeMode: String,       // "fit", "a4", "letter"
        orientationMode: String, // "auto", "portrait", "landscape"
        marginMm: Int
    ): ImageLayoutPlacement {
        val mm = 72f / 25.4f
        val m = marginMm * mm
        var pw: Float
        var ph: Float
        if (sizeMode == "fit") {
            pw = iw * 0.75f + 2f * m
            ph = ih * 0.75f + 2f * m
            return ImageLayoutPlacement(
                pw = pw,
                ph = ph,
                x = m,
                y = m,
                w = iw * 0.75f,
                h = ih * 0.75f
            )
        }
        if (sizeMode == "letter") {
            pw = 612f
            ph = 792f
        } else {
            // A4 default
            pw = 595.28f
            ph = 841.89f
        }
        val wantLandscape = when (orientationMode) {
            "landscape" -> true
            "portrait" -> false
            else -> iw > ih
        }
        if (wantLandscape && pw < ph) {
            val tmp = pw
            pw = ph
            ph = tmp
        }
        val availW = max(1f, pw - 2f * m)
        val availH = max(1f, ph - 2f * m)
        val scale = min(availW / iw.toFloat(), availH / ih.toFloat())
        val drawW = iw * scale
        val drawH = ih * scale
        val drawX = m + (availW - drawW) / 2f
        val drawY = m + (availH - drawH) / 2f
        return ImageLayoutPlacement(pw = pw, ph = ph, x = drawX, y = drawY, w = drawW, h = drawH)
    }

    /** Read EXIF orientation (1..8) from JPEG bytes; returns 1 if none. */
    fun jpegOrientation(bytes: ByteArray): Int {
        try {
            if (bytes.size < 12) return 1
            if ((bytes[0].toInt() and 0xFF) != 0xFF || (bytes[1].toInt() and 0xFF) != 0xD8) return 1
            var off = 2
            val bb = ByteBuffer.wrap(bytes)
            while (off + 4 < bytes.size) {
                if ((bytes[off].toInt() and 0xFF) != 0xFF) return 1
                val marker = bytes[off + 1].toInt() and 0xFF
                bb.order(ByteOrder.BIG_ENDIAN)
                val len = bb.getShort(off + 2).toInt() and 0xFFFF
                if (marker == 0xE1 && off + 10 < bytes.size) {
                    val s = off + 4
                    if (bytes[s] == 0x45.toByte() &&
                        bytes[s + 1] == 0x78.toByte() &&
                        bytes[s + 2] == 0x69.toByte() &&
                        bytes[s + 3] == 0x66.toByte()
                    ) {
                        val t = s + 6
                        val little = bytes[t] == 0x49.toByte()
                        bb.order(if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN)
                        val ifd = t + bb.getInt(t + 4)
                        if (ifd + 2 >= bytes.size) return 1
                        val n = bb.getShort(ifd).toInt() and 0xFFFF
                        for (i in 0 until n) {
                            val e = ifd + 2 + i * 12
                            if (e + 10 >= bytes.size) break
                            val tag = bb.getShort(e).toInt() and 0xFFFF
                            if (tag == 0x0112) {
                                val v = bb.getShort(e + 8).toInt() and 0xFFFF
                                return if (v in 1..8) v else 1
                            }
                        }
                    }
                    return 1
                }
                if (marker == 0xDA) return 1
                off += 2 + len
            }
        } catch (_: Exception) {
        }
        return 1
    }

    private fun applyExifOrientation(src: Bitmap, orientation: Int): Bitmap {
        if (orientation <= 1) return src
        val m = Matrix()
        when (orientation) {
            2 -> m.postScale(-1f, 1f)
            3 -> m.postRotate(180f)
            4 -> m.postScale(1f, -1f)
            5 -> {
                m.postRotate(90f)
                m.postScale(-1f, 1f)
            }
            6 -> m.postRotate(90f)
            7 -> {
                m.postRotate(270f)
                m.postScale(-1f, 1f)
            }
            8 -> m.postRotate(270f)
        }
        return try {
            val rotated = Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
            rotated
        } catch (_: Exception) {
            src
        }
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) result = cursor.getString(idx)
                }
            }
        }
        if (result.isNullOrBlank()) {
            result = uri.lastPathSegment?.substringAfterLast('/') ?: "document.pdf"
        }
        return result
    }

    suspend fun loadPdfFromUri(
        context: Context,
        uri: Uri,
        id: Long,
        loadThumbnails: Boolean = true
    ): LoadedPdfInfo = withContext(Dispatchers.IO) {
        val rawName = getFileNameFromUri(context, uri)
        val name = if (rawName.lowercase(Locale.ROOT).endsWith(".pdf")) rawName else "$rawName.pdf"
        val cacheFile = File(context.cacheDir, "input_${System.currentTimeMillis()}_$id.pdf")
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw AppError("errBadPdf")
        inputStream.use { input ->
            FileOutputStream(cacheFile).use { output ->
                input.copyTo(output)
            }
        }
        loadPdfFromFile(cacheFile, name, id, loadThumbnails)
    }

    suspend fun loadPdfFromFile(
        file: File,
        displayName: String,
        id: Long,
        loadThumbnails: Boolean = true
    ): LoadedPdfInfo = withContext(Dispatchers.IO) {
        if (!file.exists() || file.length() < 10L) {
            throw AppError("errBadPdf")
        }
        try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val count = renderer.pageCount
                    if (count < 1) throw AppError("errBadPdf")
                    val thumbs = mutableListOf<Bitmap>()
                    if (loadThumbnails) {
                        val maxThumbCount = min(count, 60)
                        for (i in 0 until maxThumbCount) {
                            coroutineContext.ensureActive()
                            renderer.openPage(i).use { page ->
                                val scale = min(240f / max(1, page.width), 310f / max(1, page.height))
                                val bw = max(1, (page.width * scale).roundToInt())
                                val bh = max(1, (page.height * scale).roundToInt())
                                val bmp = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bmp)
                                canvas.drawColor(AndroidColor.WHITE)
                                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                thumbs.add(bmp)
                            }
                        }
                    }
                    LoadedPdfInfo(
                        id = id,
                        name = displayName,
                        size = file.length(),
                        file = file,
                        pageCount = count,
                        thumbnails = thumbs
                    )
                }
            }
        } catch (se: SecurityException) {
            throw AppError("errEncrypted")
        } catch (ae: AppError) {
            throw ae
        } catch (_: Exception) {
            throw AppError("errBadPdf")
        }
    }

    suspend fun loadImageFromUri(
        context: Context,
        uri: Uri,
        id: Long
    ): LoadedImageItem = withContext(Dispatchers.IO) {
        val rawName = getFileNameFromUri(context, uri)
        val cacheFile = File(context.cacheDir, "img_${System.currentTimeMillis()}_$id")
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw AppError("errImage", mapOf("name" to rawName))
        cacheFile.writeBytes(bytes)
        loadImageFromFile(cacheFile, rawName, id)
    }

    suspend fun loadImageFromFile(
        file: File,
        displayName: String,
        id: Long
    ): LoadedImageItem = withContext(Dispatchers.IO) {
        val bytes = file.readBytes()
        val orient = jpegOrientation(bytes)
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw AppError("errImage", mapOf("name" to displayName))
        val oriented = applyExifOrientation(decoded, orient)
        val w = oriented.width
        val h = oriented.height
        val scale = min(1f, min(320f / max(1, w), 320f / max(1, h)))
        val preview = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                oriented,
                max(1, (w * scale).roundToInt()),
                max(1, (h * scale).roundToInt()),
                true
            )
        } else {
            oriented
        }
        LoadedImageItem(
            id = id,
            name = displayName,
            size = file.length(),
            file = file,
            previewBitmap = preview,
            width = w,
            height = h
        )
    }

    /* =====================================================================
       DIRECT PDF 1.4 WRITER (/DCTDecode JPEG XObjects)
       ===================================================================== */

    data class PdfPageSpec(
        val pageWidthPt: Float,
        val pageHeightPt: Float,
        val drawXPt: Float,
        val drawYPt: Float,
        val drawWidthPt: Float,
        val drawHeightPt: Float,
        val jpegBytes: ByteArray,
        val imgPixelWidth: Int,
        val imgPixelHeight: Int
    )

    fun writeDirectPdf(pages: List<PdfPageSpec>, includeMetadata: Boolean = true): ByteArray {
        val out = ByteArrayOutputStream()
        val offsets = mutableListOf<Int>()

        fun writeAscii(s: String) {
            out.write(s.toByteArray(StandardCharsets.US_ASCII))
        }

        fun startObj(objNum: Int) {
            while (offsets.size <= objNum) offsets.add(0)
            offsets[objNum] = out.size()
            writeAscii("$objNum 0 obj\n")
        }

        fun endObj() {
            writeAscii("\nendobj\n")
        }

        fun fmtPt(v: Float): String = String.format(Locale.US, "%.2f", v)

        writeAscii("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n")

        // Obj 1: Catalog
        startObj(1)
        writeAscii("<< /Type /Catalog /Pages 2 0 R >>")
        endObj()

        // Obj 2: Pages
        val pageObjIds = pages.indices.map { idx -> 3 + idx * 3 }
        startObj(2)
        val kidsStr = pageObjIds.joinToString(" ") { "$it 0 R" }
        writeAscii("<< /Type /Pages /Kids [ $kidsStr ] /Count ${pages.size} >>")
        endObj()

        for ((idx, spec) in pages.withIndex()) {
            val pageObjId = 3 + idx * 3
            val contentObjId = pageObjId + 1
            val imgObjId = pageObjId + 2

            // Page object
            startObj(pageObjId)
            writeAscii(
                "<< /Type /Page /Parent 2 0 R " +
                    "/MediaBox [0 0 ${fmtPt(spec.pageWidthPt)} ${fmtPt(spec.pageHeightPt)}] " +
                    "/Resources << /ProcSet [/PDF /ImageC] /XObject << /Im0 $imgObjId 0 R >> >> " +
                    "/Contents $contentObjId 0 R >>"
            )
            endObj()

            // Content stream
            val contentCmd = "q ${fmtPt(spec.drawWidthPt)} 0 0 ${fmtPt(spec.drawHeightPt)} ${fmtPt(spec.drawXPt)} ${fmtPt(spec.drawYPt)} cm /Im0 Do Q\n"
            val contentBytes = contentCmd.toByteArray(StandardCharsets.US_ASCII)
            startObj(contentObjId)
            writeAscii("<< /Length ${contentBytes.size} >>\nstream\n")
            out.write(contentBytes)
            writeAscii("endstream")
            endObj()

            // Image XObject
            startObj(imgObjId)
            writeAscii(
                "<< /Type /XObject /Subtype /Image " +
                    "/Width ${spec.imgPixelWidth} /Height ${spec.imgPixelHeight} " +
                    "/ColorSpace /DeviceRGB /BitsPerComponent 8 " +
                    "/Filter /DCTDecode /Length ${spec.jpegBytes.size} >>\nstream\n"
            )
            out.write(spec.jpegBytes)
            writeAscii("\nendstream")
            endObj()
        }

        var totalObjects = 3 + pages.size * 3
        var infoObjId = -1
        if (includeMetadata) {
            infoObjId = totalObjects
            totalObjects++
            startObj(infoObjId)
            writeAscii("<< /Producer (DeskMate Tools PDFLab Android) /CreationDate (D:20261028000000Z) >>")
            endObj()
        }

        val xrefOffset = out.size()
        writeAscii("xref\n0 $totalObjects\n")
        writeAscii("0000000000 65535 f \n")
        for (objNum in 1 until totalObjects) {
            val off = offsets[objNum]
            writeAscii(String.format(Locale.US, "%010d 00000 n \n", off))
        }
        val infoRef = if (infoObjId > 0) " /Info $infoObjId 0 R" else ""
        writeAscii("trailer\n<< /Size $totalObjects /Root 1 0 R$infoRef >>\nstartxref\n$xrefOffset\n%%EOF")
        return out.toByteArray()
    }

    private fun renderPageSpecFromRenderer(
        renderer: PdfRenderer,
        pageIndex0: Int,
        dpi: Int = 180,
        jpegQuality: Int = 86,
        extraRotationDeg: Int = 0
    ): PdfPageSpec {
        renderer.openPage(pageIndex0).use { page ->
            val origW = max(1, page.width)
            val origH = max(1, page.height)
            val scale = (dpi / 72f).coerceIn(0.6f, 4.2f)
            val renderW = max(1, (origW * scale).roundToInt())
            val renderH = max(1, (origH * scale).roundToInt())

            val rawBmp = Bitmap.createBitmap(renderW, renderH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(rawBmp)
            canvas.drawColor(AndroidColor.WHITE)
            page.render(rawBmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            val rot = ((extraRotationDeg % 360) + 360) % 360
            val finalBmp = if (rot != 0) {
                val m = Matrix().apply { postRotate(rot.toFloat()) }
                val rotated = Bitmap.createBitmap(rawBmp, 0, 0, rawBmp.width, rawBmp.height, m, true)
                if (rotated != rawBmp) rawBmp.recycle()
                rotated
            } else {
                rawBmp
            }

            val swap = (rot % 180) != 0
            val pageWPt = if (swap) origH.toFloat() else origW.toFloat()
            val pageHPt = if (swap) origW.toFloat() else origH.toFloat()

            val baos = ByteArrayOutputStream()
            finalBmp.compress(Bitmap.CompressFormat.JPEG, jpegQuality.coerceIn(10, 100), baos)
            val bw = finalBmp.width
            val bh = finalBmp.height
            finalBmp.recycle()

            return PdfPageSpec(
                pageWidthPt = pageWPt,
                pageHeightPt = pageHPt,
                drawXPt = 0f,
                drawYPt = 0f,
                drawWidthPt = pageWPt,
                drawHeightPt = pageHPt,
                jpegBytes = baos.toByteArray(),
                imgPixelWidth = bw,
                imgPixelHeight = bh
            )
        }
    }

    /* =====================================================================
       1) MERGE PDF
       ===================================================================== */
    suspend fun mergePdfs(
        items: List<LoadedPdfInfo>,
        onProgress: suspend (Int, Int) -> Unit
    ): Pair<ByteArray, Int> = withContext(Dispatchers.IO) {
        val specs = mutableListOf<PdfPageSpec>()
        for ((idx, item) in items.withIndex()) {
            coroutineContext.ensureActive()
            onProgress(idx + 1, items.size)
            ParcelFileDescriptor.open(item.file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    for (p in 0 until renderer.pageCount) {
                        coroutineContext.ensureActive()
                        specs.add(renderPageSpecFromRenderer(renderer, p, dpi = 175, jpegQuality = 85))
                    }
                }
            }
        }
        val bytes = writeDirectPdf(specs, includeMetadata = false)
        Pair(bytes, specs.size)
    }

    /* =====================================================================
       2) SPLIT PDF
       ===================================================================== */
    suspend fun splitPdf(
        info: LoadedPdfInfo,
        jobs: List<SplitJob>,
        onProgress: suspend (Int, Int) -> Unit
    ): ByteArray = withContext(Dispatchers.IO) {
        val zipEntries = mutableListOf<ZipFileEntry>()
        ParcelFileDescriptor.open(info.file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                for ((i, job) in jobs.withIndex()) {
                    coroutineContext.ensureActive()
                    onProgress(i + 1, jobs.size)
                    val pageSpecs = job.pages1Based.map { page1 ->
                        renderPageSpecFromRenderer(renderer, page1 - 1, dpi = 175, jpegQuality = 85)
                    }
                    val pdfBytes = writeDirectPdf(pageSpecs, includeMetadata = false)
                    zipEntries.add(ZipFileEntry(name = job.name, data = pdfBytes))
                }
            }
        }
        buildZip(zipEntries)
    }

    /* =====================================================================
       3) COMPRESS PDF
       ===================================================================== */
    suspend fun compressPdf(
        info: LoadedPdfInfo,
        dpi: Int,
        quality: Int,
        stripMetadata: Boolean,
        onProgress: suspend (Float, Int, Int) -> Unit
    ): CompressResultData = withContext(Dispatchers.IO) {
        val specs = mutableListOf<PdfPageSpec>()
        ParcelFileDescriptor.open(info.file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                val total = max(1, renderer.pageCount)
                for (p in 0 until total) {
                    coroutineContext.ensureActive()
                    val frac = 0.1f + 0.8f * (p.toFloat() / total.toFloat())
                    onProgress(frac, p + 1, total)
                    specs.add(
                        renderPageSpecFromRenderer(
                            renderer = renderer,
                            pageIndex0 = p,
                            dpi = dpi.coerceIn(50, 300),
                            jpegQuality = quality.coerceIn(10, 100)
                        )
                    )
                }
            }
        }
        val candidateBytes = writeDirectPdf(specs, includeMetadata = !stripMetadata)
        val origBytes = info.file.readBytes()
        if (candidateBytes.size >= origBytes.size) {
            CompressResultData(bytes = origBytes, alreadyOptimized = true)
        } else {
            CompressResultData(bytes = candidateBytes, alreadyOptimized = false)
        }
    }

    /* =====================================================================
       4) IMAGES -> PDF
       ===================================================================== */
    suspend fun imagesToPdf(
        items: List<LoadedImageItem>,
        sizeMode: String,
        orientationMode: String,
        marginMm: Int,
        onProgress: suspend (Int, Int) -> Unit
    ): ByteArray = withContext(Dispatchers.IO) {
        val specs = mutableListOf<PdfPageSpec>()
        for ((idx, item) in items.withIndex()) {
            coroutineContext.ensureActive()
            onProgress(idx + 1, items.size)
            val rawBytes = item.file.readBytes()
            val orient = jpegOrientation(rawBytes)
            val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
                ?: throw AppError("errImage", mapOf("name" to item.name))
            val oriented = applyExifOrientation(decoded, orient)

            // Composite onto white background in case of PNG/WebP transparency
            val flatBmp = Bitmap.createBitmap(oriented.width, oriented.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(flatBmp)
            canvas.drawColor(AndroidColor.WHITE)
            canvas.drawBitmap(oriented, 0f, 0f, null)
            if (oriented != decoded) oriented.recycle()
            decoded.recycle()

            val baos = ByteArrayOutputStream()
            flatBmp.compress(Bitmap.CompressFormat.JPEG, 90, baos)
            val iw = flatBmp.width
            val ih = flatBmp.height
            flatBmp.recycle()

            val placement = layoutImagePage(iw, ih, sizeMode, orientationMode, marginMm)
            specs.add(
                PdfPageSpec(
                    pageWidthPt = placement.pw,
                    pageHeightPt = placement.ph,
                    drawXPt = placement.x,
                    drawYPt = placement.y,
                    drawWidthPt = placement.w,
                    drawHeightPt = placement.h,
                    jpegBytes = baos.toByteArray(),
                    imgPixelWidth = iw,
                    imgPixelHeight = ih
                )
            )
        }
        writeDirectPdf(specs, includeMetadata = false)
    }

    /* =====================================================================
       5) PDF -> IMAGES (ZIP)
       ===================================================================== */
    suspend fun pdfToImagesZip(
        info: LoadedPdfInfo,
        pages1Based: List<Int>,
        format: String, // "png" or "jpeg"
        dpi: Int,
        onProgress: suspend (Int, Int) -> Unit
    ): ByteArray = withContext(Dispatchers.IO) {
        val ext = if (format == "png") "png" else "jpg"
        val base = safeName(stripExt(info.name))
        val widthDigits = info.pageCount.toString().length
        val entries = mutableListOf<ZipFileEntry>()

        ParcelFileDescriptor.open(info.file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                for ((i, pageNum1) in pages1Based.withIndex()) {
                    coroutineContext.ensureActive()
                    onProgress(i + 1, pages1Based.size)
                    renderer.openPage(pageNum1 - 1).use { page ->
                        val baseW = max(1, page.width).toDouble()
                        val baseH = max(1, page.height).toDouble()
                        var scale = dpi.toDouble() / 72.0
                        // Cap dimensions safely for mobile memory while preserving high DPI
                        val k = min(
                            1.0,
                            min(
                                4096.0 / (baseW * scale),
                                min(
                                    4096.0 / (baseH * scale),
                                    sqrt(12_000_000.0 / (baseW * scale * baseH * scale))
                                )
                            )
                        )
                        scale *= k
                        val w = max(1, floor(baseW * scale).toInt())
                        val h = max(1, floor(baseH * scale).toInt())
                        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bmp)
                        canvas.drawColor(AndroidColor.WHITE)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        val baos = ByteArrayOutputStream()
                        val compressFmt = if (format == "png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                        bmp.compress(compressFmt, 92, baos)
                        bmp.recycle()
                        val fileName = "${base}_page_${padNum(pageNum1, widthDigits)}.$ext"
                        entries.add(ZipFileEntry(name = fileName, data = baos.toByteArray()))
                    }
                }
            }
        }
        buildZip(entries)
    }

    /* =====================================================================
       6) ROTATE PDF PAGES
       ===================================================================== */
    suspend fun rotatePdf(
        info: LoadedPdfInfo,
        rotationsDeg: List<Int>
    ): ByteArray = withContext(Dispatchers.IO) {
        val specs = mutableListOf<PdfPageSpec>()
        ParcelFileDescriptor.open(info.file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                for (p in 0 until renderer.pageCount) {
                    coroutineContext.ensureActive()
                    val rot = rotationsDeg.getOrElse(p) { 0 }
                    specs.add(
                        renderPageSpecFromRenderer(
                            renderer = renderer,
                            pageIndex0 = p,
                            dpi = 180,
                            jpegQuality = 88,
                            extraRotationDeg = rot
                        )
                    )
                }
            }
        }
        writeDirectPdf(specs, includeMetadata = false)
    }

    /* =====================================================================
       7) REMOVE PAGES / 8) EXTRACT PAGES
       ===================================================================== */
    suspend fun buildSubsetPdf(
        info: LoadedPdfInfo,
        keepPages1Based: List<Int>
    ): ByteArray = withContext(Dispatchers.IO) {
        val specs = mutableListOf<PdfPageSpec>()
        ParcelFileDescriptor.open(info.file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                for (p1 in keepPages1Based) {
                    coroutineContext.ensureActive()
                    specs.add(
                        renderPageSpecFromRenderer(
                            renderer = renderer,
                            pageIndex0 = p1 - 1,
                            dpi = 180,
                            jpegQuality = 86
                        )
                    )
                }
            }
        }
        writeDirectPdf(specs, includeMetadata = false)
    }

    suspend fun extractSeparateZip(
        info: LoadedPdfInfo,
        selectedPages1Based: List<Int>,
        onProgress: suspend (Int, Int) -> Unit
    ): ByteArray = withContext(Dispatchers.IO) {
        val base = safeName(stripExt(info.name))
        val widthDigits = info.pageCount.toString().length
        val entries = mutableListOf<ZipFileEntry>()
        ParcelFileDescriptor.open(info.file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                for ((idx, p1) in selectedPages1Based.withIndex()) {
                    coroutineContext.ensureActive()
                    onProgress(idx + 1, selectedPages1Based.size)
                    val spec = renderPageSpecFromRenderer(renderer, p1 - 1, dpi = 180, jpegQuality = 86)
                    val pdfBytes = writeDirectPdf(listOf(spec), includeMetadata = false)
                    entries.add(
                        ZipFileEntry(
                            name = "${base}_page_${padNum(p1, widthDigits)}.pdf",
                            data = pdfBytes
                        )
                    )
                }
            }
        }
        buildZip(entries)
    }

    /* =====================================================================
       ZIP BUILDER & INSPECTOR
       ===================================================================== */
    fun buildZip(entries: List<ZipFileEntry>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            val usedNames = mutableSetOf<String>()
            for (entry in entries) {
                var name = entry.name
                var suffix = 1
                while (!usedNames.add(name)) {
                    val dot = entry.name.lastIndexOf('.')
                    name = if (dot > 0) {
                        "${entry.name.substring(0, dot)}_${++suffix}${entry.name.substring(dot)}"
                    } else {
                        "${entry.name}_${++suffix}"
                    }
                }
                val ze = ZipEntry(name)
                zos.putNextEntry(ze)
                zos.write(entry.data)
                zos.closeEntry()
            }
        }
        return baos.toByteArray()
    }

    fun inspectZipEntries(zipBytes: ByteArray): List<Pair<String, Long>> {
        val list = mutableListOf<Pair<String, Long>>()
        try {
            ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
                while (true) {
                    val entry = zis.nextEntry ?: break
                    val buf = ByteArray(4096)
                    var total = 0L
                    while (true) {
                        val r = zis.read(buf)
                        if (r <= 0) break
                        total += r
                    }
                    list.add(entry.name to total)
                    zis.closeEntry()
                }
            }
        } catch (_: Exception) {
        }
        return list
    }

    /* =====================================================================
       SAMPLE PDF & SAMPLE IMAGES GENERATORS (For One-Tap Emulator/Device Demo)
       ===================================================================== */
    suspend fun createSamplePdfFile(
        context: Context,
        variantIndex: Int = 1,
        pageCount: Int = 6
    ): Pair<File, String> = withContext(Dispatchers.IO) {
        val docName = when (variantIndex) {
            1 -> "DeskMate_Annual_Report.pdf"
            2 -> "Project_Roadmap_2026.pdf"
            else -> "Financial_Summary_Q4.pdf"
        }
        val file = File(context.cacheDir, "sample_${variantIndex}_${System.currentTimeMillis()}.pdf")
        val pdfDoc = PdfDocument()

        val palette = listOf(
            AndroidColor.rgb(37, 99, 235),  // Royal Blue
            AndroidColor.rgb(124, 58, 237), // Violet
            AndroidColor.rgb(5, 150, 105),  // Emerald
            AndroidColor.rgb(217, 119, 6),  // Amber
            AndroidColor.rgb(225, 29, 72),  // Rose
            AndroidColor.rgb(8, 145, 178)   // Cyan
        )

        val paintBg = Paint(Paint.ANTI_ALIAS_FLAG)
        val paintHeader = Paint(Paint.ANTI_ALIAS_FLAG)
        val paintTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintSubtitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(220, 255, 255, 255)
            textSize = 12f
        }
        val paintBodyBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(15, 23, 42)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintBody = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(71, 85, 105)
            textSize = 12f
        }
        val paintPageBadge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        for (p in 1..pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, p).create()
            val page = pdfDoc.startPage(pageInfo)
            val c = page.canvas
            val accent = palette[(p + variantIndex - 2) % palette.size]

            // Background
            paintBg.color = AndroidColor.rgb(248, 250, 252)
            c.drawRect(0f, 0f, 595f, 842f, paintBg)

            // Header Banner
            paintHeader.color = accent
            c.drawRoundRect(RectF(28f, 28f, 567f, 165f), 18f, 18f, paintHeader)

            c.drawText(stripExt(docName).replace('_', ' '), 50f, 78f, paintTitle)
            c.drawText("PDFLab • DeskMate Offline Suite • Document Section #$p", 50f, 105f, paintSubtitle)
            c.drawText("100% On-Device Vector & Raster Sample Page", 50f, 128f, paintSubtitle)

            // Large Page Number Badge on Header Right
            paintBg.color = AndroidColor.argb(55, 255, 255, 255)
            c.drawCircle(505f, 96f, 40f, paintBg)
            c.drawText("P$p", 505f, 108f, paintPageBadge)

            // Content Card 1: Visual Bar Chart
            paintBg.color = AndroidColor.WHITE
            c.drawRoundRect(RectF(28f, 190f, 567f, 430f), 14f, 14f, paintBg)
            c.drawText("Quarterly Performance Metrics — Page $p of $pageCount", 48f, 226f, paintBodyBold)
            c.drawText("Visualizing sample graphical bars to test rotation, extraction, and DPI compression.", 48f, 248f, paintBody)

            for (barIdx in 0 until 5) {
                val barLeft = 58f + barIdx * 96f
                val barH = 50f + ((barIdx * 37 + p * 29) % 110)
                val barTop = 395f - barH
                paintHeader.color = palette[(barIdx + p) % palette.size]
                c.drawRoundRect(RectF(barLeft, barTop, barLeft + 56f, 395f), 8f, 8f, paintHeader)
                c.drawText("Q${barIdx + 1}", barLeft + 18f, 415f, paintBody)
            }

            // Content Card 2: Structured Table Rows
            paintBg.color = AndroidColor.WHITE
            c.drawRoundRect(RectF(28f, 452f, 567f, 750f), 14f, 14f, paintBg)
            c.drawText("Document Specification & Audit Log", 48f, 488f, paintBodyBold)

            for (row in 0 until 5) {
                val yTop = 510f + row * 44f
                paintBg.color = if (row % 2 == 0) AndroidColor.rgb(241, 245, 249) else AndroidColor.WHITE
                c.drawRoundRect(RectF(44f, yTop, 551f, yTop + 36f), 6f, 6f, paintBg)
                c.drawText("Item #${p}0${row + 1} • Module Verification Check", 58f, yTop + 23f, paintBody)
                c.drawText("PASS • ${(row + 1) * 18}%", 445f, yTop + 23f, paintBodyBold)
            }

            // Footer
            c.drawText("DeskMate Tools • PDFLab Android — Page $p / $pageCount", 200f, 805f, paintBody)
            pdfDoc.finishPage(page)
        }

        FileOutputStream(file).use { pdfDoc.writeTo(it) }
        pdfDoc.close()
        Pair(file, docName)
    }

    suspend fun createSampleImages(context: Context): List<Pair<File, String>> = withContext(Dispatchers.IO) {
        val specs = listOf(
            Triple("01_Architecture_Diagram.jpg", AndroidColor.rgb(37, 99, 235), "System Architecture"),
            Triple("02_Analytics_Dashboard.jpg", AndroidColor.rgb(5, 150, 105), "Revenue & Growth"),
            Triple("03_Product_Wireframe.png", AndroidColor.rgb(124, 58, 237), "UI Design Mockup"),
            Triple("04_Release_Checklist.jpg", AndroidColor.rgb(217, 119, 6), "Sprint Milestone")
        )
        val result = mutableListOf<Pair<File, String>>()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(230, 255, 255, 255)
            textSize = 26f
        }

        for ((idx, spec) in specs.withIndex()) {
            val (name, bgColor, label) = spec
            val bmp = Bitmap.createBitmap(900, 1200, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp)
            paint.color = bgColor
            c.drawRect(0f, 0f, 900f, 1200f, paint)

            paint.color = AndroidColor.argb(45, 255, 255, 255)
            c.drawCircle(740f, 220f, 220f, paint)
            c.drawCircle(160f, 980f, 260f, paint)

            paint.color = AndroidColor.argb(235, 255, 255, 255)
            c.drawRoundRect(RectF(70f, 360f, 830f, 1040f), 32f, 32f, paint)

            c.drawText("IMAGE #${idx + 1}", 80f, 150f, titlePaint)
            c.drawText(label, 80f, 215f, subPaint)
            c.drawText("PDFLab Sample High-Res Asset", 80f, 265f, subPaint)

            paint.color = bgColor
            for (bar in 0 until 4) {
                val top = 450f + bar * 130f
                val width = 320f + ((bar * 110 + idx * 70) % 360)
                c.drawRoundRect(RectF(130f, top, 130f + width, top + 72f), 16f, 16f, paint)
            }

            val file = File(context.cacheDir, "sample_img_${idx + 1}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { fos ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 92, fos)
            }
            bmp.recycle()
            result.add(file to name)
        }
        result
    }
}
