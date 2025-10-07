package org.testmode.asd.commands.shop

import org.apache.commons.lang3.ObjectUtils.Null
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
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.io.BukkitObjectInputStream
import org.testmode.asd.SQL.usershop.find_and_delete_expired_items
import org.testmode.asd.SQL.usershop.getExpiredItem
import org.testmode.asd.SQL.usershop.getitemlist
import org.testmode.asd.setting.SettingsManager
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// TODO:2주동안 안팔린 아이템은 유저한테 다시 돌려주셈
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
                } else if (value < 100) {
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

            "만료된_아이템_반환" -> {
                if (SettingsManager.getSettingValue("userShop.use_Item_return_cycle").toString() == "true") {

                    if (args.size < 2) {
                        player.sendMessage(
                            "${ChatColor.RED}⚠ 인벤토리가 가득 차있으면 아이템이 증발하거나 바닥에 떨어질 수 있습니다.\n" +
                                    "꼭 인벤토리를 비우고 명령어를 입력해주세요.\n" +
                                    "확인했다면 /상점 만료된_아이템_반환 yes 를 입력해주세요"
                        )
                        return true
                    }

                    if (args[1].equals("yes", ignoreCase = true)) {
                        val items = find_and_delete_expired_items(javaPlugin, player.uniqueId.toString())
                        if (items.isEmpty()) {
                            player.sendMessage("${ChatColor.YELLOW}반환할 만료 아이템이 없습니다.")
                            return true
                        } else {
                            items.forEach { item ->
                                player.inventory.addItem(item)
                            }
                            player.sendMessage("${ChatColor.GREEN}아이템 반환을 완료 하였습니다.")
                            return true
                        }
                    } else if (args[1] == "목록") {
                        openRetunGui(player,javaPlugin)
                    }else {
                        player.sendMessage(
                            "${ChatColor.RED}⚠ 인벤토리가 가득 차있으면 아이템이 증발하거나 바닥에 떨어질 수 있습니다.\n" +
                                    "꼭 인벤토리를 비우고 명령어를 입력해주세요.\n" +
                                    "확인했다면 /상점 만료된_아이템_반환 yes 를 입력해주세요"
                        )
                    }

                } else {
                    player.sendMessage("${ChatColor.RED}이 서버에서는 이 기능을 사용할 수 없습니다.")
                }
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
            return listOf("등록", "만료된_아이템_반환")
                .filter { it.startsWith(args[0], ignoreCase = true) }
                .toMutableList()
        }

        if (args.size == 2 && args[0].equals("등록", ignoreCase = true)) {
            // 가격 입력 힌트
            return listOf("100", "1000", "5000", "10000")
                .filter { it.startsWith(args[1]) }
                .toMutableList()
        }else if (args.size == 2 && args[0].equals("만료된_아이템_반환", ignoreCase = true)){
            return listOf("1" , "yes","목록").filter { it.startsWith(args[1]) }.toMutableList()
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
    val gui: Inventory = Bukkit.createInventory(null, 54, "상점  ${page}페이지")

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

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val uploadDateStr = itemMap["upload_date"] as String
        val uploadDate = LocalDateTime.parse(uploadDateStr, formatter)

// 14일 후 계산
        val expireCycle = SettingsManager.getSettingValue("userShop.Item_return_cycle").toString().toLongOrNull()
        if (expireCycle !is Long){
            javaPlugin.logger.warning("[openShopGUI] 유저상점 아이템 만료일 불러오기중 오류 발생 \nuserShop.Item_return_cycle값이 숫자가 아니거나 잘못되었습니다")
            player.closeInventory()
            player.sendMessage("${ChatColor.RED}아이템 불러오기중 오류가 발생하였습니다.")
            return
        }
        val expireDate = uploadDate.plusDays(expireCycle)

// 문자열로 다시 변환
        val expireDateStr = expireDate.format(formatter)

        val meta = itemStack.itemMeta
        meta.lore = listOf(
            "${ChatColor.YELLOW}판매자: ${itemMap["seller_name"]}",
            "${ChatColor.GREEN}가격: ${itemMap["value"]}원",
            "${ChatColor.GRAY}업로드: ${itemMap["upload_date"]}",
            "${ChatColor.RED}만료일 : $expireDateStr"
        )
        itemStack.itemMeta = meta
        setTag(itemStack , javaPlugin , "sell" , "true")
        setTag(itemStack , javaPlugin , "Item_value" , itemMap["value"].toString())
        setTag(itemStack , javaPlugin , "seller" , itemMap["seller_name"].toString())
        setTag(itemStack,javaPlugin,"seller_uuid",itemMap["seller_uuid"].toString())
        setTag(itemStack , javaPlugin , "sell_Item_data" , itemMap["item_data"].toString())
        setTag(itemStack , javaPlugin , "id" , itemMap["id"].toString())
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
    setTag(yes , javaPlugin , "action" , "confirm")
    val no = ItemStack(Material.RED_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta.apply { setDisplayName("${ChatColor.RED}취소하기") }
    }
    setTag(no , javaPlugin, "action" , "cancel")
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

fun openRetunGui(player: Player, javaPlugin: JavaPlugin) {
    val gui: Inventory = Bukkit.createInventory(null, 54, "만료된 아이템 목록")

    val border = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta.apply { setDisplayName("") }
    }

    // 상단 및 하단 테두리
    for (i in 0..8) {
        gui.setItem(i, border)
        gui.setItem(45 + i, border)
    }

    // 좌우 테두리
    for (row in 1..4) {
        gui.setItem(row * 9, border)
        gui.setItem(row * 9 + 8, border)
    }

    // 도움말 다이아몬드
    val help = ItemStack(Material.DIAMOND).apply {
        itemMeta = itemMeta.apply {
            setDisplayName("§b유저상점 만료 아이템 보관함")
            lore = listOf(
                "§7아이템 보관함에 §c3일 동안§7 보관이 가능합니다.",
                "§73일이 지나면 아이템이 §4삭제됩니다§7."
            )
        }
    }
    gui.setItem(4, help)

    // ✅ 만료된 아이템 불러오기
    val list = getExpiredItem(javaPlugin, player)

    // ✅ GUI의 빈칸 인덱스 (테두리 제외)
    val itemSlots = listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    )

    // ✅ 아이템을 GUI에 채우기
    for ((index, data) in list.withIndex()) {
        if (index >= itemSlots.size) break
        val slot = itemSlots[index]

        val item = data["item"] as? ItemStack ?: continue
        val expiration = data["expiration_at"] as? Long ?: 0L //초
        javaPlugin.logger.info("만료일$expiration")

        val remaining = expiration - System.currentTimeMillis() / 1000
        val hours = remaining / 3600
        val minutes = (remaining % 3600) / 60


        val meta = item.itemMeta
        meta.lore = (meta.lore ?: listOf()) + listOf(
            "",
            if (remaining > 0)
                "§7만료까지: §e${hours}시간 ${minutes}분 남음"
            else
                "§c이미 만료된 아이템입니다."
        )
        item.itemMeta = meta

        gui.setItem(slot, item)
    }

    player.openInventory(gui)
}
