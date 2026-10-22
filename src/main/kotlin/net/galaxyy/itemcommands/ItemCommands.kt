package net.galaxyy.itemcommands

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.resource.v1.ResourceLoader
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.slf4j.LoggerFactory

object ItemCommands : ModInitializer {
	const val MOD_ID: String = "item_commands"
	val LOGGER = LoggerFactory.getLogger(MOD_ID)

	val RegisteredItemCommands: ArrayList<ItemCommand> = arrayListOf()

	override fun onInitialize() {
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
			id("item_commands"), ItemCommandReloadListener
		)
	}

	fun id(path: String): Identifier
		= Identifier.fromNamespaceAndPath(MOD_ID, path)

	fun getItemCommandsForItem(itemStack: ItemStack): List<ItemCommand> {
		return RegisteredItemCommands.filter { command -> command.test(itemStack) }
	}
}
