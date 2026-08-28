package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.util.Locale

object ChineseRomHelper {

    enum class Manufacturer {
        XIAOMI, OPPO, VIVO, HUAWEI, HONOR, SAMSUNG, ONEPLUS, OTHER
    }

    fun getManufacturer(): Manufacturer {
        val brand = Build.MANUFACTURER.lowercase(Locale.getDefault())
        val brand2 = Build.BRAND.lowercase(Locale.getDefault())
        return when {
            brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") ||
            brand2.contains("xiaomi") || brand2.contains("redmi") || brand2.contains("poco") -> Manufacturer.XIAOMI
            brand.contains("oppo") || brand.contains("realme") ||
            brand2.contains("oppo") || brand2.contains("realme") -> Manufacturer.OPPO
            brand.contains("vivo") || brand.contains("iqoo") ||
            brand2.contains("vivo") || brand2.contains("iqoo") -> Manufacturer.VIVO
            brand.contains("huawei") || brand2.contains("huawei") -> Manufacturer.HUAWEI
            brand.contains("honor") || brand2.contains("honor") -> Manufacturer.HONOR
            brand.contains("samsung") || brand2.contains("samsung") -> Manufacturer.SAMSUNG
            brand.contains("oneplus") || brand2.contains("oneplus") -> Manufacturer.ONEPLUS
            else -> Manufacturer.OTHER
        }
    }

    fun isChineseRom(): Boolean {
        val m = getManufacturer()
        return m == Manufacturer.XIAOMI || m == Manufacturer.OPPO || m == Manufacturer.VIVO ||
               m == Manufacturer.HUAWEI || m == Manufacturer.HONOR || m == Manufacturer.ONEPLUS
    }

    fun getManufacturerDisplayName(): String {
        return when (getManufacturer()) {
            Manufacturer.XIAOMI -> "Xiaomi / Redmi / POCO (MIUI/HyperOS)"
            Manufacturer.OPPO -> "OPPO / Realme (ColorOS)"
            Manufacturer.VIVO -> "Vivo / iQOO (OriginOS/Funtouch)"
            Manufacturer.HUAWEI -> "Huawei (HarmonyOS/EMUI)"
            Manufacturer.HONOR -> "Honor (MagicOS)"
            Manufacturer.SAMSUNG -> "Samsung (One UI)"
            Manufacturer.ONEPLUS -> "OnePlus (OxygenOS/ColorOS)"
            Manufacturer.OTHER -> Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        }
    }

    fun getAutostartInstructions(): String {
        return when (getManufacturer()) {
            Manufacturer.XIAOMI -> "1. Open Security App -> Manage Apps -> Permissions -> Autostart\n2. Enable 'Easy Scroll'\n3. Battery Saver: Set to 'No Restrictions'"
            Manufacturer.OPPO -> "1. Settings -> App Management -> Auto-launch apps\n2. Turn ON toggle for Easy Scroll\n3. Allow Background Activity"
            Manufacturer.VIVO -> "1. Settings -> Battery -> High background power consumption\n2. Allow 'Easy Scroll'\n3. Enable Autostart in i Manager"
            Manufacturer.HUAWEI -> "1. Settings -> Battery -> App launch\n2. Find Easy Scroll, disable 'Manage automatically', enable 'Auto-launch', 'Secondary launch', 'Run in background'"
            Manufacturer.HONOR -> "1. Settings -> Apps -> App launch -> Set Easy Scroll to Manual and allow all permissions"
            Manufacturer.SAMSUNG -> "1. Settings -> Battery & device care -> Battery -> Background usage limits -> Add Easy Scroll to 'Never sleeping apps'"
            else -> "Ensure Easy Scroll is allowed to run in the background and not killed by system battery optimization."
        }
    }

    fun openAutostartSettings(context: Context): Boolean {
        val intents = mutableListOf<Intent>()

        // Xiaomi / Redmi / POCO
        intents.add(
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            )
        )
        intents.add(
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.powercenter.PowerSettings"
                )
            )
        )

        // OPPO / Realme
        intents.add(
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            )
        )
        intents.add(
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                )
            )
        )
        intents.add(
            Intent().setComponent(
                ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity"
                )
            )
        )

        // Vivo / iQOO
        intents.add(
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
            )
        )
        intents.add(
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                )
            )
        )
        intents.add(
            Intent().setComponent(
                ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            )
        )

        // Huawei / Honor
        intents.add(
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
            )
        )
        intents.add(
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
            )
        )

        // Try device-specific intents
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {
            }
        }

        // Fallback: App Details Settings
        return try {
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
