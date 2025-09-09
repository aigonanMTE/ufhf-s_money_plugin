package org.testmode.asd.commands.shop

import org.bukkit.ChatColor
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayOutputStream
import java.util.*
import org.testmode.asd.SQL.usershop.uploaditem

fun itemUploadCommand(javaPlugin: JavaPlugin,uploader:Player , item: ItemStack, value:Int){
    val strItem = itemStackToBase64(item)
    if (!uploaditem(javaPlugin,uploader , strItem , value)){
        uploader.sendMessage("${ChatColor.YELLOW}아이템 등롞 도중 오류가 발생하였습니다.")
    }else{
        uploader.sendMessage("${ChatColor.GREEN}아이템을 등록 하였습니다.")
    }
}

fun itemStackToBase64(item: ItemStack): String {
    ByteArrayOutputStream().use { byteOut ->
        BukkitObjectOutputStream(byteOut).use { out ->
            out.writeObject(item)
        }
        return Base64.getEncoder().encodeToString(byteOut.toByteArray())
    }
}