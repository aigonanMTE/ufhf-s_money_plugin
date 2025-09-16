package org.testmode.asd.setting

import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.DriverManager

fun makePluginFolder(javaPlugin: JavaPlugin): Boolean {
    try {
        val pluginFolder = javaPlugin.dataFolder
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs()
        }

        //json 폴더 생성
        val jsonFolder = File(pluginFolder,"json")
        if (!jsonFolder.exists()){
            jsonFolder.mkdirs()
        }

        //setting.json 파일 생성
        val settingJson = File(jsonFolder,"setting.json")
        if(!settingJson.exists()){
            settingJson.createNewFile()
            javaPlugin.logger.info("setting.json 파일 생성됌")
            javaPlugin.logger.info("setting.json 내용 추가중...")
            if (!overwriteJson(settingJson)){
                javaPlugin.logger.warning("setting.json 내용 추가중 오류 발생!")
                return false
            }
            javaPlugin.logger.info("setting.json 파일 내용 추가 성공")
        }

        // db 폴더 생성
        val dbFolder = File(pluginFolder, "db")
        if (!dbFolder.exists()) {
            dbFolder.mkdirs()
        }

        // money.db 파일 생성
        val moneyDbFile = File(dbFolder, "money.db")
        if (!moneyDbFile.exists()) {
            moneyDbFile.createNewFile()
            javaPlugin.logger.info("money.db 파일 생성됨")
        }

        val userShopDbFile = File(dbFolder,"UserShop.db")
        if (!userShopDbFile.exists()){
            userShopDbFile.createNewFile()
            javaPlugin.logger.info("UserShop.db 파일 생성됌")
        }

        // money.db sql
        var connection = DriverManager.getConnection("jdbc:sqlite:${moneyDbFile.absolutePath}")
        connection.use { conn ->
            val stmt = conn.createStatement()
            stmt.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS user_money (
                    user_uuid TEXT UNIQUE NOT NULL,
                    money     INTEGER NOT NULL DEFAULT 0
                );
                """.trimIndent()
                        )

            stmt.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS money_log (
                    number       INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    system       TEXT    NOT NULL,
                    target_uuid  TEXT    NOT NULL,
                    target_name  TEXT    NOT NULL,
                    sender_uuid  TEXT    DEFAULT 'system',
                    sender_name  TEXT    DEFAULT 'system',
                    type         TEXT    NOT NULL,
                    date         TEXT    NOT NULL,
                    value        INTEGER NOT NULL
                );
                """.trimIndent()
            )

            javaPlugin.logger.info("user_money , money_log 테이블 확인/생성 완료")
        }

        connection = DriverManager.getConnection("jdbc:sqlite:${userShopDbFile.absolutePath}")
        connection.use { conn ->
            val stmt = conn.createStatement()
            stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS shop_item_list (
                id       INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                item_data   TEXT    NOT NULL,
                seller_uuid TEXT    NOT NULL,
                seller_name TEXT    NOT NULL,
                value       INTEGER NOT NULL,
                upload_date TEXT    NOT NULL
            );
            """.trimIndent())
            javaPlugin.logger.info("")
        }

    } catch (e: Exception) {
        javaPlugin.logger.warning("폴더/DB 설정 중 오류 발생\n$e")
        return false
    }
    return true
}
