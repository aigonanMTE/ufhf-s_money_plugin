package org.testmode.asd.setting

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.md_5.bungee.api.ChatColor
import java.io.File

object SettingsManager {
    private var settings: JsonObject? = null

    // JSON 파일 불러오기 (플러그인 실행 시 1회 실행)
    fun loadSettings(file: File) {
        if (!file.exists()) {
            throw IllegalStateException("설정 파일이 존재하지 않습니다: ${file.absolutePath}")
        }
        val jsonText = file.readText(Charsets.UTF_8)
        settings = Gson().fromJson(jsonText, JsonObject::class.java)
    }

    // 값 가져오기
    fun getSettingValue(key: String): Any? {
        val json = settings ?: throw IllegalStateException("Settings not loaded. Call loadSettings first.")

        val parts = key.split(".")
        var current: JsonElement = json
        for (part in parts) {
            if (current.isJsonObject) {
                current = current.asJsonObject[part] ?: return null
            } else {
                return null
            }
        }

        return when {
            current.isJsonPrimitive -> {
                val primitive = current.asJsonPrimitive
                when {
                    primitive.isBoolean -> primitive.asBoolean
                    primitive.isNumber -> primitive.asNumber
                    primitive.isString -> applyColorPlaceholders(primitive.asString)
                    else -> primitive.toString()
                }
            }
            current.isJsonArray -> current.asJsonArray.map {
                if (it.isJsonPrimitive && it.asJsonPrimitive.isString) {
                    applyColorPlaceholders(it.asString)
                } else it.toString()
            }
            current.isJsonObject -> current.asJsonObject.toString()
            else -> null
        }
    }

    // 색상 치환 함수
    private fun applyColorPlaceholders(text: String): String {
        val regex = "\\{coler\\.([a-zA-Z_]+)\\}".toRegex()
        return regex.replace(text) { match ->
            val colorName = match.groupValues[1].uppercase()
            try {
                ChatColor.valueOf(colorName).toString()
            } catch (e: IllegalArgumentException) {
                match.value // 못 찾으면 원래 문자열 유지
            }
        }
    }
}