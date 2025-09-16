package org.testmode.asd.listeners

import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.io.BukkitObjectInputStream
import org.testmode.asd.SQL.money.sendMoney
import org.testmode.asd.SQL.usershop.sellItem
import org.testmode.asd.commands.shop.itemStackFromBase64
import org.testmode.asd.commands.shop.openShopGUI
import org.testmode.asd.commands.shop.open_buy_check
import java.io.ByteArrayInputStream
import java.util.*
import kotlin.reflect.typeOf

class ShopListener(private val javaPlugin: JavaPlugin) : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        // GUI 판별
        if (!event.view.title.startsWith("상점") && !event.view.title.startsWith("구매 확인창")) return

        event.isCancelled = true // 아이템 이동 방지

        val clicked = event.currentItem ?: return
        if (!clicked.hasItemMeta()) return

        val name = ChatColor.stripColor(clicked.itemMeta?.displayName ?: "")

        // 상점 GUI
        if (event.view.title.startsWith("상점")) {
            val currentPage = event.view.title
                .replace("상점  ", "")
                .replace("페이지", "")
                .trim()
                .toIntOrNull() ?: 1

            when (getTag(clicked, javaPlugin, "page_button")) {
                "prev" -> openShopGUI(player, javaPlugin, currentPage - 1)
                "next" -> openShopGUI(player, javaPlugin, currentPage + 1)
            }

            if (getTag(clicked, javaPlugin, "sell") == "true") {
                // 구매 확인창 열기
                if (getTag(clicked,javaPlugin,"seller") == player.name){
                    player.sendMessage("${ChatColor.YELLOW}그거 아시나요? 자신이 올린 아이템을 자신이 다시 사면 그게 아이템 상점에서 내리기 기능 아닐까요?\n" +
                            "그러니 아이템을 상점에서 내리고 싶다면 아이템을 그냥 다시 사세요")
                }
                open_buy_check(player, javaPlugin, clicked)
            }
        }

        // 구매 확인창 GUI
        if (event.view.title.startsWith("구매 확인창")) {
            when (getTag(clicked, javaPlugin, "action")) {
                "confirm" -> {
                    try {
                        val inv = event.inventory
                        val item = inv.getItem(4)
                        if (item !is ItemStack) return
                        val itemValue = getTag(item, javaPlugin, "Item_value")?.toIntOrNull() ?: return
                        val sellerUuid = getTag(item, javaPlugin, "seller_uuid") ?: return
                        val itemId = getTag(item, javaPlugin, "id")?.toIntOrNull() ?: return
                        val itemData = getTag(item, javaPlugin, "sell_Item_data")
                        if (itemData == null) {
                            player.sendMessage("${ChatColor.RED}아이템 데이터가 비어 있습니다 (태그 없음)")
                            return
                        }


                        // 상점에서 아이템 내리기
                        if (!sellItem(javaPlugin, itemId)){
                            player.sendMessage("${ChatColor.RED}구매 도중 오류 발생")
                            return
                        }
                        // 송금
                        if (!sendMoney(javaPlugin, player.uniqueId.toString(), sellerUuid, itemValue)){
                            player.sendMessage("${ChatColor.RED}돈 보내기중 오류 발생")
                            return
                        }
                        //유저에게 아이템 지급
                        val buingItem = itemStackFromBase64(itemData)
                        player.give(buingItem)


                        player.sendMessage("${ChatColor.GREEN}구매가 완료되었습니다!")
                        player.closeInventory()
                    }catch (e:Exception){
                        player.sendMessage("${ChatColor.RED}구매 도중 오류 발생")
                        javaPlugin.logger.warning("유저 상점 아이템 구매 도중 오류 발생 \n $e")
                    }
                }
                "cancel" -> {
                    openShopGUI(player, javaPlugin, 1)
                    player.sendMessage("${ChatColor.RED}구매를 취소했습니다")
                }
            }
        }
    }
}



fun getTag(item: ItemStack, javaPlugin: JavaPlugin , key:String): String? {
    val meta = item.itemMeta ?: return null
    val keY = NamespacedKey(javaPlugin, key)
    return meta.persistentDataContainer.get(keY, PersistentDataType.STRING)
}


