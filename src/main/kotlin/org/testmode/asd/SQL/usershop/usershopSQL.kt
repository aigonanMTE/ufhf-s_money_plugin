package org.testmode.asd.SQL.usershop

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.testmode.asd.setting.SettingsManager
import java.io.File
import java.sql.DriverManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val now: LocalDateTime = LocalDateTime.now()
val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
val formatted: String = now.format(formatter)
private val cycle = SettingsManager.getSettingValue("userShop.Item_return_cycle").toString().toIntOrNull() ?: throw IllegalStateException("Item_return_cycle값은 숫자 여야 합니다.")
val expireAt = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * cycle)

fun uploaditem(javaPlugin: JavaPlugin,seller:Player,item:String,value:Int):Boolean{
    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}UserShop.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        connection.use { conn ->
            val sql = "insert into shop_item_list (item_data , seller_uuid , seller_name , value , upload_date, expiration_at) values (? ,? ,? ,? ,? ,?)"
            val pstmt = conn.prepareStatement(sql)
            pstmt.use {
                it.setString(1, item)
                it.setString(2,seller.uniqueId.toString())
                it.setString(3, seller.name)
                it.setInt(4,value)
                it.setString(5, formatted)
                it.setLong(6,expireAt)
                it.executeUpdate()
            }
        }
    } catch (e:Exception){
        javaPlugin.logger.warning("[uploaditem] 유저 상점 아이템 추가중 오류 발생 \n $e")
        return false
    }
    return true
}

fun getitemlist(javaPlugin: JavaPlugin, page:Int ,value: Int): List<Map<String, Any>> {
    val resultList = mutableListOf<Map<String, Any>>()

    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}UserShop.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        connection.use { conn ->
            val sql = """
                SELECT *
                FROM shop_item_list
                ORDER BY id DESC
                LIMIT ? OFFSET ?;
            """.trimIndent()

            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setInt(1, value)
                pstmt.setInt(2,(page-1)*value)

                pstmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val row = mapOf(
                            "item_data" to rs.getString("item_data"),
                            "seller_uuid" to rs.getString("seller_uuid"),
                            "seller_name" to rs.getString("seller_name"),
                            "value" to rs.getInt("value"),
                            "upload_date" to rs.getString("upload_date"),
                            "id" to rs.getInt("id")
                        )
                        resultList.add(row)
                    }
                }
            }
        }
    } catch (e: Exception) {
        javaPlugin.logger.warning("[getitemlist] 아이템 리스트 가져오기 중 오류 발생 \n $e")
    }
    return resultList
}

fun sellItem(javaPlugin: JavaPlugin, id:Int):Boolean{
    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}UserShop.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        connection.use { conn ->
            val sql = "delete from shop_item_list where id =?;"
            val pstmt = conn.prepareStatement(sql)
            pstmt.use {
                it.setInt(1, id)
                it.executeUpdate()
            }
        }
    }catch (e:Exception){
        javaPlugin.logger.warning("[sellItem] 아이템 상점에서 지우기중 오류 발생 item_id : $id \n$e")
        return false
    }
    return true
}

fun getValueOfUserItem(javaPlugin: JavaPlugin, player: Player): Int? {
    return try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}UserShop.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")

        connection.use { conn ->
            val sql = "SELECT COUNT(*) AS cnt FROM shop_item_list WHERE seller_uuid = ?;"
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, player.uniqueId.toString())

                pstmt.executeQuery().use { rs ->
                    return if (rs.next()) {
                        rs.getInt("cnt") // 유저가 올린 아이템 개수 반환
                    } else {
                        0 // 결과가 없으면 0
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        javaPlugin.logger.warning("[getValueOfUserItem] 유저가 상점에 올린 아이템 겟수 가저오기 중 오류 발생 \n$e")
        null// 오류 발생 시 null 반환
    }
}

fun delite_after_expiration_at_days_item(javaPlugin: JavaPlugin):Boolean{
    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}UserShop.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        connection.use {
            conn ->
            val sql = "delete from shop_item_list where expiration_at < ?"
            val pstmt = conn.prepareStatement(sql)
            pstmt.use {
                it.setLong(1, System.currentTimeMillis())
                it.executeUpdate()
            }
        }
    }catch (e:Exception){
        e.printStackTrace()
        javaPlugin.logger.warning("[delite_after_expiration_at_days_item] 유저상점 안팔린 아이템 삭제 처리 도중 오류 발생 \n$e")
        return false
    }
    return true
}