package org.testmode.asd.SQL.usershop

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.testmode.asd.commands.shop.itemStackFromBase64
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

fun delete_after_expiration_at_days_item(javaPlugin: JavaPlugin): Boolean {
    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}UserShop.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")

        val expiredItems = mutableListOf<Map<String, Any>>()
        javaPlugin.logger.info("유저상점을 청소중입니다... 앞으로 2시간 후에 다시 청소합니다")

        connection.use { conn ->
            // ✅ 먼저 만료 아이템 SELECT
            val selectSql = "SELECT id, item_data, seller_uuid, expiration_at FROM shop_item_list WHERE expiration_at < ?"
            conn.prepareStatement(selectSql).use { pstmt ->
                pstmt.setLong(1, System.currentTimeMillis())
                pstmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        expiredItems.add(
                            mapOf(
                                "id" to rs.getInt("id"),
                                "item_data" to rs.getString("item_data"),
                                "seller_uuid" to rs.getString("seller_uuid"),
                                "expiration_at" to rs.getLong("expiration_at")
                            )
                        )
                    }
                }
            }
            javaPlugin.logger.info(System.currentTimeMillis().toString())
            if (expiredItems.isEmpty()) {
                javaPlugin.logger.info("만료된 아이템이 없습니다.")
            } else {
                javaPlugin.logger.info("만료된 아이템 ${expiredItems.size}개 발견")
//                expiredItems.forEach {
//                    javaPlugin.logger.info("ID: ${it["id"]}, 판매자: ${it["seller_uuid"]}")
//                }

                //만료된 아이템들 not_selling_items 테이블로 옮기기
                val addsql = "insert INTO not_selling_items values (?,?,?)"
                conn.prepareStatement(addsql).use { pstmt->
                    expiredItems.forEach{
                        pstmt.setString(1,it["seller_uuid"].toString())//user_uuid
                        pstmt.setString(2,it["item_data"].toString())//item_data
                        pstmt.setLong(3,it["expiration_at"].toString().toLong())//만료일
                        pstmt.executeUpdate()
                    }
                }

                // ✅ 옮기기 후 DELETE 실행
                val deleteSql = "DELETE FROM shop_item_list WHERE expiration_at < ?"
                conn.prepareStatement(deleteSql).use { pstmt ->
                    pstmt.setLong(1, System.currentTimeMillis())
                    val deletedCount = pstmt.executeUpdate()
                    javaPlugin.logger.info("만료된 아이템 ${deletedCount}개 not_selling_items 테이블로 이동 완료")
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        javaPlugin.logger.warning("[delete_after_expiration_at_days_item] 유저상점 만료 아이템 삭제 처리 도중 오류 발생 \n$e")
        return false
    }
    return true
}

fun find_and_delete_expired_items(javaPlugin: JavaPlugin, userUuid: String): List<ItemStack> {
    val items = mutableListOf<ItemStack>()
    val dleitems = mutableListOf<String>()

    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}UserShop.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")

        connection.use { conn ->
            // 📌 1. 아이템 조회
            val sql = "SELECT item_data FROM not_selling_items WHERE user_uuid=?"
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, userUuid)
                pstmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val itemData = rs.getString("item_data")
                        dleitems.add(itemData)
                        val item = itemStackFromBase64(itemData) // 직렬화 해제 함수
                        items.add(item)
                    }
                }
            }

            // 📌 2. 아이템 삭제
            val delsql = "DELETE FROM not_selling_items WHERE item_data=?"
            conn.prepareStatement(delsql).use { pstmt ->
                for (itemData in dleitems) {
                    pstmt.setString(1, itemData)
                    pstmt.executeUpdate()
                }
            }
        }
    } catch (e: Exception) {
        javaPlugin.logger.warning("[find_and_delete_expired_items] 유저($userUuid)의 만료된 아이템 찾기/삭제 중 오류 발생: $e")
    }

    return items
}