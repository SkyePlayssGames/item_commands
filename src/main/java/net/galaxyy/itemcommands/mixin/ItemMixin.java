package net.galaxyy.itemcommands.mixin;

import net.galaxyy.itemcommands.ItemCommand;
import net.galaxyy.itemcommands.ItemCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Item.class)
abstract class ItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    void useInject(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide()) return;
        List<ItemCommand> commands = ItemCommands.INSTANCE.getItemCommandsForItem(player.getItemInHand(hand));
        commands.forEach(command -> command.run(((ServerPlayer) player)));
    }
}
