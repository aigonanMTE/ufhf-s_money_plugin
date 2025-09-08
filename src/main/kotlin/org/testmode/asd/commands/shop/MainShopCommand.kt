package org.testmode.asd.commands.shop

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class MainShopCommand:CommandExecutor {
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

        // 9칸짜리 GUI 생성 (이름은 "테스트 GUI")
        val gui: Inventory = Bukkit.createInventory(null, 54, "상점")

        val item = ItemStack(Material.DIAMOND)
        val meta: ItemMeta = item.itemMeta
        meta.setDisplayName("""상점 사용법
            |
            |사고 싶은 아이템을 클릭해 살수 있습니다
            |등록은 /상점 등록 명령어를 사용해 등록할수 있습니다
            |구매한 아이템은 바로 지급됩니다
        """.trimMargin())
        item.itemMeta = meta
        gui.setItem(4, item) // 인벤토리 중앙에 배치
        val item2 = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val meta2: ItemMeta = item2.itemMeta
        meta.setDisplayName("")
        item2.itemMeta = meta2
        for (i in 0..8){
            gui.setItem(i+45,item2)
        }

        player.openInventory(gui)
        return true
    }
}