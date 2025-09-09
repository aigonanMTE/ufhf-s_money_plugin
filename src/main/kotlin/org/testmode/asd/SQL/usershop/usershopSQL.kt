package org.testmode.asd.SQL.usershop

import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.DriverManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val now: LocalDateTime = LocalDateTime.now()
val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
val formatted: String = now.format(formatter)

fun uploaditem(javaPlugin: JavaPlugin,seller:Player,item:String,value:Int):Boolean{
    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}UserShop.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        connection.use { conn ->
            val sql = "insert into shop_item_list (item_data , seller_uuid , seller_name , value , upload_date) values (? ,? ,? ,? ,?)"
            val pstmt = conn.prepareStatement(sql)
            pstmt.use {
                it.setString(1, item)
                it.setString(2,seller.uniqueId.toString())
                it.setString(3, seller.name)
                it.setInt(4,value)
                it.setString(5, formatted)
                it.executeUpdate()
            }
        }
    } catch (e:Exception){
        javaPlugin.logger.warning("[uploaditem] 유저 상점 아이템 추가중 오류 발생 \n $e")
        return false
    }
    return true
}