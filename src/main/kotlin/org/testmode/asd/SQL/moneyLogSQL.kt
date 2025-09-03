package org.testmode.asd.SQL

import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.DriverManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Log_sys_addMoney(javaPlugin: JavaPlugin,target: Player , value:Int):Boolean{
    val target_name = target.name
    val target_UUID = target.uniqueId
    if (value <= 0){
        return false
    }

    val now = LocalDateTime.now()  // ← 여기서 현재 시간 가져오기
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formatted = now.format(formatter)

    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}money.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        connection.use { conn ->
            val sql = "insert into money_log (system , target_uuid , target_name , type , date, value) values (1 ,?,?,'add',?,?)"
            val pstmt = conn.prepareStatement(sql)
            pstmt.use {
                it.setString(1,target_UUID.toString())
                it.setString(2, target_name.toString())
                it.setString(3, formatted.toString())
                it.setInt(4, value)
            }
        }
    } catch (e:Exception){
        return false
    }
    return true
}
