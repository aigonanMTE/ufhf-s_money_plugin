package org.testmode.asd.commands

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class testing(private val plugin: JavaPlugin) : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("/테스트 <닉네임> 으로 사용하세요!")
            return true
        }

        val target: Player? = Bukkit.getPlayerExact(args[0])
        if (target == null) {
            sender.sendMessage("해당 플레이어를 찾을 수 없습니다!")
            return true
        }

        // 반복 작업 시작 (1틱 = 1/20초)
        val taskId = intArrayOf(-1) // 배열로 Wrapping하여 참조 가능하게 함

        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
            if (!target.isOnline) {
                Bukkit.getScheduler().cancelTask(taskId[0])
                return@scheduleSyncRepeatingTask
            }

            // 타이틀 보내기
            target.sendTitle("§c시아 방해", "§7죽지 마세요!", 5, 1, 5) // fadeIn:5, stay:1틱, fadeOut:5틱

            // 채팅 도배
            target.sendMessage("§c시아 방해!!! §c시아 방해!!! §c시아 방해!!!")

            // 드래곤 죽는 소리 재생
            target.playSound(target.location, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.0f)

            // 화면에 토템 사용 이펙트
            target.playEffect(target.location, org.bukkit.Effect.LAVA_INTERACT, 1)
        }, 0L, 1L) // 1틱마다 반복



        sender.sendMessage("${target.name}에게 시아 방해를 시작했습니다!")

        return true
    }
}
