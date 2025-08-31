package org.testmode.asd.SQL

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.DriverManager

fun userExists(javaPlugin: JavaPlugin, userUuid: String): Boolean {
    val pluginFolder = javaPlugin.dataFolder
    val dbPath = File(pluginFolder, "db${File.separator}money.db")
    val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")

    connection.use { conn ->
        val stmt = conn.prepareStatement("SELECT COUNT(*) FROM user_money WHERE user_uuid = ?")
        stmt.use {
            it.setString(1, userUuid)
            val rs = it.executeQuery()
            return rs.next() && rs.getInt(1) > 0
        }
    }
}

fun addUser(javaPlugin: JavaPlugin, userUUid: String): String {
    val player = Bukkit.getOfflinePlayer(userUUid).player
    if (userExists(javaPlugin, userUUid)) {
        return "notadded" // 이미 존재하는 유저
    }
    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}money.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        connection.use { conn ->
            val sql = "INSERT INTO user_money (user_uuid, money) VALUES (?, ?)"
            val pstmt = conn.prepareStatement(sql)
            pstmt.use {
                it.setString(1, userUUid)
                it.setInt(2, 100000)
                it.executeUpdate()
            }
        }
    } catch (e: Exception) {
        javaPlugin.logger.warning("[addUser]플레이어 ${userUUid}(${player?.name})님의 정보를 추가중 오류 발생 \n$e")
        return "error" // 오류 발생
    }
    javaPlugin.logger.info("[addUser]플레이어 ${userUUid}(${player?.name})님의 정보를 추가하였습니다")
    return "added"
}

fun getmoney(javaPlugin: JavaPlugin, userUuid: String): Int? {
    if (!userExists(javaPlugin,userUuid)){
        javaPlugin.logger.warning("[getmoney] 데이터 베이스에 없는 유저의 정보 검색 시도")
        return null
    }
    val player = Bukkit.getOfflinePlayer(userUuid).player
    try {
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}money.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        //select money from user_money where user_uuid=?;
        connection.use { conn ->
            val stmt = conn.prepareStatement("select money from user_money where user_uuid=?")
            stmt.use {
                it.setString(1, userUuid)
                val rs = it.executeQuery()
                rs.use {
                    return if (rs.next()) {
                        rs.getInt("money") // 컬럼 이름 사용
                    } else {
                        null // 없으면 0 반환 (원하는 기본값)
                    }
                }
            }
        }
    } catch (e: Exception) {
        javaPlugin.logger.warning("[getmoney]플레이어 ${userUuid}(${player?.name})님의 돈 값 불러오기중 오류 발생 \n$e")
        return null // 오류 발생
    }

}

