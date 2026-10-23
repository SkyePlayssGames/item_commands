package net.galaxyy.itemcommands

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import kotlinx.serialization.json.Json
import net.minecraft.advancements.predicates.ItemPredicate
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import kotlin.jvm.optionals.getOrNull

object ItemCommandReloadListener : ResourceManagerReloadListener {
    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        ItemCommands.RegisteredItemCommands.clear()

        ItemCommands.LOGGER.info("Loading Item Commands")

        resourceManager.listResources("item_commands", { path -> path.toString().endsWith(".json")}).keys.forEach { key ->
            val resource: Resource = resourceManager.getResourceOrThrow(key)
            val resourceJson: JsonElement = Gson().fromJson(resource.open().readAllBytes().decodeToString(), JsonObject::class.java)
            val command = ItemCommand.CODEC.parse(JsonOps.INSTANCE, resourceJson)
            if (command.isError) {
                ItemCommands.LOGGER.error("Could not parse Item Command $key")
            } else {
                ItemCommands.RegisteredItemCommands.add(command.orThrow)
            }
        }
    }
}