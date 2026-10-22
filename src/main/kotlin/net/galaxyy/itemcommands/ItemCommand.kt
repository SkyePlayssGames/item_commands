package net.galaxyy.itemcommands

import net.minecraft.advancements.predicates.ItemPredicate
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class ItemCommand(val item: Item, val commandList: List<String>, val predicate: List<ItemPredicate> = listOf()) {
    fun run(serverPlayer: ServerPlayer) {
        commandList.forEach { command ->
            serverPlayer.level().server.commands.dispatcher.execute(
                command,
                serverPlayer.createCommandSourceStack()
            )
        }
    }

    fun test(itemStack: ItemStack): Boolean {
        if (!itemStack.`is`(item) && item != Items.AIR) return false
        predicate.forEach { predicate ->
            if (!predicate.test(itemStack)) return false
        }
        return true
    }
}