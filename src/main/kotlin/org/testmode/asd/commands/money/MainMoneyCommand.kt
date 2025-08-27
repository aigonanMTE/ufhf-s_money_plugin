package org.testmode.asd.commands.money

import org.bukkit.Bukkit
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
            sender.sendMessage("ㅗ")
            return true
        }

        when (args[0].lowercase()) {
            "보내기" -> {
                if (args.size < 3) {
                    sender.sendMessage("/돈 보내기 <플레이어> <금액>")
                } else {
                    val targetName = args[1]
                    val amount = args[2].toIntOrNull()
                    if (amount == null) {
                        sender.sendMessage("금액을 숫자로 입력해주세요!")
                    } else {
                        sender.sendMessage("${targetName}에게 ${amount}원을 보냅니다!")
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
                // 첫 번째 인자 자동완성 (명령어 종류)
                val options = listOf("보내기")
                completions.addAll(options.filter { it.startsWith(args[0], ignoreCase = true) })
            }
            2 -> {
                // 두 번째 인자 자동완성 (온라인 플레이어 이름)
                completions.addAll(Bukkit.getOnlinePlayers()
                    .map { it.name }
                    .filter { it.startsWith(args[1], ignoreCase = true) })
            }
        }

        return completions
    }
}
