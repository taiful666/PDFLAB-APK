package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ToolCompressColor
import com.example.ui.theme.ToolExtractColor
import com.example.ui.theme.ToolImg2PdfColor
import com.example.ui.theme.ToolMergeColor
import com.example.ui.theme.ToolPdf2ImgColor
import com.example.ui.theme.ToolRemoveColor
import com.example.ui.theme.ToolRotateColor
import com.example.ui.theme.ToolSplitColor
import java.util.Locale

enum class AppLang(val code: String, val label: String) {
    BN("bn", "বাংলা"),
    EN("en", "English")
}

enum class ToolCategory {
    ALL,
    ORGANIZE,
    CONVERT,
    OPTIMIZE
}

enum class ToolId(
    val key: String,
    val enTitle: String,
    val color: Color,
    val category: ToolCategory
) {
    MERGE("merge", "Merge PDF", ToolMergeColor, ToolCategory.ORGANIZE),
    SPLIT("split", "Split PDF", ToolSplitColor, ToolCategory.ORGANIZE),
    COMPRESS("compress", "Compress PDF", ToolCompressColor, ToolCategory.OPTIMIZE),
    IMG2PDF("img2pdf", "Images to PDF", ToolImg2PdfColor, ToolCategory.CONVERT),
    PDF2IMG("pdf2img", "PDF to Images", ToolPdf2ImgColor, ToolCategory.CONVERT),
    ROTATE("rotate", "Rotate PDF pages", ToolRotateColor, ToolCategory.ORGANIZE),
    REMOVE("remove", "Remove PDF pages", ToolRemoveColor, ToolCategory.ORGANIZE),
    EXTRACT("extract", "Extract PDF pages", ToolExtractColor, ToolCategory.ORGANIZE)
}

object I18n {
    private val enMap = mapOf(
        "docTitle" to "DeskMate Tools • PDFLab",
        "brandSub" to "100% Offline • Private & Secure Productivity Suite",
        "offlineBadge" to "100% Offline",
        "themeTitle" to "Switch Dark / Light Mode",
        "footerNote" to "All processing happens locally on your Android device. No data leaves your phone.",
        "pleaseWait" to "Please wait — processing on device…",
        "heroTitle" to "Simple & Fast Offline PDF Tools",
        "heroSub" to "Merge, split, compress, rotate, extract, remove pages, or convert PDFs on your phone without any internet connection.",
        "privacy" to "Your files are never uploaded anywhere — 100% of processing happens securely on your device",
        "start" to "Open Tool →",
        "back" to "All Tools",
        "searchPh" to "Search PDF tools (e.g. Merge, Split, Compress)…",
        "catAll" to "All Tools (8)",
        "catOrganize" to "Organize Pages",
        "catConvert" to "Convert",
        "catOptimize" to "Optimize",
        "recentTitle" to "Recent Processed Files",
        "recentEmpty" to "Files you create will appear here for quick preview, saving, or sharing.",
        "clearRecent" to "Clear History",
        "trySamplePdf" to "Try with Sample PDF",
        "trySampleMultiPdf" to "Load 3 Sample PDFs",
        "trySampleImages" to "Load 4 Sample Images",
        "previewBtn" to "Preview Output",
        "shareBtn" to "Share File",
        "saveDeviceBtn" to "Save to Device",
        "savedToDevice" to "Saved to device successfully!",

        "merge_title" to "Merge PDF",
        "merge_desc" to "Combine multiple PDF files in your preferred order into a single document.",
        "split_title" to "Split PDF",
        "split_desc" to "Split a PDF into individual single pages, custom page ranges, or fixed intervals.",
        "compress_title" to "Compress PDF",
        "compress_desc" to "Reduce file size of one or multiple PDFs while preserving quality. Download individually or as ZIP.",
        "img2pdf_title" to "Images to PDF",
        "img2pdf_desc" to "Convert JPG, PNG, and WebP images into a clean, customizable PDF document.",
        "pdf2img_title" to "PDF to Images",
        "pdf2img_desc" to "Render PDF pages into high-resolution PNG or JPEG images bundled in a ZIP.",
        "rotate_title" to "Rotate PDF Pages",
        "rotate_desc" to "Rotate specific pages or the entire document by 90° or 180° with live visual preview.",
        "remove_title" to "Remove PDF Pages",
        "remove_desc" to "Tap unwanted pages to delete them and build a clean PDF document.",
        "extract_title" to "Extract PDF Pages",
        "extract_desc" to "Select specific pages to save as a new merged PDF or separate PDFs in a ZIP.",

        "dzTitle" to "Select a PDF document from your device",
        "dzTitleMulti" to "Select one or more PDF files from your device",
        "dzTitleImg" to "Select images from your gallery or files",
        "dzOr" to "or test immediately",
        "selectPdf" to "Select PDF File",
        "selectPdfs" to "Select PDF Files",
        "selectImages" to "Select Images",
        "dzHintPdf" to "Supports all standard PDF files. Processed 100% offline on your device.",
        "dzHintImg" to "Supports JPG, PNG, and WebP images.",
        "errOnlyPdf" to "Please select valid PDF files only.",
        "errOnlyImg" to "Please select JPG, PNG, or WebP images only.",
        "errGeneric" to "Something went wrong while processing.",
        "errEncrypted" to "This PDF is password-protected / encrypted and cannot be processed.",
        "errBadPdf" to "Could not read this file. It may be corrupted or not a valid PDF.",
        "reading" to "Reading file…",
        "readingN" to "Reading {n}/{total}: {name}",
        "saving" to "Saving PDF…",
        "zipping" to "Building ZIP archive…",
        "building" to "Building PDF…",

        "doneTitle" to "Done! Your file is ready",
        "dlHint" to "Save the file to your Downloads/Document folder, preview pages inside the app, or share directly.",
        "download" to "Save to Device",
        "startOver" to "Start Over",
        "changeFile" to "Change File",
        "fileName" to "File Name",
        "fileSize" to "File Size",
        "totalPages" to "Total Pages",
        "filesMerged" to "Files Merged",
        "filesCreated" to "Files Created",
        "origSize" to "Original Size",
        "newSize" to "Compressed Size",
        "savedLabel" to "Space Saved",
        "imagesCreated" to "Images Created",
        "resolution" to "Resolution",
        "pagesRemoved" to "Pages Removed",
        "pagesWord" to "pages",
        "pageWord" to "page",
        "page" to "Page",
        "remove" to "Remove",

        "addMore" to "Add More PDFs",
        "sortAZ" to "Sort A–Z",
        "clearAll" to "Clear All",
        "mergeHint" to "Use the up/down arrows to set the document order. Pages will be combined from top to bottom.",
        "mergeNow" to "Merge PDFs Now",
        "errNeed2" to "Please add at least 2 PDF files to merge.",
        "merging" to "Merging PDFs…",
        "mergingN" to "Merging file {n} of {total}…",
        "moveUp" to "Move Up",
        "moveDown" to "Move Down",
        "mergeSummary" to "{files} files · total {pages} pages",

        "splitMode" to "Split Method",
        "splitSingle" to "Single Pages",
        "splitSingleD" to "Every page becomes a separate PDF file.",
        "splitRanges" to "Custom Ranges",
        "splitRangesD" to "Each range becomes one PDF, e.g. 1-3, 4-5 or 1, 3, 5-8.",
        "splitEvery" to "Every N Pages",
        "splitEveryD" to "Split into fixed chunks of N pages each.",
        "every" to "Every",
        "rangesPh" to "e.g. 1-3, 4-5",
        "splitNow" to "Split & Build ZIP",
        "willCreate" to "{n} PDF files will be created",
        "errRangeTok" to "Invalid page range: \"{tok}\"",
        "errEnterRanges" to "Enter page ranges to split.",
        "errEveryN" to "Enter a valid page count (1 or more).",
        "splitting" to "Splitting PDF…",
        "splittingN" to "Creating file {n} of {total}…",

        "cmpLevel" to "Compression Level",
        "cmpRec" to "Recommended",
        "cmpRecD" to "Balanced size & clarity · 144 DPI, 75%",
        "cmpExt" to "Extreme Compression",
        "cmpExtD" to "Smallest file size; slightly lower image clarity · 72 DPI, 50%",
        "cmpLow" to "Low Compression",
        "cmpLowD" to "High visual quality, moderate reduction · 200 DPI, 85%",
        "cmpNote" to "Pages and embedded graphics above the target DPI are downsampled and re-encoded at the selected JPEG quality.",
        "compressNow" to "Compress PDF",
        "compressing" to "Compressing PDF…",
        "stPending" to "Pending",
        "stCompressing" to "Compressing…",
        "stDone" to "Done",
        "stFailed" to "Failed",
        "savedPct" to "Saved {pct}%",
        "optAlready" to "Already optimized",
        "cmpCustom" to "Custom",
        "cmpCustomD" to "Specify your own DPI & image quality below.",
        "dpiLabel" to "DPI (Resolution)",
        "qualityLabel" to "Image Quality (%)",
        "stripLabel" to "Strip metadata & unused objects",
        "stripHint" to "Removes document metadata and rebuilds clean page streams.",
        "errCmpValues" to "DPI must be between {dmin}–{dmax} and Quality between {qmin}–{qmax}.",
        "compScan" to "Analyzing pages…",
        "compressAll" to "Compress All PDFs",
        "compressAgain" to "Re-compress All",
        "dlAllZip" to "Save All as ZIP",
        "stopBtn" to "Stop",
        "batchHint" to "Add as many PDFs as you like. They are compressed sequentially on your device using the same settings.",
        "batchSummary" to "{n} files · total {size}",
        "batchDone" to "{ok} of {total} done · {orig} → {now} (saved {pct}%)",
        "batchFailed" to "{n} failed",
        "batchProgress" to "File {n} of {total}: {name}",
        "stopping" to "Stopping after current file…",
        "batchStopped" to "Stopped. {ok} file(s) completed.",
        "batchFinished" to "Compression finished: {ok} of {total} completed.",
        "zipBuilding" to "Building ZIP…",
        "noneToZip" to "There are no compressed files yet.",
        "dupSkipped" to "{n} duplicate file(s) skipped.",
        "compImages" to "Optimizing page {n} of {total}…",
        "alreadyOptimized" to "This file was already well optimized, so the original is kept unchanged.",

        "addMoreImg" to "Add More Images",
        "imgHint" to "Use the left/right arrows on each image card to reorder pages.",
        "imgOptions" to "Page Layout Options",
        "pageSize" to "Page Size",
        "orientation" to "Orientation",
        "margins" to "Margins",
        "optFit" to "Fit to Image",
        "optA4" to "A4",
        "optLetter" to "US Letter",
        "optAuto" to "Auto (match image)",
        "optPortrait" to "Portrait",
        "optLandscape" to "Landscape",
        "optM0" to "0 mm (No margin)",
        "optM10" to "10 mm",
        "optM20" to "20 mm",
        "moveLeft" to "Move Left",
        "moveRight" to "Move Right",
        "imgSummary" to "{n} images selected",
        "imgNow" to "Convert to PDF",
        "errNoImages" to "Please add at least one image.",
        "converting" to "Creating PDF…",
        "convertingN" to "Adding image {n} of {total}…",
        "errImage" to "Could not read image \"{name}\".",

        "format" to "Image Format",
        "pagesOpt" to "Pages (optional)",
        "allPagesPh" to "All pages (or e.g. 1-3, 5)",
        "dpiNote" to "Higher resolution produces sharper images but larger files. 150 DPI is great for screens, 300 DPI for print.",
        "p2iNow" to "Convert to Images (ZIP)",
        "rendering" to "Rendering pages…",
        "renderingN" to "Rendering page {n} of {total}…",
        "errRender" to "Could not create an image from this page.",

        "rotAllCW" to "All 90° CW",
        "rotAllCCW" to "All 90° CCW",
        "rotAll180" to "All 180°",
        "resetAll" to "Reset All",
        "rotCW" to "Rotate 90° CW",
        "rotCCW" to "Rotate 90° CCW",
        "rotSummary" to "{n} of {total} pages rotated",
        "rotNow" to "Save Rotated PDF",
        "errNoRotation" to "Rotate at least one page first.",

        "delLabel" to "Pages to delete",
        "extLabel" to "Pages to extract",
        "delPh" to "e.g. 2, 4-6",
        "extPh" to "e.g. 1, 3, 5-7",
        "selEven" to "Even Pages",
        "selOdd" to "Odd Pages",
        "selAll" to "Select All",
        "selClear" to "Clear Selection",
        "delHint" to "Tap any page thumbnail to mark it for deletion (tap again to undo).",
        "extHint" to "Tap the page thumbnails you want to keep in the extracted file.",
        "delNow" to "Remove Pages & Save",
        "extNow" to "Extract Pages & Save",
        "deleted" to "DELETE",
        "delSummary" to "{n} marked for deletion · {left} will remain",
        "extSummary" to "{n} of {total} pages selected",
        "errSelDel" to "Select at least one page to delete.",
        "errSelExt" to "Select at least one page to extract.",
        "errDelAll" to "You cannot delete every page of the document.",
        "extOne" to "One Merged PDF",
        "extOneD" to "All selected pages combined into a single PDF file.",
        "extSep" to "Separate PDFs (ZIP)",
        "extSepD" to "Each selected page saved as its own PDF inside a ZIP archive."
    )

    private val bnMap = mapOf(
        "docTitle" to "DeskMate Tools • PDFLab",
        "brandSub" to "১০০% অফলাইন • ব্যক্তিগত ও নিরাপদ প্রোডাক্টিভিটি স্যুট",
        "offlineBadge" to "১০০% অফলাইন",
        "themeTitle" to "ডার্ক / লাইট মোড পরিবর্তন",
        "footerNote" to "সব প্রসেসিং আপনার ফোনেই হয়। কোনো তথ্য বা ফাইল ইন্টারনেটে যায় না।",
        "pleaseWait" to "অনুগ্রহ করে অপেক্ষা করুন — প্রসেস হচ্ছে…",
        "heroTitle" to "সহজ ও দ্রুত অফলাইন পিডিএফ টুলস",
        "heroSub" to "কোনো ইন্টারনেট ছাড়াই আপনার ফোনে পিডিএফ ফাইল সহজে একত্র করুন, কাটুন, সাইজ কমান, পৃষ্ঠা ঘোরান বা রূপান্তর করুন।",
        "privacy" to "আপনার কোনো ফাইল কোথাও আপলোড হয় না — সব প্রসেসিং সম্পূর্ণ আপনার ডিভাইসে নিরাপদে হয়",
        "start" to "শুরু করুন →",
        "back" to "সব টুলস",
        "searchPh" to "টুল খুঁজুন (যেমন: মার্জ, স্প্লিট, কমপ্রেস)…",
        "catAll" to "সব টুলস (৮)",
        "catOrganize" to "পৃষ্ঠা সাজান",
        "catConvert" to "রূপান্তর",
        "catOptimize" to "অপটিমাইজ",
        "recentTitle" to "সাম্প্রতিক তৈরি ফাইল",
        "recentEmpty" to "আপনার তৈরি করা পিডিএফ ও ZIP ফাইল এখানে দেখা যাবে।",
        "clearRecent" to "ইতিহাস মুছুন",
        "trySamplePdf" to "ডেমো পিডিএফ দিয়ে টেস্ট করুন",
        "trySampleMultiPdf" to "৩টি ডেমো পিডিএফ লোড করুন",
        "trySampleImages" to "৪টি ডেমো ছবি লোড করুন",
        "previewBtn" to "প্রিভিউ দেখুন",
        "shareBtn" to "শেয়ার করুন",
        "saveDeviceBtn" to "ডিভাইসে সংরক্ষণ",
        "savedToDevice" to "ফাইলটি সফলভাবে সংরক্ষণ করা হয়েছে!",

        "merge_title" to "পিডিএফ মার্জ",
        "merge_desc" to "একাধিক পিডিএফ ফাইলকে একটিমাত্র ফাইলে ক্রমানুসারে জোড়া লাগান।",
        "split_title" to "পিডিএফ স্প্লিট",
        "split_desc" to "পিডিএফ ফাইলকে আলাদা আলাদা পৃষ্ঠায় বা নির্দিষ্ট রেঞ্জে ভাগ করুন।",
        "compress_title" to "পিডিএফ কমপ্রেস",
        "compress_desc" to "এক বা একাধিক পিডিএফের মান ঠিক রেখে সাইজ ছোট করুন। সব একসাথে ZIP হিসেবে নামান।",
        "img2pdf_title" to "ছবি থেকে পিডিএফ",
        "img2pdf_desc" to "ছবি (JPG, PNG, WebP) নির্বাচন করে একটি সুন্দর পিডিএফ ডকুমেন্টে রূপান্তর করুন।",
        "pdf2img_title" to "পিডিএফ থেকে ছবি",
        "pdf2img_desc" to "পিডিএফের পৃষ্ঠাগুলোকে হাই-কোয়ালিটি PNG অথবা JPG ছবিতে কনভার্ট করুন।",
        "rotate_title" to "পিডিএফ পৃষ্ঠা ঘোরান",
        "rotate_desc" to "পিডিএফের নির্দিষ্ট পৃষ্ঠা বা সবগুলো পৃষ্ঠা ৯০° বা ১৮০° কোণে ঘুরিয়ে সোজা করুন।",
        "remove_title" to "পিডিএফ পৃষ্ঠা মুছুন",
        "remove_desc" to "পিডিএফ থেকে অপ্রয়োজনীয় পৃষ্ঠা ক্লিক করে মুছে নতুন ফাইল তৈরি করুন।",
        "extract_title" to "পিডিএফ পৃষ্ঠা আলাদা করুন",
        "extract_desc" to "পিডিএফ থেকে প্রয়োজনীয় পৃষ্ঠাগুলো বেছে আলাদা একটি ফাইলে সংরক্ষণ করুন।",

        "dzTitle" to "আপনার ডিভাইস থেকে পিডিএফ ফাইল নির্বাচন করুন",
        "dzTitleMulti" to "এক বা একাধিক পিডিএফ ফাইল নির্বাচন করুন",
        "dzTitleImg" to "গ্যালারি বা ফাইল থেকে ছবি নির্বাচন করুন",
        "dzOr" to "অথবা তাৎক্ষণিক টেস্ট করুন",
        "selectPdf" to "পিডিএফ ফাইল নির্বাচন করুন",
        "selectPdfs" to "পিডিএফ ফাইল নির্বাচন করুন",
        "selectImages" to "ছবি নির্বাচন করুন",
        "dzHintPdf" to "শুধু পিডিএফ ফাইল সমর্থিত। ফাইল আপনার ডিভাইসের বাইরে যায় না।",
        "dzHintImg" to "JPG, PNG ও WebP ছবি সমর্থিত।",
        "errOnlyPdf" to "শুধুমাত্র পিডিএফ ফাইল নির্বাচন করুন।",
        "errOnlyImg" to "শুধুমাত্র JPG, PNG বা WebP ছবি নির্বাচন করুন।",
        "errGeneric" to "কিছু সমস্যা হয়েছে।",
        "errEncrypted" to "এই পিডিএফটি পাসওয়ার্ড-সুরক্ষিত / এনক্রিপ্টেড, তাই প্রসেস করা যাচ্ছে না।",
        "errBadPdf" to "ফাইলটি পড়া যায়নি। এটি নষ্ট হতে পারে বা বৈধ পিডিএফ নয়।",
        "reading" to "ফাইল পড়া হচ্ছে…",
        "readingN" to "পড়া হচ্ছে {n}/{total}: {name}",
        "saving" to "পিডিএফ সংরক্ষণ হচ্ছে…",
        "zipping" to "ZIP আর্কাইভ তৈরি হচ্ছে…",
        "building" to "পিডিএফ তৈরি হচ্ছে…",

        "doneTitle" to "সম্পন্ন! আপনার ফাইল প্রস্তুত",
        "dlHint" to "ফাইলটি আপনার ফোনে সংরক্ষণ করুন, অ্যাপের ভেতরেই প্রিভিউ দেখুন অথবা সরাসরি শেয়ার করুন।",
        "download" to "ডাউনলোড / সংরক্ষণ",
        "startOver" to "নতুন করে শুরু করুন",
        "changeFile" to "ফাইল পরিবর্তন",
        "fileName" to "ফাইলের নাম",
        "fileSize" to "ফাইলের সাইজ",
        "totalPages" to "মোট পৃষ্ঠা",
        "filesMerged" to "মার্জ করা ফাইল",
        "filesCreated" to "তৈরি ফাইল",
        "origSize" to "আসল সাইজ",
        "newSize" to "কমপ্রেসড সাইজ",
        "savedLabel" to "সাশ্রয়",
        "imagesCreated" to "তৈরি ছবি",
        "resolution" to "রেজোলিউশন",
        "pagesRemoved" to "মুছে ফেলা পৃষ্ঠা",
        "pagesWord" to "পৃষ্ঠা",
        "pageWord" to "পৃষ্ঠা",
        "page" to "পৃষ্ঠা",
        "remove" to "সরান",

        "addMore" to "আরও ফাইল যোগ করুন",
        "sortAZ" to "A–Z সাজান",
        "clearAll" to "সব মুছুন",
        "mergeHint" to "তীর বাটনে ফাইলগুলোর ক্রম ঠিক করুন। উপর থেকে নিচের ক্রমে পৃষ্ঠাগুলো একত্র হবে।",
        "mergeNow" to "পিডিএফ মার্জ করুন",
        "errNeed2" to "মার্জ করতে কমপক্ষে ২টি পিডিএফ ফাইল যোগ করুন।",
        "merging" to "পিডিএফ মার্জ হচ্ছে…",
        "mergingN" to "{total}টির মধ্যে {n} নম্বর ফাইল মার্জ হচ্ছে…",
        "moveUp" to "উপরে নিন",
        "moveDown" to "নিচে নিন",
        "mergeSummary" to "{files}টি ফাইল · মোট {pages} পৃষ্ঠা",

        "splitMode" to "ভাগ করার পদ্ধতি",
        "splitSingle" to "একক পৃষ্ঠা",
        "splitSingleD" to "প্রতিটি পৃষ্ঠা আলাদা পিডিএফ ফাইল হবে।",
        "splitRanges" to "কাস্টম রেঞ্জ",
        "splitRangesD" to "প্রতিটি রেঞ্জ একটি পিডিএফ হবে, যেমন 1-3, 4-5 বা 1, 3, 5-8।",
        "splitEvery" to "প্রতি N পৃষ্ঠা পরপর",
        "splitEveryD" to "নির্দিষ্ট সংখ্যক পৃষ্ঠা করে ভাগ হবে।",
        "every" to "প্রতি",
        "rangesPh" to "যেমন 1-3, 4-5",
        "splitNow" to "ভাগ করে ZIP তৈরি করুন",
        "willCreate" to "{n}টি পিডিএফ ফাইল তৈরি হবে",
        "errRangeTok" to "অবৈধ পৃষ্ঠা রেঞ্জ: \"{tok}\"",
        "errEnterRanges" to "ভাগ করার পৃষ্ঠা রেঞ্জ লিখুন।",
        "errEveryN" to "সঠিক পৃষ্ঠা সংখ্যা লিখুন (১ বা তার বেশি)।",
        "splitting" to "পিডিএফ ভাগ হচ্ছে…",
        "splittingN" to "{total}টির মধ্যে {n} নম্বর ফাইল তৈরি হচ্ছে…",

        "cmpLevel" to "কমপ্রেশনের মাত্রা",
        "cmpRec" to "প্রস্তাবিত",
        "cmpRecD" to "সাইজ ও মানের ভারসাম্য · ১৪৪ DPI, ৭৫%",
        "cmpExt" to "সর্বোচ্চ কমপ্রেশন",
        "cmpExtD" to "সবচেয়ে ছোট ফাইল; ছবির মান কিছুটা কমে · ৭২ DPI, ৫০%",
        "cmpLow" to "কম কমপ্রেশন",
        "cmpLowD" to "সর্বোচ্চ ভিজ্যুয়াল মান, সাশ্রয় কম · ২০০ DPI, ৮৫%",
        "cmpNote" to "উপরের DPI-এর চেয়ে বেশি রেজোলিউশনের পৃষ্ঠা ও ছবি ছোট করে নির্বাচিত মানে সংরক্ষণ করা হয়।",
        "compressNow" to "পিডিএফ কমপ্রেস করুন",
        "compressing" to "পিডিএফ কমপ্রেস হচ্ছে…",
        "stPending" to "অপেক্ষমাণ",
        "stCompressing" to "কমপ্রেস হচ্ছে…",
        "stDone" to "সম্পন্ন",
        "stFailed" to "ব্যর্থ",
        "savedPct" to "সাশ্রয় {pct}%",
        "optAlready" to "আগে থেকেই অপটিমাইজড",
        "cmpCustom" to "কাস্টম",
        "cmpCustomD" to "নিচে নিজের DPI ও মান লিখুন।",
        "dpiLabel" to "DPI (ছবির রেজোলিউশন)",
        "qualityLabel" to "ছবির মান (%)",
        "stripLabel" to "মেটাডেটা ও অপ্রয়োজনীয় অবজেক্ট মুছুন",
        "stripHint" to "ডকুমেন্ট তথ্য ও অব্যবহৃত অবজেক্ট সরানো হয়।",
        "errCmpValues" to "DPI অবশ্যই {dmin} থেকে {dmax} এবং ছবির মান {qmin} থেকে {qmax}-এর মধ্যে দিন।",
        "compScan" to "পৃষ্ঠা বিশ্লেষণ হচ্ছে…",
        "compressAll" to "সব পিডিএফ কমপ্রেস করুন",
        "compressAgain" to "আবার কমপ্রেস করুন",
        "dlAllZip" to "সব ZIP হিসেবে সংরক্ষণ",
        "stopBtn" to "থামান",
        "batchHint" to "যতগুলো খুশি পিডিএফ যোগ করুন। সবগুলো একটার পর একটা এই ফোনেই কমপ্রেস হবে এবং সব ফাইলে একই সেটিংস প্রযোজ্য হবে।",
        "batchSummary" to "{n}টি ফাইল · মোট {size}",
        "batchDone" to "{ok}/{total}টি সম্পন্ন · {orig} → {now} (সাশ্রয় {pct}%)",
        "batchFailed" to "{n}টি ব্যর্থ",
        "batchProgress" to "ফাইল {n}/{total}: {name}",
        "stopping" to "বর্তমান ফাইল শেষ হলে থামবে…",
        "batchStopped" to "থামানো হয়েছে। {ok}টি ফাইল সম্পন্ন।",
        "batchFinished" to "কমপ্রেশন শেষ: {ok}/{total}টি সম্পন্ন।",
        "zipBuilding" to "ZIP তৈরি হচ্ছে…",
        "noneToZip" to "এখনও কোনো কমপ্রেসড ফাইল নেই।",
        "dupSkipped" to "{n}টি পুনরাবৃত্ত ফাইল বাদ দেওয়া হয়েছে।",
        "compImages" to "{total}টির মধ্যে {n} নম্বর পৃষ্ঠা অপটিমাইজ হচ্ছে…",
        "alreadyOptimized" to "ফাইলটি আগে থেকেই ভালোভাবে অপটিমাইজড, তাই আসল ফাইলই দেওয়া হলো।",

        "addMoreImg" to "আরও ছবি যোগ করুন",
        "imgHint" to "পৃষ্ঠার ক্রম ঠিক করতে তীর বাটন ব্যবহার করুন।",
        "imgOptions" to "পৃষ্ঠার অপশন",
        "pageSize" to "পৃষ্ঠার সাইজ",
        "orientation" to "ওরিয়েন্টেশন",
        "margins" to "মার্জিন",
        "optFit" to "ছবির সমান",
        "optA4" to "A4",
        "optLetter" to "US Letter",
        "optAuto" to "অটো (ছবি অনুযায়ী)",
        "optPortrait" to "পোর্ট্রেট",
        "optLandscape" to "ল্যান্ডস্কেপ",
        "optM0" to "০ মিমি (মার্জিন নেই)",
        "optM10" to "১০ মিমি",
        "optM20" to "২০ মিমি",
        "moveLeft" to "বামে নিন",
        "moveRight" to "ডানে নিন",
        "imgSummary" to "{n}টি ছবি",
        "imgNow" to "পিডিএফে রূপান্তর করুন",
        "errNoImages" to "কমপক্ষে একটি ছবি যোগ করুন।",
        "converting" to "পিডিএফ তৈরি হচ্ছে…",
        "convertingN" to "{total}টির মধ্যে {n} নম্বর ছবি যোগ হচ্ছে…",
        "errImage" to "\"{name}\" ছবিটি পড়া যায়নি।",

        "format" to "ফরম্যাট",
        "pagesOpt" to "পৃষ্ঠা (ঐচ্ছিক)",
        "allPagesPh" to "সব পৃষ্ঠা (অথবা যেমন 1-3, 5)",
        "dpiNote" to "রেজোলিউশন বেশি হলে ছবি ধারালো হয়, কিন্তু ফাইল বড় হয় ও সময় বেশি লাগে। স্ক্রিনের জন্য ১৫০ DPI, প্রিন্টের জন্য ৩০০ DPI ভালো।",
        "p2iNow" to "ছবিতে রূপান্তর করুন (ZIP)",
        "rendering" to "পৃষ্ঠা রেন্ডার হচ্ছে…",
        "renderingN" to "{total}টির মধ্যে {n} নম্বর পৃষ্ঠা রেন্ডার হচ্ছে…",
        "errRender" to "এই পৃষ্ঠা থেকে ছবি তৈরি করা যায়নি।",

        "rotAllCW" to "সব ৯০° ডানে",
        "rotAllCCW" to "সব ৯০° বামে",
        "rotAll180" to "সব ১৮০°",
        "resetAll" to "সব রিসেট",
        "rotCW" to "৯০° ডানে ঘোরান",
        "rotCCW" to "৯০° বামে ঘোরান",
        "rotSummary" to "{total}টির মধ্যে {n}টি পৃষ্ঠা ঘোরানো হয়েছে",
        "rotNow" to "ঘোরানো পিডিএফ সংরক্ষণ করুন",
        "errNoRotation" to "আগে কমপক্ষে একটি পৃষ্ঠা ঘোরান।",

        "delLabel" to "যে পৃষ্ঠা মুছবেন",
        "extLabel" to "যে পৃষ্ঠা আলাদা করবেন",
        "delPh" to "যেমন 2, 4-6",
        "extPh" to "যেমন 1, 3, 5-7",
        "selEven" to "জোড় পৃষ্ঠা",
        "selOdd" to "বিজোড় পৃষ্ঠা",
        "selAll" to "সব নির্বাচন",
        "selClear" to "নির্বাচন মুছুন",
        "delHint" to "মুছতে চাইলে পৃষ্ঠায় ট্যাপ করুন (আবার ট্যাপ করলে বাতিল)।",
        "extHint" to "নতুন ফাইলে রাখতে চান এমন পৃষ্ঠায় ট্যাপ করুন।",
        "delNow" to "পৃষ্ঠা মুছে সংরক্ষণ করুন",
        "extNow" to "পৃষ্ঠা আলাদা করে সংরক্ষণ করুন",
        "deleted" to "মুছে ফেলা হবে",
        "delSummary" to "{n}টি পৃষ্ঠা মোছার জন্য নির্বাচিত · {left}টি থাকবে",
        "extSummary" to "{total}টির মধ্যে {n}টি পৃষ্ঠা নির্বাচিত",
        "errSelDel" to "মুছতে কমপক্ষে একটি পৃষ্ঠা নির্বাচন করুন।",
        "errSelExt" to "আলাদা করতে কমপক্ষে একটি পৃষ্ঠা নির্বাচন করুন।",
        "errDelAll" to "ডকুমেন্টের সব পৃষ্ঠা মোছা যাবে না।",
        "extOne" to "একটি একত্রিত পিডিএফ",
        "extOneD" to "নির্বাচিত সব পৃষ্ঠা একটি পিডিএফ ফাইলে।",
        "extSep" to "আলাদা আলাদা পিডিএফ (ZIP)",
        "extSepD" to "প্রতিটি নির্বাচিত পৃষ্ঠা আলাদা পিডিএফ, সব ZIP-এ।"
    )

    fun t(lang: AppLang, key: String, vars: Map<String, Any> = emptyMap()): String {
        val dict = if (lang == AppLang.BN) bnMap else enMap
        var str = dict[key] ?: enMap[key] ?: key
        for ((k, v) in vars) {
            str = str.replace("{$k}", v.toString())
        }
        return str
    }
}

/* =====================================================================
   PURE RANGE PARSER & FORMATTING HELPERS (Ported from PDFLab)
   ===================================================================== */

fun fmtBytes(bytes: Long): String {
    if (bytes < 0) return "-"
    if (bytes < 1024L) return "$bytes B"
    if (bytes < 1024L * 1024L) {
        val kb = bytes / 1024.0
        return if (bytes < 10240L) String.format(Locale.US, "%.1f KB", kb)
        else String.format(Locale.US, "%.0f KB", kb)
    }
    if (bytes < 1024L * 1024L * 1024L) {
        val mb = bytes / 1048576.0
        return if (bytes < 10485760L) String.format(Locale.US, "%.2f MB", mb)
        else String.format(Locale.US, "%.1f MB", mb)
    }
    val gb = bytes / 1073741824.0
    return String.format(Locale.US, "%.2f GB", gb)
}

fun stripExt(name: String): String {
    val idx = name.lastIndexOf('.')
    return if (idx > 0) name.substring(0, idx) else name
}

fun safeName(name: String): String {
    val cleaned = name.replace(Regex("[\\\\/:*?\"<>|\\u0000-\\u001f]+"), "_")
        .replace(Regex("\\s+"), " ")
        .trim()
    return cleaned.ifEmpty { "file" }
}

fun padNum(n: Int, width: Int): String = n.toString().padStart(width, '0')

/** Accepts Bengali digits ০-৯ in user input and normalizes to ASCII 0-9 */
fun normalizeDigits(input: String): String {
    val sb = StringBuilder(input.length)
    for (ch in input) {
        if (ch in '\u09E6'..'\u09EF') {
            sb.append((ch.code - 0x09E6).toString())
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

data class RangeParseResult(
    val groups: List<List<Int>> = emptyList(),
    val errorToken: String? = null
)

/**
 * Parse "1-3, 5, 7-9" into groups. Each comma-separated token is one group (list of 1-based pages).
 */
fun parseRanges(raw: String, maxPage: Int): RangeParseResult {
    val normalized = normalizeDigits(raw)
        .replace(Regex("\\s*[-–—]\\s*"), "-")
        .trim()
    if (normalized.isEmpty()) return RangeParseResult(groups = emptyList())
    val tokens = normalized.split(Regex("[,;\\s]+")).filter { it.isNotBlank() }
    val groups = mutableListOf<List<Int>>()
    val singleRegex = Regex("^(\\d+)$")
    val rangeRegex = Regex("^(\\d+)-(\\d+)$")

    for (tok in tokens) {
        val mSingle = singleRegex.matchEntire(tok)
        if (mSingle != null) {
            val p = mSingle.groupValues[1].toIntOrNull() ?: return RangeParseResult(errorToken = tok)
            if (p < 1 || p > maxPage) return RangeParseResult(errorToken = tok)
            groups.add(listOf(p))
            continue
        }
        val mRange = rangeRegex.matchEntire(tok)
        if (mRange != null) {
            val a = mRange.groupValues[1].toIntOrNull() ?: return RangeParseResult(errorToken = tok)
            val b = mRange.groupValues[2].toIntOrNull() ?: return RangeParseResult(errorToken = tok)
            if (a < 1 || b > maxPage || a > b) return RangeParseResult(errorToken = tok)
            groups.add((a..b).toList())
            continue
        }
        return RangeParseResult(errorToken = tok)
    }
    return RangeParseResult(groups = groups)
}

fun flattenGroups(groups: List<List<Int>>): List<Int> {
    return groups.flatten().toSortedSet().toList()
}

fun compactRanges(pages: Collection<Int>): String {
    val sorted = pages.toSortedSet().toList()
    if (sorted.isEmpty()) return ""
    val out = mutableListOf<String>()
    var i = 0
    while (i < sorted.size) {
        var j = i
        while (j + 1 < sorted.size && sorted[j + 1] == sorted[j] + 1) {
            j++
        }
        out.add(if (j > i) "${sorted[i]}-${sorted[j]}" else "${sorted[i]}")
        i = j + 1
    }
    return out.joinToString(", ")
}
