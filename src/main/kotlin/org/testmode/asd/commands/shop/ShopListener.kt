package org.testmode.asd.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ShopListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        // GUI 이름이 "상점" 인지 확인
        if (event.view.title != "상점") return

        event.isCancelled = true // 아이템 꺼내기 방지

        val clickedItem = event.currentItem ?: return
    }
}
