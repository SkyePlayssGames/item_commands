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
            val itemJson: String? = if (resourceJson.asJsonObject.keySet().contains("item")) resourceJson.asJsonObject["item"].asString else null
            val commandsJson: JsonElement = resourceJson.asJsonObject["commands"]
            val predicatesJson: JsonElement = if (resourceJson.asJsonObject.keySet().contains("predicates")) resourceJson.asJsonObject["predicates"] else JsonNull.INSTANCE
            var item: Item = Items.AIR
            if (commandsJson.isJsonNull) {
                ItemCommands.LOGGER.error("Item Command $key is missing a commands field")
                return@forEach
            }
            if (itemJson != null) {
                item = BuiltInRegistries.ITEM[Identifier.parse(itemJson)].getOrNull()?.value() ?: Items.AIR
                if (item == Items.AIR) {
                    ItemCommands.LOGGER.error("Item Command $key has non-existent item")
                    return@forEach
                }
            }

            val resultCommands = validateWords(commandsJson.asJsonArray)
            if (resultCommands.second != "") {
                ItemCommands.LOGGER.error(resultCommands.second + " for Item Command $key")
                return
            }
            if (resultCommands.first == listOf<String>()) {
                ItemCommands.LOGGER.error("No elements found in commands for Item Command $key")
                return
            }

            val resultPredicates = if (predicatesJson.isJsonNull) Pair(listOf(), "") else validatePredicates(predicatesJson.asJsonArray)
            if (resultPredicates.second != "") {
                ItemCommands.LOGGER.error(resultCommands.second + " for Item Command $key")
                return
            }


            ItemCommands.RegisteredItemCommands.add(ItemCommand(item, resultCommands.first, resultPredicates.first))
        }
    }

    private fun validateWords(words: JsonArray): Pair<List<String>, String> {
        val result: ArrayList<String> = arrayListOf()
        words.forEach { element ->
            if (!element.isJsonPrimitive) {
                return Pair(listOf(), "One of the elements of commands is not a String")
            }
            result.add(element.asString)
        }
        return Pair(result, "")
    }

    private fun validatePredicates(words: JsonArray): Pair<List<ItemPredicate>, String> {
        val result: ArrayList<ItemPredicate> = arrayListOf()
        words.forEach { element ->
            if (!element.isJsonObject) {
                return Pair(listOf(), "One of the elements of predicates is not an Object")
            }
            val predicate = ItemPredicate.CODEC.parse(JsonOps.INSTANCE, element.asJsonObject)
            if (predicate.isError) return Pair(listOf(), "One of the elements of predicates is not an Object")
            result.add(predicate.orThrow)
        }
        return Pair(result, "")
    }
}