package kaptainwutax.seedcrackerX.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kaptainwutax.seedcrackerX.SeedCracker;
import kaptainwutax.seedcrackerX.baritone.BaritoneBridge;
import kaptainwutax.seedcrackerX.config.Config;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class BaritoneCommand extends ClientCommand {
    @Override
    public String getName() {
        return "baritone";
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        builder.then(literal("ON").executes(context -> setEnabled(true)))
                .then(literal("OFF").executes(context -> setEnabled(false)))
                .then(literal("stop").executes(context -> stopNow()))
                .then(literal("status").executes(context -> status()))
                .executes(context -> status());
    }

    private int setEnabled(boolean enabled) {
        Config.get().baritoneAutoEnabled = enabled;
        Config.save();
        if (!enabled) {
            SeedCracker.get().getAutoCrackController().stopAutomation(true);
        }
        sendFeedback(enabled ? "Baritone automation enabled." : "Baritone automation disabled.", ChatFormatting.GREEN);
        return 0;
    }

    private int stopNow() {
        SeedCracker.get().getAutoCrackController().stopAutomation(true);
        sendFeedback("Stopped Baritone automation goal.", ChatFormatting.GREEN);
        return 0;
    }

    private int status() {
        if (!BaritoneBridge.isAvailable()) {
            sendFeedback("Baritone not found. Install Baritone to use automation.", ChatFormatting.RED);
            return 0;
        }
        sendFeedback("Baritone automation status: " + SeedCracker.get().getAutoCrackController().getStatus(), ChatFormatting.AQUA);
        return 0;
    }
}
