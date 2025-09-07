package org.testmode.asd.SQL

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.DriverManager

// TODO:돈 쓰는 상점 만드셈 (gui로)

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
        Log_sys_adduser(javaPlugin , userUUid)
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

fun sendMoney(javaPlugin: JavaPlugin, sender_Uuid:String, target_Uuid:String, amount: Int):Boolean{
    if (!userExists(javaPlugin,sender_Uuid) || !userExists(javaPlugin, target_Uuid)){
        javaPlugin.logger.warning("[sendMoney] 데이터 베이스에 없는 유저의 정보 검색 시도")
        return false
    }
    try {
        val senderMoney = getmoney(javaPlugin, sender_Uuid)
        val targetMoney = getmoney(javaPlugin, target_Uuid)
        if (amount <= 100 || amount >= 10000000 || senderMoney!! <= amount) {
            javaPlugin.logger.warning("[sendMoney] ${sender_Uuid}님이 ${target_Uuid}님에게 허용치를 넘는 송금 시도를 차단 하였습니다.")
            return false
        }
        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}money.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        connection.use { conn ->
            val sql = "update user_money SET money=? where user_uuid=?;"
            val pstmt = conn.prepareStatement(sql)
            pstmt.use {
                it.setInt(1, senderMoney - amount)
                it.setString(2, sender_Uuid)
                it.executeUpdate()
                it.setInt(1, targetMoney!! + amount)
                it.setString(2, target_Uuid)
                it.executeUpdate()
            }
        }
        Log_user_send(javaPlugin, target_Uuid, sender_Uuid , amount)
    }catch (e:Exception){
        javaPlugin.logger.warning("[sendMoney] ${sender_Uuid}님이 ${target_Uuid}님에게 송금 도중 오류 발생 \n$e")
        return false
    }
    return true
}

fun sysSendMoney(javaPlugin: JavaPlugin, target_uuid : String, value:Int):Boolean{
    if (!userExists(javaPlugin, target_uuid)){
        javaPlugin.logger.warning("[sys_send_money] 데이터 베이스에 없는 유저의 정보 검색 시도")
        return false
    }
    try {
        val targetMoney: Int? = getmoney(javaPlugin, target_uuid)
        if (value <= 0 || value >= 10000000) {
            javaPlugin.logger.warning("[sys_send_money] ${target_uuid} 10000000이상 0이하의 돈 지급 차단")
            return false
        }else if (targetMoney!! >= Int.MAX_VALUE - 15000000){
            javaPlugin.logger.warning("[sys_send_money] ${target_uuid}돈 최대 보유 한도 초과 송금 불가")
            return false
        }

        val pluginFolder = javaPlugin.dataFolder
        val dbPath = File(pluginFolder, "db${File.separator}money.db")
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}")
        connection.use { conn ->
            val sql = "update user_money SET money=? where user_uuid=?;"
            val pstmt = conn.prepareStatement(sql)
            pstmt.use {
                it.setInt(1, targetMoney + value)
                it.setString(2, target_uuid.toString())
                it.executeUpdate()
            }
        }
        Log_sys_addMoney(javaPlugin , target_uuid, value)
    }catch (e:Exception){
        javaPlugin.logger.warning("[sys_send_money] ${target_uuid}에게 시스템 돈 지급중 오류 발생 \n $e")
        return false
    }
    return true
}

