package org.testmode.asd.commands.shop

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin

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
            val gui: Inventory = Bukkit.createInventory(null, 54, "상점")

            val item = ItemStack(Material.DIAMOND)
            val meta: ItemMeta = item.itemMeta
            meta.setDisplayName(
                """상점 사용법
                |
                |사고 싶은 아이템을 클릭해 살 수 있습니다
                |등록은 /상점 등록 [가격] 명령어를 사용해 등록할 수 있습니다
                |구매한 아이템은 바로 지급됩니다
            """.trimMargin()
            )
            item.itemMeta = meta

            val item2 = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
            val meta2: ItemMeta = item2.itemMeta
            meta2.setDisplayName("")
            item2.itemMeta = meta2

            for (i in 0..8) {
                gui.setItem(i + 45, item2)
                gui.setItem(i, item2)
            }
            for (i in 1..4) {
                gui.setItem(10 + 9 * i - 10, item2)
                gui.setItem(10 + 9 * i - 2, item2)
            }
            gui.setItem(4, item)

            player.openInventory(gui)
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
