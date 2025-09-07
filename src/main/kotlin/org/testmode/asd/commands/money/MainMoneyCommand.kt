package org.testmode.asd.commands.money

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.testmode.asd.SQL.getmoney
import org.testmode.asd.SQL.sendMoney

class MainMoneyCommand(private val javaPlugin: JavaPlugin) : CommandExecutor, TabCompleter {

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
        val money = getmoney(javaPlugin , sender.uniqueId.toString())

        if (args.isEmpty()) {
            sender.sendMessage(
                """
                ${ChatColor.YELLOW}===========돈===========
                이름 : ${sender.name}
                잔액 : $money
                송금 : /돈 보내기 <플레이어 이름> <금액>
                조회 : /돈 조회 <플레이어 이름>
                =======================
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

                    val targetMoney: Int = getmoney(javaPlugin, target.uniqueId.toString())!!
                    val amount = args[2].toIntOrNull()
                    if (amount == null || amount <= 100 || amount > money!!) {
                        sender.sendMessage("100원 이하로는 송금이 불가합니다")
                        return true
                    } else if (amount >= 10000000) {
                        sender.sendMessage("10000000원 이상으론 송금 할수 없습니다.")
                        return true
                    } else if (targetMoney >= Int.MAX_VALUE - 15000000) {
                        sender.sendMessage("입금 타겟의 돈이 최대 한도에 도달하였습니다.")
                    } else {
                        if (!sendMoney(javaPlugin, sender.uniqueId.toString(), target.uniqueId.toString(), amount)) {
                            sender.sendMessage("${ChatColor.RED}송금중 오류 발생! \n 보내기 전 돈 값${money}")
                            return true
                        }
                        sender.sendMessage("${target.name}에게 ${amount}원을 보냅니다!")
                        target.sendMessage("${sender.name}님이 당신에게 ${amount}원을 보냈습니다!")
                    }
                }
                return true
            }
            "조회" ->{
                MoneyCheck(javaPlugin,sender,args)
                }
            }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        val completions = mutableListOf<String>()

        when (args.size) {
            // 첫 번째 인자 자동완성
            1 -> {
                val options = listOf("보내기", "조회")
                completions.addAll(options.filter { it.startsWith(args[0], ignoreCase = true) })
            }
            // 두 번째 인자 자동완성 (플레이어 이름)
            2 -> {
                when (args[0].lowercase()) {
                    "보내기", "조회" -> {
                        completions.addAll(
                            Bukkit.getOnlinePlayers()
                                .map { it.name }
                                .filter { it.startsWith(args[1], ignoreCase = true) }
                        )
                    }
                }
            }
            // 세 번째 인자 자동완성 (보내기 금액)
            3 -> {
                if (args[0].equals("보내기", ignoreCase = true)) {
                    completions.addAll(listOf("1000", "5000", "10000"))
                }
            }
        }

        return completions
    }
}
