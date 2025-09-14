package org.testmode.asd.commands.shop

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.io.BukkitObjectInputStream
import org.testmode.asd.SQL.usershop.getitemlist
import java.io.ByteArrayInputStream
import java.util.*

// TODO: 아이템 클릭시 구메 확인창 띄우기
class MainShopCommand(private val javaPlugin: JavaPlugin) : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("플레이어만 사용할 수 있는 명령어입니다.")
            return true
        }
        val player: Player = sender

        if (args.isEmpty()) {
            openShopGUI(player, javaPlugin, 1) // 기본 1페이지
            return true
        }

        when (args[0].lowercase()) {
            "등록" -> {
                if (args.size < 2) {
                    player.sendMessage("${ChatColor.YELLOW} 가격을 입력해주세요")
                    return true
                }
                val value = args[1].toIntOrNull()
                if (player.itemInHand.isEmpty) {
                    player.sendMessage("${ChatColor.YELLOW} 손에 등록할 아이템을 들고 명령어를 사용해주세요")
                    return true
                } else if (value == null) {
                    player.sendMessage("${ChatColor.YELLOW}정상적인 가격으로 입력해주세요")
                    return true
                }else if (value < 100) {
                    player.sendMessage("${ChatColor.YELLOW} 최소 가격은 100원 이상이어야 합니다.")
                    return true
                } else if (value > Int.MAX_VALUE - 15000) {
                    player.sendMessage("${ChatColor.YELLOW} 입력한 값이 너무 큽니다.")
                    return true
                }
                val item = player.itemInHand
                val itemInHand = player.inventory.itemInMainHand
                if (itemInHand.type != Material.AIR) {
                    player.inventory.setItemInMainHand(null) // 또는 ItemStack(Material.AIR)
                }
                itemUploadCommand(javaPlugin, player, item, value)
            }
        }
        return true
    }

    // ⬇️ 자동완성 기능 추가
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (args.size == 1) {
            return listOf("등록")
                .filter { it.startsWith(args[0], ignoreCase = true) }
                .toMutableList()
        }

        if (args.size == 2 && args[0].equals("등록", ignoreCase = true)) {
            // 가격 입력 힌트
            return listOf("100", "1000", "5000", "10000")
                .filter { it.startsWith(args[1]) }
                .toMutableList()
        }

        return mutableListOf()
    }
}

fun itemFromBase64(data: String): ItemStack {
    val byteArray = Base64.getDecoder().decode(data)
    ByteArrayInputStream(byteArray).use { inputStream ->
        BukkitObjectInputStream(inputStream).use { dataInput ->
            return dataInput.readObject() as ItemStack
        }
    }
}

fun openShopGUI(player: Player, javaPlugin: JavaPlugin, page: Int) {
    val gui: Inventory = Bukkit.createInventory(null, 54, "상점 - ${page}페이지")

    val border = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta.apply { setDisplayName("") }
    }

    // 테두리 채우기
    for (i in 0..8) {
        gui.setItem(i, border)
        gui.setItem(i + 45, border)
    }
    for (i in 1..4) {
        gui.setItem(10 + 9 * i - 10, border)
        gui.setItem(10 + 9 * i - 2, border)
    }

    // 아이템 불러오기 (페이지네이션 적용)
    val itemList = getitemlist(javaPlugin, page, 28)
    var slot = 10
    for (itemMap in itemList) {
        val itemStack = itemFromBase64(itemMap["item_data"] as String)

        val meta = itemStack.itemMeta
        meta.lore = listOf(
            "${ChatColor.YELLOW}판매자: ${itemMap["seller_name"]}",
            "${ChatColor.GREEN}가격: ${itemMap["value"]}원",
            "${ChatColor.GRAY}업로드: ${itemMap["upload_date"]}"
        )
        itemStack.itemMeta = meta
        setTag(itemStack , javaPlugin , "sell" , "true")
        setTag(itemStack , javaPlugin , "Item_value" , itemMap["value"].toString())
        setTag(itemStack , javaPlugin , "seller" , itemMap["seller_name"].toString())
        setTag(itemStack , javaPlugin , "sell_Item_data" , itemMap["item_data"].toString())
        gui.setItem(slot, itemStack)

        if ((slot + 1) % 9 == 8) {
            slot += 3
        } else {
            slot++
        }
    }

    // 이전 페이지 버튼
    if (page > 1) {
        val prev = ItemStack(Material.ARROW).apply {
            itemMeta = itemMeta.apply { setDisplayName("${ChatColor.YELLOW}이전 페이지") }
        }
        setTag(prev, javaPlugin,"page_button", "prev")
        gui.setItem(48, prev)
    }

    // 📖 현재 페이지 표시
    val pageInfo = ItemStack(Material.BOOK).apply {
        itemMeta = itemMeta.apply {
            setDisplayName("${ChatColor.AQUA}${page} 페이지")
            lore = listOf("${ChatColor.GRAY}현재 보고 있는 페이지입니다")
        }
    }
    gui.setItem(49, pageInfo)

    // 다음 페이지 버튼
    val next = ItemStack(Material.ARROW).apply {
        itemMeta = itemMeta.apply { setDisplayName("${ChatColor.YELLOW}다음 페이지") }
    }
    setTag(next, javaPlugin, "page_button" , "next")
    gui.setItem(50, next)

    // 도움말 아이템
    val help = ItemStack(Material.DIAMOND).apply {
        itemMeta = itemMeta.apply {
            setDisplayName("상점 사용법")
            lore = listOf(
                "사고 싶은 아이템을 클릭해 구매할 수 있습니다",
                "/상점 등록 [가격] 으로 아이템 등록 가능"
            )
        }
    }
    gui.setItem(4, help)

    player.openInventory(gui)
}

fun open_buy_check(player: Player , javaPlugin: JavaPlugin , item: ItemStack){
    val gui: Inventory = Bukkit.createInventory(null, 54, "구매 확인창")

    val border = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta.apply { setDisplayName("") }
    }

    // 테두리 채우기
    for (i in 0..8) {
        gui.setItem(i, border)
        gui.setItem(i + 45, border)
    }
    for (i in 1..4) {
        gui.setItem(10 + 9 * i - 10, border)
        gui.setItem(10 + 9 * i - 2, border)
        gui.setItem(4 + 9 * i, border)
    }
    gui.setItem(4 , item)

    val yes = ItemStack(Material.GREEN_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta.apply { setDisplayName("${ChatColor.GREEN}구매하기") }
    }
    setTag(yes , javaPlugin , "buy" , "buy")
    val no = ItemStack(Material.RED_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta.apply { setDisplayName("${ChatColor.RED}취소하기") }
    }
    setTag(no , javaPlugin, "cancel" , "cancel")
    for (i in 0..3){
        gui.setItem(10 + 9 * i , yes)
        gui.setItem(11 + 9 * i , yes)
        gui.setItem(12 + 9 * i , yes)
        gui.setItem(14 + 9 * i , no)
        gui.setItem(15 + 9 * i , no)
        gui.setItem(16 + 9 * i , no)
    }

    player.openInventory(gui)
}


fun setTag(item: ItemStack, javaPlugin: JavaPlugin, key:String, tag: String) {
    val meta = item.itemMeta ?: return
    val keY = NamespacedKey(javaPlugin, key)
    meta.persistentDataContainer.set(keY, PersistentDataType.STRING, tag)
    item.itemMeta = meta
}
