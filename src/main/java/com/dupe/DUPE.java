package com.dupe;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ModInitializer;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class DUPE implements ModInitializer {
	@Override
	public void onInitialize() {
		// Register the -dupe command with wait time paramater
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, enviroment) -> {
			LiteralCommandNode<ServerCommandSource> register = dispatcher.register(literal("dupe")
					.then(argument("waitTime", IntegerArgumentType.integer(0))
							.executes(context -> {
								ServerPlayerEntity player = context.getSource().getPlayer();
								if (player != null) {
									int waitTime = IntegerArgumentType.getInteger(context, "waitTime");

									// Execute the secondary command
									player.getServer().getCommandManager().executeWithPrefix(player.getCommandSource(), "/spawn 1");
									player.sendMessage(Text.literal("Executing Duplication").formatted(Formatting.GREEN), false);

									//wait the specified time before executing hot-bar drop
									new Thread(new Runnable() {
										@Override
										public void run() {
											try {
												Thread.sleep(waitTime);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											for (int i = 0; i < 9; i++) {
												player.getInventory().selectedSlot = i;
												dropItemInHand(player);
												try {
													Thread.sleep(50);
												} catch (InterruptedException e) {
													LOGGER.error("Interrupted while dropping items", e);
												}
											}
											player.sendMessage(Text.literal("Done Executing Duplication").formatted(Formatting.GREEN), false);
											LOGGER.info("Completed duplication");
										}
									});
								}
								return 1;
							})
					));
		});
	}

private void dropItemInHand(PlayerEntity player) {
	ItemStack stack = player.getInventory().getMainHandStack();
	if (!stack.isEmpty()) {
		player.dropItem(stack, true, false);
		player.getInventory().setStack(player.getInventory().selectedSlot, ItemStack.EMPTY);
		LOGGER.debug("Drpped item from slot {}", player.getInventory().selectedSlot);
	}
}
    public static final Logger LOGGER = LoggerFactory.getLogger("dupe");}
