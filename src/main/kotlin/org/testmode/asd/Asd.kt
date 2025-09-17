package org.testmode.asd

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.testmode.asd.commands.money.MainMoneyCommand
import org.testmode.asd.SQL.money.addUser
import org.testmode.asd.SQL.money.getmoney
import org.testmode.asd.commands.money.sys_money_commnad
import org.testmode.asd.commands.shop.MainShopCommand
import org.testmode.asd.commands.testing
import org.testmode.asd.listeners.ShopListener
import org.testmode.asd.setting.SettingsManager
import org.testmode.asd.setting.makePluginFolder
import java.io.File

class Asd : JavaPlugin(), Listener {
    //TODO : json 파일 내용에 맞춰서 코드 짜기 (돈 리더보드는 완성함)
    //TODO : 2주동안 안팔린 쓰래기 돌려주기 기능 제발 만들기
    override fun onEnable() {
        // 이벤트 리스너 등록
        server.pluginManager.registerEvents(this, this)

        if (!makePluginFolder(this)) {
            logger.warning("""
                ===================================================================
                                     !!!폴더 생성중 오류 발생!!!
                                       
                             오류로 인하여 사용자의 데이터를 가저올수 없습니다.
                          이 오류는 대부분 플러그인 폴더가 손상 되었을떄 발생합니다.
                           서버 폴더의 ${this.dataFolder.path}를 삭재해주세요.
                ===================================================================
            """.trimMargin())
            error("폴더 생성중 오류 발생!")
        }

        SettingsManager.loadSettings(File(this.dataFolder.path+"""\json\setting.json"""))


        // 커맨드 등록
        getCommand("테스트")?.setExecutor(testing(this))
        getCommand("돈")?.setExecutor(MainMoneyCommand(this))
        getCommand("sys_money")?.setExecutor(sys_money_commnad(this))
        getCommand("상점")?.setExecutor(MainShopCommand(this))
        server.pluginManager.registerEvents(PlayerJoinListener(this), this)

        //리스너 등록
        server.pluginManager.registerEvents(ShopListener(this), this)

        // ✅ 리로드 시에도 모든 플레이어 보드 다시 세팅
        for (player in Bukkit.getOnlinePlayers()) {
            setupScoreboard(player)
        }

        // ✅ 공용 스케줄러 (모든 플레이어 돈 갱신)
        object : BukkitRunnable() {
            // 실행 전에 JSON 값 캐싱
            private val moneyLine: Int = SettingsManager.getSettingValue("leaderboard.money_line").toString().toIntOrNull() as? Int
                ?: throw IllegalStateException("leaderboard.money_line 값은 Int 타입이어야 합니다.")
            private val moneyLineContentTemplate: String = SettingsManager.getSettingValue("leaderboard.money_line_content") as? String
                ?: throw IllegalStateException("leaderboard.money_line_content 값은 String 타입이어야 합니다.")

            override fun run() {
                for (player in Bukkit.getOnlinePlayers()) {
                    val board = player.scoreboard
                    val objective = board.getObjective("money") ?: continue

                    val moneyValue = getmoney(this@Asd, player.uniqueId.toString())

                    // 기존 라인 엔트리 제거
                    board.getEntriesAtLine(moneyLine).forEach { board.resetScores(it) }

                    // 치환자 처리 후 새 값 반영
                    val replacedContent = replacePlaceholders(moneyLineContentTemplate, player, moneyValue)
                    objective.getScore(replacedContent).score = moneyLine
                }
            }

            // 치환자 함수
            private fun replacePlaceholders(template: String, player: Player, money: Int): String {
                return template
                    .replace("{money}", money.toString())
                    .replace("{player_name}", player.name)
            }

            // 특정 라인에 해당하는 엔트리만 가져오기
            private fun Scoreboard.getEntriesAtLine(line: Int): List<String> {
                val entries = mutableListOf<String>()
                val obj = this.getObjective("money") ?: return entries
                for (entry in this.entries) {
                    if (obj.getScore(entry).score == line) {
                        entries.add(entry)
                    }
                }
                return entries
            }
        }.runTaskTimer(this, 0L, 100L)

        logger.info("Wtf 플러그인 활성화됨!")
    }

    override fun onDisable() {
        logger.info("Wtf 플러그인 비활성화됨!")
    }

    class PlayerJoinListener(private val plugin: Asd) : org.bukkit.event.Listener {

        @org.bukkit.event.EventHandler
        fun onJoin(event: org.bukkit.event.player.PlayerJoinEvent) {
            val player = event.player
            val add = addUser(plugin , player.uniqueId.toString())
            if (add == "error") {
                player.kick(Component.text("${ChatColor.RED}계정 등록 실패! 관리자에게 문의 해주세요"))
                return
            }

            // ✅ 플레이어 접속 시 보드 세팅
            plugin.setupScoreboard(player)
        }
    }

    // ✅ 보드 세팅 함수 (공용 스케줄러용)
    fun setupScoreboard(player: Player) {
        val manager = Bukkit.getScoreboardManager() ?: return
        val board: Scoreboard = manager.newScoreboard

        // 📌 제목 불러오기
        val title = SettingsManager.getSettingValue("leaderboard.title")?.toString() ?: "리더보드"
        val objective: Objective = board.registerNewObjective("money", "dummy", ChatColor.translateAlternateColorCodes('&', title))
        objective.displaySlot = DisplaySlot.SIDEBAR

        // 📌 content 배열 불러오기
        val content = SettingsManager.getSettingValue("leaderboard.content")
        if (content !is List<*>) return

        val moneylien = SettingsManager.getSettingValue("leaderboard.money_line").toString().toIntOrNull()
        if (moneylien !is Int) return

        // ✅ content 배열을 순서대로 점수에 반영
        var scoreValue = content.size
        for ((index, lineAny) in content.withIndex()) {
            if (lineAny !is String) continue

            var line = lineAny

            // 치환 처리
            line = line.replace("{player_name}", player.name)

            // 중복 방지용 공백 추가
            line += " ".repeat(index)
            if (scoreValue == moneylien){
                scoreValue--
            }
            objective.getScore(line).score = scoreValue--
        }

        // 플레이어에게 보드 적용
        player.scoreboard = board
    }
}
