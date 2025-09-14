package org.testmode.asd.listeners

import net.md_5.bungee.api.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.testmode.asd.commands.shop.openShopGUI
import org.testmode.asd.commands.shop.open_buy_check

class ShopListener(private val javaPlugin: JavaPlugin) : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        if (!event.view.title.startsWith("상점") || !event.view.title.startsWith("구매 확인창")) return

        event.isCancelled = true // 아이템 이동 방지

        val clicked = event.currentItem ?: return
        if (!clicked.hasItemMeta()) return

        val name = ChatColor.stripColor(clicked.itemMeta?.displayName ?: "")
        val currentPage = event.view.title
            .replace("상점 - ", "")
            .replace("페이지", "")
            .trim()
            .toIntOrNull() ?: 1

        var tag = getTag(clicked , javaPlugin , "page_button")
        when(tag){
            "prev" -> openShopGUI(player, javaPlugin, currentPage - 1)
            "next" -> openShopGUI(player, javaPlugin, currentPage + 1)
        }
        tag = getTag(clicked , javaPlugin , "sell")
        if (tag == "true"){
            //구매 확인창 열기
            open_buy_check(player,javaPlugin , clicked)
            if (getTag(clicked , javaPlugin , "cancel") == "cancel"){
                openShopGUI(player , javaPlugin, 1)
                player.sendMessage("${ChatColor.RED}구매를 취소했습니다")
            }else{
                //TODO : 구매 로직 만드셈
            }
        }
    }
}


fun getTag(item: ItemStack, javaPlugin: JavaPlugin , key:String): String? {
    val meta = item.itemMeta ?: return null
    val keY = NamespacedKey(javaPlugin, key)
    return meta.persistentDataContainer.get(keY, PersistentDataType.STRING)
}

