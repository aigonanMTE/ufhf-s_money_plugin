package org.testmode.asd.listeners

import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.testmode.asd.commands.shop.openShopGUI

class ShopListener(private val javaPlugin: JavaPlugin) : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        if (!event.view.title.startsWith("상점")) return

        event.isCancelled = true // 아이템 이동 방지

        val clicked = event.currentItem ?: return
        if (!clicked.hasItemMeta()) return

        val name = ChatColor.stripColor(clicked.itemMeta?.displayName ?: "")
        val currentPage = event.view.title
            .replace("상점 - ", "")
            .replace("페이지", "")
            .trim()
            .toIntOrNull() ?: 1

        when (name) {
            "이전 페이지" -> openShopGUI(player, javaPlugin, currentPage - 1)
            "다음 페이지" -> openShopGUI(player, javaPlugin, currentPage + 1)
        }
    }
}
