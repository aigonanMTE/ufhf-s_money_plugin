package org.testmode.asd.commands.money

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.testmode.asd.SQL.sysSendMoney

class sys_money_commnad(private val javaPlugin: JavaPlugin):CommandExecutor {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p0 !is Player){
            javaPlugin.logger.info("님 플레이어 아님")
            return false
        }else if (p0.name != "ufhf"){
            p0.sendMessage("너이 게이씨 나가 임마!${p0.name}")
            return true
        }
        val moneyFun = sysSendMoney(javaPlugin,p0.uniqueId.toString(),p3[0].toInt())
        if(!moneyFun) {
            p0.sendMessage("오류 시발")
            return true
        }else{
            p0.sendMessage("${p0.name}에게 ${p3[0]} 만큼 지급 완료")
        }
        return true
    }
}