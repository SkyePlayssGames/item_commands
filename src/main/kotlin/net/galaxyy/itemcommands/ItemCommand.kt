package net.galaxyy.itemcommands

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.predicates.ItemPredicate
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.Optional

data class ItemCommand(val item: Optional<Holder<Item>>, val commandList: List<String>, val predicate: List<ItemPredicate>) {
    fun run(serverPlayer: ServerPlayer) {
        commandList.forEach { command ->
            serverPlayer.level().server.commands.dispatcher.execute(
                command,
                serverPlayer.createCommandSourceStack()
            )
        }
    }

    fun test(itemStack: ItemStack): Boolean {
        if (item.isPresent && !itemStack.`is`(item.get())) return false
        predicate.forEach { predicate ->
            if (!predicate.test(itemStack)) return false
        }
        return true
    }

    companion object {
        val CODEC: Codec<ItemCommand> = RecordCodecBuilder.create { builder -> builder.group(
            Item.CODEC.optionalFieldOf("item").forGetter(ItemCommand::item),
            Codec.list(Codec.STRING).fieldOf("commands").forGetter(ItemCommand::commandList),
            Codec.list(ItemPredicate.CODEC).optionalFieldOf("predicates", listOf()).forGetter(ItemCommand::predicate)
        ).apply(builder, ::ItemCommand)}
    }
}