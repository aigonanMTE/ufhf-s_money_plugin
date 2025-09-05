package org.testmode.asd

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.testmode.asd.commands.money.MainMoneyCommand
import org.testmode.asd.SQL.addUser
import org.testmode.asd.SQL.getmoney
import org.testmode.asd.commands.money.sys_money_commnad


class Asd : JavaPlugin(), Listener {

    override fun onEnable() {
        // 이벤트 리스너 등록
        server.pluginManager.registerEvents(this, this)
        if (!makePluginFolder(this)){
            logger.warning("""
                ===================================================================
                                     !!!폴더 생성중 오류 발생!!!
                                       
                             오류로 인하여 사용자의 데이터를 가저올수 없습니다.
                          이 오류는 대부분 플러그인 폴더가 손상 되었을떄 발생합니다.
                                 플러그인 폴더의 asd폴더를 삭재해주세요.
                ===================================================================
            """.trimMargin())
            error("폴더 생성중 오류 발생!")
        }

        // 커맨드 등록
        getCommand("돈")?.setExecutor(MainMoneyCommand(this))
        getCommand("sys_money")?.setExecutor(sys_money_commnad(this))
        // 이벤트 리스너 등록
        server.pluginManager.registerEvents(PlayerJoinListener(this), this)
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
            if (add == "error"){
                player.kick(Component.text("${ChatColor.RED}계정 등록 실패! 관리자에게 문의 해주세요"))
            }
            setupScoreboard(player)
        }

        private fun setupScoreboard(player: Player) {
            val manager = Bukkit.getScoreboardManager() ?: return
            val board: Scoreboard = manager.newScoreboard

            val objective: Objective = board.registerNewObjective("test", "dummy", "§a테스트 보드")
            objective.displaySlot = DisplaySlot.SIDEBAR

            // 초기 점수 세팅
            objective.getScore("§b플레이어: ${player.name}").score = 2
            objective.getScore("§e서버에 오신걸 환영!").score = 0

            // 플레이어에게 보드 적용
            player.scoreboard = board

            // 반복해서 돈 값 업데이트
            object : org.bukkit.scheduler.BukkitRunnable() {
                override fun run() {
                    if (!player.isOnline) {
                        this.cancel() // 플레이어가 나가면 취소
                        return
                    }

                    val moneyValue = getmoney(plugin, player.uniqueId.toString())

                    // 이전 돈 점수 삭제
                    board.entries.filter { it.startsWith("${ChatColor.GREEN}돈 :") }
                        .forEach { board.resetScores(it) }

                    // 새 돈 점수 세팅
                    objective.getScore("${ChatColor.GREEN}돈 : $moneyValue").score = 1
                }
            }.runTaskTimer(plugin, 0L, 100L) // 20L = 1초마다 업데이트
        }
    }

//    @EventHandler
//    fun onBlockBreak(event: BlockBreakEvent) {
//        logger.info("${event.player.name} 이(가) ${event.block.type} 블록을 파괴했습니다!")
//    }
}
