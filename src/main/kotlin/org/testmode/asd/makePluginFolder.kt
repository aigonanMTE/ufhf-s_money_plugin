package org.testmode.asd

import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.DriverManager

fun makePluginFolder(javaPlugin: JavaPlugin): Boolean {
    try {
        val pluginFolder = javaPlugin.dataFolder
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs()
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

        // ✅ SQLite 연결 후 테이블 생성
        val connection = DriverManager.getConnection("jdbc:sqlite:${moneyDbFile.absolutePath}")
        connection.use { conn ->
            val stmt = conn.createStatement()
            stmt.use {
                it.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS user_money (
                        user_uuid TEXT UNIQUE NOT NULL,
                        money INTEGER NOT NULL DEFAULT 0
                    );
                    CREATE TABLE IF NOT EXISTS money_log (
                        number      NUMERIC PRIMARY KEY NOT NULL,
                        system      BLOB    NOT NULL,
                        target_uuid NUMERIC NOT NULL,
                        target_name TEXT    NOT NULL,
                        sender_uuid NUMERIC ,
                        sender_name TEXT ,
                        why         TEXT    NOT NULL
                    );
                    """.trimIndent()
                )
            }
            javaPlugin.logger.info("user_money 테이블 확인/생성 완료")
        }

    } catch (e: Exception) {
        javaPlugin.logger.warning("폴더/DB 설정 중 오류 발생\n$e")
        return false
    }
    return true
}
