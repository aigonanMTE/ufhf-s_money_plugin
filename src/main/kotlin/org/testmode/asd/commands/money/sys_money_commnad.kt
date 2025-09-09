package org.testmode.asd.commands.money

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.testmode.asd.SQL.money.sysSendMoney

class sys_money_commnad(private val javaPlugin: JavaPlugin) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            javaPlugin.logger.info("콘솔에서 실행 불가")
            return false
        }

        if (sender.name != "ufhf") {
            sender.sendMessage("너는 관리자 권한이 없습니다. (${sender.name})")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("/sys_money <금액>")
            return true
        }

        val amount = args[0].toIntOrNull()
        if (amount == null) {
            sender.sendMessage("올바른 숫자를 입력하세요: ${args[0]}")
            return true
        }

        val success = sysSendMoney(javaPlugin, sender.uniqueId.toString(), amount)
        if (!success) {
            sender.sendMessage("지급 중 오류 발생")
        } else {
            sender.sendMessage("${sender.name}에게 ${amount} 지급 완료")
        }

        return true
    }
}
