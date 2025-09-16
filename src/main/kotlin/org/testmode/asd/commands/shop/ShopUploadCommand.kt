package org.testmode.asd.commands.shop

import org.bukkit.ChatColor
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.testmode.asd.SQL.usershop.getValueOfUserItem
import java.io.ByteArrayOutputStream
import java.util.*
import org.testmode.asd.SQL.usershop.uploaditem
import java.io.ByteArrayInputStream

fun itemUploadCommand(javaPlugin: JavaPlugin,uploader:Player , item: ItemStack, value:Int){
    val strItem = itemStackToBase64(item)

    //아이템 겟수 확인
    val userItemValue = getValueOfUserItem(javaPlugin,uploader) ?: return
    if (userItemValue > 7){
        uploader.sendMessage("${ChatColor.YELLOW}상점에 아이템은 최대 7개 까지 올릴수 있습니다.")
        return
    }

    if (!uploaditem(javaPlugin,uploader , strItem , value)){
        uploader.sendMessage("${ChatColor.YELLOW}아이템 등롞 도중 오류가 발생하였습니다.")
        val _item = itemStackFromBase64(strItem)
        uploader.inventory.addItem(_item)
        return
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

// String(Base64) -> ItemStack
fun itemStackFromBase64(data: String): ItemStack {
    val bytes = Base64.getDecoder().decode(data)
    ByteArrayInputStream(bytes).use { byteIn ->
        BukkitObjectInputStream(byteIn).use { `in` ->
            return `in`.readObject() as ItemStack
        }
    }
}