package com.senthil.AI

import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.senthil.AI.ui.ReceiptSpatialSLM
import com.senthil.AI.ui.getSanitizedReceiptBitmap
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentOcrTest {

    @Test
    fun testAllReceiptImages() {
        val expectations = mapOf(
            "user_latest_screenshot.jpg" to "₹120",
            "phonepe_user1.jpg" to "₹90",
            "phonepe_user2.jpg" to "₹400",
            "phonepe_user3.jpg" to "₹225"
        )

        val testContext = InstrumentationRegistry.getInstrumentation().context
        val devanagariRecognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
        val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        for ((assetName, expectedAmount) in expectations) {
            val inputStream = testContext.assets.open(assetName)
            val rawBmp = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val sanitized = getSanitizedReceiptBitmap(rawBmp)
            val inputImage = InputImage.fromBitmap(sanitized, 0)

            val devResult = Tasks.await(devanagariRecognizer.process(inputImage))
            val latResult = Tasks.await(latinRecognizer.process(inputImage))

            val (extracted, candidates) = ReceiptSpatialSLM.parseAmount(
                devText = devResult,
                latText = latResult,
                imageWidth = sanitized.width,
                imageHeight = sanitized.height
            )

            Log.i("TEST_OCR", "RESULT for $assetName: EXTRACTED='$extracted', EXPECTED='$expectedAmount', CANDIDATES=$candidates")
            assertEquals("Amount mismatch for $assetName", expectedAmount, extracted)
        }
    }

    @Test
    fun testTextFallbackParsing() {
        val textCases = mapOf(
            "Paid ₹1,250 to Grocery Store UTR 429182910291" to "₹1,250",
            "Received from Merchant Alpha ₹90 credited to Paytm UTR: 633586844657" to "₹90",
            "Google Pay Payment to Store ₹1,500.00" to "₹1,500",
            "Paytm Payment Successful ₹350" to "₹350",
            "Paid to Retail Partner F225 Debited from 1004" to "₹225"
        )

        for ((rawText, expected) in textCases) {
            val (extracted, _) = ReceiptSpatialSLM.parseAmountFromText(rawText)
            assertEquals("Text fallback mismatch for: $rawText", expected, extracted)
        }
    }
}
