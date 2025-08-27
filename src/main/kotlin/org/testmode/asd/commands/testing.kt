package org.testmode.asd.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Test : CommandExecutor{

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p0 is Player){
            p0.sendMessage("omg!!!! 당신은 플레이어인 하지만 op가 있는지 확인하는 나는")
            if (p0.isOp) {
                p0.sendMessage("omg!!!! 당신은 op가 있는")
                return true
            }else {
                p0.sendMessage("omg!!!! 당신은 op가 없는!! 창문을 열고 떨어 지다")
                return true
            }
        }else{
            p0.sendMessage("omg!!!! 당신은 플레이어가 아닌!")
            return true
        }
        return false
    }
}