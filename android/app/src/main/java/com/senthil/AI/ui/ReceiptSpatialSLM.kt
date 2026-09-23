package com.senthil.AI.ui

import android.graphics.Rect
import com.google.mlkit.vision.text.Text

/**
 * ReceiptSpatialSLM: Lightweight On-Device Spatial Layout & Language Model
 * for Payment Screenshot Forensics & Accurate Amount Extraction.
 *
 * Solves:
 * 1. Rupee symbol glyph misidentifications in ML Kit (₹ transcribed as F, 7, ?, r, Rs, or dropped).
 * 2. Spatial layout correlation (horizontal alignment between recipient/account and amount column).
 * 3. Dual-engine consensus (Devanagari + Latin ML Kit OCR models).
 * 4. Smart exclusion filtering (timestamps, dates, years, battery %, account numbers, UTRs).
 */

data class SpatialCandidate(
    val amountStr: String,
    val numericVal: Double,
    val boundingBox: Rect?,
    val source: String,
    var score: Int = 0
)

object ReceiptSpatialSLM {

    fun parseAmount(
        devText: Text?,
        latText: Text?,
        imageWidth: Int,
        imageHeight: Int
    ): Pair<String, List<String>> {
        val candidates = mutableListOf<SpatialCandidate>()

        val allBlocks = mutableListOf<Pair<Text.TextBlock, String>>()
        devText?.textBlocks?.forEach { allBlocks.add(Pair(it, "devanagari")) }
        latText?.textBlocks?.forEach { allBlocks.add(Pair(it, "latin")) }

        val fullCombinedText = ((devText?.text ?: "") + "\n" + (latText?.text ?: ""))

        // 1. Identify non-amount patterns to exclude (dates, times, masked accounts, UTRs, etc.)
        val timeRegex = Regex("""\b\d{1,2}:\d{2}(?::\d{2})?\b""")
        val timeNumbers = mutableSetOf<Int>()
        timeRegex.findAll(fullCombinedText).forEach { m ->
            m.value.split(":").forEach { it.trim().toIntOrNull()?.let { num -> timeNumbers.add(num) } }
        }

        val yearRegex = Regex("""\b(20[1-3][0-9])\b""")
        val years = yearRegex.findAll(fullCombinedText).mapNotNull { it.value.toIntOrNull() }.toSet()

        val dateDayRegex = Regex("""\b(\d{1,2})\s*(?:st|nd|rd|th)?\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)""", RegexOption.IGNORE_CASE)
        val dateDays = dateDayRegex.findAll(fullCombinedText).mapNotNull { it.groupValues[1].toIntOrNull() }.toMutableSet()

        val maskedAccRegex = Regex("""(?:[X*x•]{2,}|\.{3,}|A/c\s*|Account\s*|debited\s+from\s+|credited\s+to\s+)\s*(\d{2,6})\b""", RegexOption.IGNORE_CASE)
        val maskedAccLastDigits = maskedAccRegex.findAll(fullCombinedText).mapNotNull { it.groupValues[1] }.toMutableSet()

        val phoneRegex = Regex("""(?:\+?91[\s\.\-•*]*|\b)[0-9•*xX]{4,15}([0-9]{3,5})\b""")
        phoneRegex.findAll(fullCombinedText).forEach { maskedAccLastDigits.add(it.groupValues[1]) }

        val utrRegex = Regex("""\b\d{12}\b""")
        val utrs = utrRegex.findAll(fullCombinedText).map { it.value }.toSet()

        fun isExcluded(numStr: String): Boolean {
            val clean = numStr.replace(",", "").replace(" ", "").trim()
            val num = clean.toDoubleOrNull() ?: return true
            val intVal = num.toInt()

            if (num <= 0 || num > 10000000) return true
            if (clean.startsWith("0") && !clean.contains(".")) return true
            if (utrs.contains(clean)) return true
            if (years.contains(intVal)) return true
            if (maskedAccLastDigits.contains(intVal.toString()) || maskedAccLastDigits.contains(clean)) return true
            if (clean == "91" || clean == "+91") return true
            if (timeNumbers.contains(intVal)) return true
            if (dateDays.contains(intVal)) return true
            return false
        }

        fun extractTokens(rawToken: String, rect: Rect?, source: String) {
            val token = rawToken.trim()
            if (token.isBlank()) return

            // Pattern 1: Direct match with currency symbol (₹, ?, Rs, INR, $, F)
            val symMatch = Regex("""(?:[₹?]|Rs\.?|INR|[$€£]|[Ff])\s*([0-9]{1,6}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?)""").find(token)
            if (symMatch != null) {
                val numStr = symMatch.groupValues[1].replace(",", "")
                if (!isExcluded(numStr)) {
                    val dbl = numStr.toDoubleOrNull()
                    if (dbl != null) {
                        candidates.add(SpatialCandidate(numStr, dbl, rect, "$source-sym", score = 300))
                    }
                }
            }

            // Pattern 2: Standalone number token
            val numMatch = Regex("""^([0-9]{1,6}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?)$""").find(token)
            if (numMatch != null) {
                val numStr = numMatch.groupValues[1].replace(",", "")
                if (!isExcluded(numStr)) {
                    val dbl = numStr.toDoubleOrNull()
                    if (dbl != null) {
                        candidates.add(SpatialCandidate(numStr, dbl, rect, "$source-num", score = 150))
                    }
                }
            }

            // Pattern 3: OCR '7' misread of Indian Rupee symbol (e.g. 7400 -> 400, 790 -> 90)
            if (token.startsWith("7") && token.length in 3..6 && token.all { it.isDigit() }) {
                val stripped = token.substring(1)
                if (!isExcluded(stripped)) {
                    val dbl = stripped.toDoubleOrNull()
                    if (dbl != null) {
                        candidates.add(SpatialCandidate(stripped, dbl, rect, "$source-7strip", score = 250))
                    }
                }
            }
        }

        // Process all lines and elements
        for ((block, source) in allBlocks) {
            for (line in block.lines) {
                val lineText = line.text.trim()
                extractTokens(lineText, line.boundingBox, source)
                for (elem in line.elements) {
                    extractTokens(elem.text, elem.boundingBox, source)
                }
            }
        }

        // Spatial anchor alignment: "Paid to", "Received from", "Debited from", "Credited to"
        val anchorYCoordinates = mutableListOf<Int>()
        for ((block, _) in allBlocks) {
            val bt = block.text.lowercase()
            if (bt.contains("paid to") || bt.contains("received from") || bt.contains("payment to") ||
                bt.contains("debited from") || bt.contains("credited to") || bt.contains("transfer to")
            ) {
                block.boundingBox?.let { anchorYCoordinates.add(it.centerY()) }
            }
        }

        val rightMarginX = imageWidth * 0.65f

        for (cand in candidates) {
            val rect = cand.boundingBox
            if (rect != null) {
                // Check if candidate is in the dedicated right-hand column
                if (rect.left >= rightMarginX) {
                    cand.score += 350
                }

                // Check vertical alignment with any anchor row
                val matchesAnchor = anchorYCoordinates.any { anchorY ->
                    Math.abs(rect.centerY() - anchorY) < (imageHeight * 0.08f)
                }
                if (matchesAnchor) {
                    cand.score += 400
                }
            }

            if (cand.numericVal in 10.0..500000.0) {
                cand.score += 100
            }
        }

        // Group by numeric value to calculate consensus score
        val groupedScores = mutableMapOf<Double, Int>()
        val valueToFormatted = mutableMapOf<Double, String>()

        for (cand in candidates) {
            val currentScore = groupedScores.getOrDefault(cand.numericVal, 0)
            groupedScores[cand.numericVal] = currentScore + cand.score
            if (!valueToFormatted.containsKey(cand.numericVal)) {
                valueToFormatted[cand.numericVal] = formatAmount(cand.amountStr)
            }
        }

        // Reward repeating numbers across receipt rows
        for ((numVal, _) in valueToFormatted) {
            val occurrences = candidates.count { it.numericVal == numVal }
            if (occurrences >= 2) {
                groupedScores[numVal] = groupedScores.getValue(numVal) + (occurrences * 250)
            }
        }

        val sorted = groupedScores.entries.sortedByDescending { it.value }
        if (sorted.isEmpty()) {
            return parseAmountFromText(fullCombinedText)
        }

        val bestVal = sorted.first().key
        val bestFormatted = valueToFormatted[bestVal] ?: "₹${bestVal.toInt()}"
        val allFormatted = sorted.mapNotNull { valueToFormatted[it.key] }.distinct()

        return Pair(bestFormatted, allFormatted)
    }

    /**
     * Fallback text-only SLM parser when spatial bounding boxes are not available.
     */
    fun parseAmountFromText(cleanText: String): Pair<String, List<String>> {
        val lines = cleanText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val timeRegex = Regex("""\b\d{1,2}:\d{2}(?::\d{2})?\b""")
        val timeNumbers = mutableSetOf<Int>()
        timeRegex.findAll(cleanText).forEach { m ->
            m.value.split(":").forEach { it.trim().toIntOrNull()?.let { num -> timeNumbers.add(num) } }
        }

        val yearRegex = Regex("""\b(20[1-3][0-9])\b""")
        val years = yearRegex.findAll(cleanText).mapNotNull { it.value.toIntOrNull() }.toSet()

        val dateDayRegex = Regex("""\b(\d{1,2})\s*(?:st|nd|rd|th)?\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)""", RegexOption.IGNORE_CASE)
        val dateDays = dateDayRegex.findAll(cleanText).mapNotNull { it.groupValues[1].toIntOrNull() }.toMutableSet()

        val maskedAccRegex = Regex("""(?:[X*x•]{2,}|\.{3,}|A/c\s*|Account\s*|debited\s+from\s+|credited\s+to\s+)\s*(\d{2,6})\b""", RegexOption.IGNORE_CASE)
        val maskedAccLastDigits = maskedAccRegex.findAll(cleanText).mapNotNull { it.groupValues[1] }.toMutableSet()

        val phoneRegex = Regex("""(?:\+?91[\s\.\-•*]*|\b)[0-9•*xX]{4,15}([0-9]{3,5})\b""")
        phoneRegex.findAll(cleanText).forEach { maskedAccLastDigits.add(it.groupValues[1]) }

        val utrRegex = Regex("""\b\d{12}\b""")
        val utrs = utrRegex.findAll(cleanText).map { it.value }.toSet()

        fun isExcluded(numStr: String): Boolean {
            val clean = numStr.replace(",", "").replace(" ", "").trim()
            val num = clean.toDoubleOrNull() ?: return true
            val intVal = num.toInt()

            if (num <= 0 || num > 10000000) return true
            if (clean.startsWith("0") && !clean.contains(".")) return true
            if (utrs.contains(clean)) return true
            if (years.contains(intVal)) return true
            if (maskedAccLastDigits.contains(intVal.toString()) || maskedAccLastDigits.contains(clean)) return true
            if (clean == "91" || clean == "+91") return true
            if (timeNumbers.contains(intVal)) return true
            if (dateDays.contains(intVal)) return true
            return false
        }

        val candidates = mutableMapOf<Double, Int>()
        val valToFmt = mutableMapOf<Double, String>()

        // 1. Explicit symbol match (supporting ₹, ?, Rs, INR, $, €, £, and OCR 'F'/'f' misread of ₹)
        val explicitRegex = Regex("""(?<![A-Za-z0-9])(?:[₹?]|Rs\.?|INR|[$€£]|[Ff])\s*([0-9]{1,6}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?)(?![A-Za-z0-9])""", RegexOption.IGNORE_CASE)
        explicitRegex.findAll(cleanText).forEach { m ->
            val numStr = m.groupValues[1].replace(",", "")
            if (!isExcluded(numStr)) {
                val dbl = numStr.toDoubleOrNull()
                if (dbl != null) {
                    candidates[dbl] = candidates.getOrDefault(dbl, 0) + 300
                    valToFmt[dbl] = formatAmount(numStr)
                }
            }
        }

        // 2. OCR '7' misread of Indian Rupee symbol (e.g. 7400 -> 400, 790 -> 90)
        val sevenRegex = Regex("""(?<![A-Za-z0-9])7([0-9]{2,5})(?![A-Za-z0-9])""")
        sevenRegex.findAll(cleanText).forEach { m ->
            val stripped = m.groupValues[1]
            if (!isExcluded(stripped)) {
                val dbl = stripped.toDoubleOrNull()
                if (dbl != null) {
                    candidates[dbl] = candidates.getOrDefault(dbl, 0) + 200
                    if (!valToFmt.containsKey(dbl)) valToFmt[dbl] = formatAmount(stripped)
                }
            }
        }

        // 3. Proximity to action keywords
        val actionKeywords = listOf("received from", "paid to", "payment to", "transfer to", "sent to", "debited from", "credited to", "amount", "total")
        for (i in lines.indices) {
            val ll = lines[i].lowercase()
            if (actionKeywords.any { ll.contains(it) }) {
                for (j in i..minOf(i + 4, lines.size - 1)) {
                    val nums = Regex("""(?<![A-Za-z0-9])([0-9]{1,6}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?)(?![A-Za-z0-9])""").findAll(lines[j])
                    for (nm in nums) {
                        val numStr = nm.groupValues[1].replace(",", "")
                        if (!isExcluded(numStr)) {
                            val dbl = numStr.toDoubleOrNull()
                            if (dbl != null) {
                                candidates[dbl] = candidates.getOrDefault(dbl, 0) + 100
                                if (!valToFmt.containsKey(dbl)) valToFmt[dbl] = formatAmount(numStr)
                            }
                        }
                    }
                }
            }
        }

        // 4. Standalone decimal numbers (e.g. 1500.00, 5000.00)
        val decimalRegex = Regex("""(?<![A-Za-z0-9])([0-9]{1,6}(?:,[0-9]{3})*\.[0-9]{2})(?![A-Za-z0-9])""")
        decimalRegex.findAll(cleanText).forEach { m ->
            val numStr = m.groupValues[1].replace(",", "")
            if (!isExcluded(numStr)) {
                val dbl = numStr.toDoubleOrNull()
                if (dbl != null) {
                    candidates[dbl] = candidates.getOrDefault(dbl, 0) + 150
                    if (!valToFmt.containsKey(dbl)) valToFmt[dbl] = formatAmount(numStr)
                }
            }
        }

        if (candidates.isEmpty()) {
            return Pair("Amount Not Detected", emptyList())
        }

        val sorted = candidates.entries.sortedByDescending { it.value }
        val bestVal = sorted.first().key
        val bestFormatted = valToFmt[bestVal] ?: "₹${bestVal.toInt()}"
        val allFormatted = sorted.mapNotNull { valToFmt[it.key] }.distinct()

        return Pair(bestFormatted, allFormatted)
    }

    fun formatAmount(raw: String): String {
        val clean = raw.replace(" ", "").trim()
        val parts = clean.split(".")
        val intPart = parts[0].replace(",", "")
        val decPart = if (parts.size > 1 && parts[1] != "00" && parts[1] != "0") ".${parts[1].padEnd(2, '0').take(2)}" else ""

        val formattedInt = if (intPart.length > 3) {
            val lastThree = intPart.takeLast(3)
            val rest = intPart.dropLast(3)
            val groupedRest = rest.reversed().chunked(2).joinToString(",").reversed()
            if (groupedRest.isNotEmpty()) "$groupedRest,$lastThree" else lastThree
        } else {
            intPart
        }

        return "₹$formattedInt$decPart"
    }
}
