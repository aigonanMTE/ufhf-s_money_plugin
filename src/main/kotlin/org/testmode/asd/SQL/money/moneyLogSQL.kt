package org.testmode.asd.SQL.money

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.DriverManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

val now: LocalDateTime = LocalDateTime.now()
val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
val formatted: String = now.format(formatter)

fun Log_user_send(
    javaPlugin: JavaPlugin,
    target_uuid: String,
    sender_uuid: String,
    value: Int
): Boolean {
    val targetTUUid = UUID.fromString(target_uuid)
    val target = Bukkit.getPlayer(targetTUUid)

    val senderTUUid = UUID.fromString(sender_uuid)
    val sender = Bukkit.getPlayer(senderTUUid)

    // 날짜 포맷팅
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formatted = LocalDateTime.now().format(formatter)

    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}money.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")

        connection.use { conn ->
            val sql = """
                INSERT INTO money_log 
                (system, target_uuid, target_name, type, sender_uuid, sender_name, date, value) 
                VALUES ('user', ?, ?, 'deposit', ?, ?, ?, ?)
            """.trimIndent()

            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, target_uuid)
                pstmt.setString(2, target?.name ?: "Unknown")
                pstmt.setString(3, sender_uuid)
                pstmt.setString(4, sender?.name ?: "Unknown")
                pstmt.setString(5, formatted)
                pstmt.setInt(6, value)
                pstmt.executeUpdate()
            }
        }
    } catch (e: Exception) {
        javaPlugin.logger.warning("[Log_user_send] 유저 송금 로그 기록 중 오류 발생 \n $e")
        return false
    }
    return true
}


fun Log_sys_adduser(javaPlugin: JavaPlugin, target_uuid: String):Boolean{
    val uuid = UUID.fromString(target_uuid)
    val target = Bukkit.getPlayer(uuid) // UUID 기반으로 검색
    val targetName = target?.name ?: "unknown" // 오프라인이면 null → "unknown"
    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}money.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        connection.use { conn ->
            val sql = """
                INSERT INTO money_log 
                (system, target_uuid, target_name, type, date, value) 
                VALUES ('system', ?, ?, 'add_user', ?, ?)
            """.trimIndent()

            val pstmt = conn.prepareStatement(sql)
            pstmt.use {
                it.setString(1, target_uuid)
                it.setString(2, targetName)
                it.setString(3, formatted)
                it.setInt(4, 0)
                it.executeUpdate()
            }
        }
    } catch (e: Exception) {
        javaPlugin.logger.warning("[Log_sys_adduser] 오류 발생: $e")
        return false
    }
    return true
}

fun Log_sys_addMoney(javaPlugin: JavaPlugin, target_uuid: String, value: Int): Boolean {
    val uuid = UUID.fromString(target_uuid)
    val target = Bukkit.getPlayer(uuid) // UUID 기반으로 검색
    val targetName = target?.name ?: "unknown" // 오프라인이면 null → "unknown"

    if (value <= 0) {
        return false
    }
    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}money.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        connection.use { conn ->
            val sql = """
                INSERT INTO money_log 
                (system, target_uuid, target_name, type, date, value) 
                VALUES ('system', ?, ?, 'money_add', ?, ?)
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