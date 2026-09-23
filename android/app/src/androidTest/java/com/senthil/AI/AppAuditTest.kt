package com.senthil.AI

import android.content.pm.PackageManager
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.senthil.AI.ui.AppAuditSLM
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppAuditTest {

    @Test
    fun testAppAuditClassificationsOnDevice() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        var testedSystemCount = 0
        var testedPlayStoreCount = 0
        var testedSideloadCount = 0

        for (pkg in packages) {
            val appInfo = pkg.applicationInfo ?: continue
            val pkgName = pkg.packageName ?: continue
            val appName = appInfo.loadLabel(pm).toString()

            val eval = AppAuditSLM.evaluateAppSecurity(pm, pkg, appName)

            // 1. Verify that NO system firmware application is ever marked as High or Critical danger
            if (eval.isSystemApp) {
                testedSystemCount++
                assertEquals(
                    "System app $pkgName should be classified as SYSTEM_FIRMWARE",
                    AppAuditSLM.AppOrigin.SYSTEM_FIRMWARE,
                    eval.origin
                )
                assertFalse("System app $pkgName must not be marked third-party", eval.isThirdParty)
                assertTrue("System app $pkgName must be marked isSystemApp", eval.isSystemApp)
                assertTrue(
                    "System app $pkgName must have Safe or Low risk, got ${eval.riskLevel} (${eval.riskScore}%)",
                    eval.riskLevel in listOf("Safe", "Low")
                )
            }

            // 2. Verify specific known sideloaded packages on device
            if (pkgName in listOf("com.example.toollink", "com.medi.care")) {
                testedSideloadCount++
                assertEquals(
                    "Sideloaded package $pkgName must be SIDELOAD_THIRD_PARTY, not Play Store",
                    AppAuditSLM.AppOrigin.SIDELOAD_THIRD_PARTY,
                    eval.origin
                )
                assertTrue("Sideloaded package $pkgName must have isThirdParty = true", eval.isThirdParty)
                assertFalse("Sideloaded package $pkgName must have isSystemApp = false", eval.isSystemApp)
            }

            // 3. Verify specific known Play Store verified apps on device
            if (pkgName in listOf("com.truecaller", "com.zomato.delivery", "in.startv.hotstar", "com.grofers.customerapp")) {
                testedPlayStoreCount++
                assertEquals(
                    "Official app $pkgName should be classified as PLAY_STORE",
                    AppAuditSLM.AppOrigin.PLAY_STORE,
                    eval.origin
                )
                assertFalse("Official Play Store app $pkgName must not be marked third-party", eval.isThirdParty)
                assertFalse("Official app $pkgName must not be marked system", eval.isSystemApp)
                assertEquals(
                    "Verified safe app $pkgName should be scored as Safe",
                    "Safe",
                    eval.riskLevel
                )
            }
        }

        Log.i("APP_AUDIT_TEST", "Verified on-device: $testedSystemCount system apps, $testedPlayStoreCount Play Store apps, $testedSideloadCount sideloaded apps")
        assertTrue("Expected to evaluate at least 10 system apps", testedSystemCount >= 10)
    }
}
