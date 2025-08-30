package org.testmode.asd

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.testmode.asd.commands.money.MainMoneyCommand


class Asd : JavaPlugin(), Listener {

    override fun onEnable() {
        // 이벤트 리스너 등록
        server.pluginManager.registerEvents(this, this)
        if (!makePluginFolder(this)){
            logger.warning("""===================================================================
                \n폴더 생성중 오류 발생!
                \n===================================================================
            """.trimMargin())
            error("폴더 생성중 오류 발생!")
        }

        // 커맨드 등록
        getCommand("돈")?.setExecutor(MainMoneyCommand())
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
            setupScoreboard(player)
        }

        private fun setupScoreboard(player: Player) {
            val manager = Bukkit.getScoreboardManager() ?: return
            val board: Scoreboard = manager.newScoreboard

            // 새로운 objective 생성
            val objective: Objective = board.registerNewObjective("test", "dummy", "§a테스트 보드")
            objective.displaySlot = DisplaySlot.SIDEBAR

            // 점수 추가
            objective.getScore("§b플레이어: ${player.name}").score = 2
            objective.getScore("${ChatColor.GREEN}돈 : 1000").score = 1
            objective.getScore("§e서버에 오신걸 환영!").score = 0

            // 플레이어에게 보드 적용
            player.scoreboard = board
        }
    }

//    @EventHandler
//    fun onBlockBreak(event: BlockBreakEvent) {
//        logger.info("${event.player.name} 이(가) ${event.block.type} 블록을 파괴했습니다!")
//    }
}
