package org.testmode.asd.commands.money

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.testmode.asd.SQL.getmoney

fun MoneyCheck(javaPlugin: JavaPlugin,sender:Player, args: Array<out String>):Boolean{
    if (args.size < 2) {
        sender.sendMessage("${ChatColor.YELLOW}/돈 조회 <플레이어>")
        return false
    }
    val targetName = args[1].toString()
    val target = Bukkit.getPlayerExact(targetName)
    if (target == null || !target.isOnline) {
        sender.sendMessage("${ChatColor.YELLOW}해당 플레이어가 접속 중이 아닙니다!")
        return true
    }
    val targetUUID = target.uniqueId.toString()
    val targetMoney = getmoney(javaPlugin, targetUUID)
    if (targetMoney !is Int){
        sender.sendMessage("${ChatColor.RED}조회중 오류가 발생했습니다.")
        return true
    }else{
        sender.sendMessage("${ChatColor.YELLOW}$targetName 님의 잔액 : ${ChatColor.GREEN}$targetMoney")
        return true
    }
}