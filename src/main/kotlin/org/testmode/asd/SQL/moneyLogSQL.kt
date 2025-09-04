package org.testmode.asd.SQL

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.DriverManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// TODO:돈 로그 남기는 함수 만드셈

fun Log_sys_addMoney(javaPlugin: JavaPlugin, target_uuid: String, value: Int): Boolean {
    val uuid = UUID.fromString(target_uuid)
    val target = Bukkit.getPlayer(uuid) // UUID 기반으로 검색
    val targetName = target?.name ?: "unknown" // 오프라인이면 null → "unknown"

    if (value <= 0) {
        return false
    }

    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formatted = now.format(formatter)

    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}money.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        connection.use { conn ->
            val sql = """
                INSERT INTO money_log 
                (system, target_uuid, target_name, type, date, value) 
                VALUES ('system', ?, ?, 'add', ?, ?)
            """.trimIndent()

            val pstmt = conn.prepareStatement(sql)
            pstmt.use {
                it.setString(1, target_uuid)
                it.setString(2, targetName)
                it.setString(3, formatted)
                it.setInt(4, value)
                it.executeUpdate()
            }
        }
    } catch (e: Exception) {
        javaPlugin.logger.warning("[Log_sys_addMoney] 오류 발생: $e")
        return false
    }
    return true
}