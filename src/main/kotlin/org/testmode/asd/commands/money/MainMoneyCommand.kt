package org.testmode.asd.commands.money

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class MainMoneyCommand : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("님 플레이어 아님 꺼지셈")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(
                """
                ${ChatColor.YELLOW}===========돈===========
                이름 : ${sender.name}
                잔액 : {나중에 돈 알아서}
                송금 : /돈 <보내기> <플레이어 이름> <금액>
                ===========돈===========
                """.trimIndent()
            )
            return true
        }

        when (args[0].lowercase()) {
            "보내기" -> {
                if (args.size < 3) {
                    sender.sendMessage("/돈 보내기 <플레이어> <금액>")
                } else {
                    val targetName = args[1]
                    if (targetName == sender.name) {
                        sender.sendMessage("자기 자신에게는 입금이 불가 합니다.")
                        return true
                    }

                    val target = Bukkit.getPlayerExact(targetName)
                    if (target == null || !target.isOnline) {
                        sender.sendMessage("해당 플레이어가 접속 중이 아닙니다!")
                        return true
                    }

                    val amount = args[2].toIntOrNull()
                    if (amount == null || amount <= 0) {
                        sender.sendMessage("금액을 올바른 숫자로 입력해주세요!")
                    } else {
                        sender.sendMessage("${target.name}에게 ${amount}원을 보냅니다!")
                        target.sendMessage("${sender.name}님이 당신에게 ${amount}원을 보냈습니다!")
                        // 실제 송금 로직 구현
                    }
                }
                return true
            }
        }

        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        val completions = mutableListOf<String>()

        when (args.size) {
            1 -> {
                val options = listOf("보내기")
                completions.addAll(options.filter { it.startsWith(args[0], ignoreCase = true) })
            }
            2 -> {
                completions.addAll(
                    Bukkit.getOnlinePlayers()
                        .map { it.name }
                        .filter { it.startsWith(args[1], ignoreCase = true) }
                )
            }
        }

        return completions
    }
}
