package kaptainwutax.seedcrackerX.baritone;

import kaptainwutax.seedcrackerX.SeedCracker;
import kaptainwutax.seedcrackerX.config.Config;
import kaptainwutax.seedcrackerX.cracker.storage.TimeMachine;
import kaptainwutax.seedcrackerX.finder.FinderQueue;
import kaptainwutax.seedcrackerX.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;

public class AutoCrackController {

    private static final int RETARGET_INTERVAL_TICKS = 80;
    private static final int GOAL_REACHED_DISTANCE_SQR = 16;
    private static final int EXPLORATION_STEP = 160;

    private BlockPos anchor = null;
    private BlockPos lastGoal = null;
    private int explorationIndex = 0;
    private int ticksUntilRetarget = 0;
    private boolean warnedNoBaritone = false;

    public void tick() {
        if (!Config.get().baritoneAutoEnabled || !Config.get().active) {
            stopAutomation(true);
            return;
        }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        Level level = client.level;
        if (player == null || level == null) {
            return;
        }

        if (!BaritoneBridge.isAvailable()) {
            if (!warnedNoBaritone) {
                warnedNoBaritone = true;
                Log.error("baritone.notFound");
            }
            return;
        }

        warnedNoBaritone = false;

        if (Config.get().baritoneAutoStopOnSolved && SeedCracker.get().getDataStorage().getTimeMachine().worldSeeds.size() == 1) {
            stopAutomation(true);
            Log.warn("baritone.autoStopped");
            return;
        }

        BlockPos target = pickTarget(level, player.blockPosition());
        if (target == null) {
            return;
        }

        ticksUntilRetarget--;
        boolean reachedGoal = this.lastGoal != null && player.blockPosition().distSqr(this.lastGoal) <= GOAL_REACHED_DISTANCE_SQR;
        boolean targetChanged = this.lastGoal == null || this.lastGoal.distSqr(target) > GOAL_REACHED_DISTANCE_SQR;
        boolean shouldRetarget = targetChanged || reachedGoal || ticksUntilRetarget <= 0;
        if (shouldRetarget && (targetChanged || reachedGoal) && BaritoneBridge.setGoal(target)) {
            this.lastGoal = target;
            this.ticksUntilRetarget = RETARGET_INTERVAL_TICKS;
            Log.warn("baritone.newGoal", target.getX(), target.getY(), target.getZ());
        }
    }

    private BlockPos pickTarget(Level level, BlockPos playerPos) {
        if (level.dimensionType().skybox() == net.minecraft.world.level.dimension.DimensionType.Skybox.END
                && SeedCracker.get().getDataStorage().getTimeMachine().pillarSeeds == null) {
            return new BlockPos(0, playerPos.getY(), 0);
        }

        if (Config.get().baritonePreferFinderTargets) {
            List<BlockPos> finderTargets = FinderQueue.get().finderControl.getActiveFinders().stream()
                    .flatMap(finder -> finder.getTargetPositions().stream())
                    .distinct()
                    .toList();
            if (!finderTargets.isEmpty()) {
                return finderTargets.stream()
                        .min(Comparator.comparingDouble(pos -> pos.distSqr(playerPos)))
                        .orElse(null);
            }
        }

        if (anchor == null) {
            anchor = playerPos;
        }

        BlockPos target = getExplorationTarget(playerPos.getY());
        if (playerPos.distSqr(target) <= GOAL_REACHED_DISTANCE_SQR) {
            explorationIndex++;
            target = getExplorationTarget(playerPos.getY());
        }
        return target;
    }

    private BlockPos getExplorationTarget(int y) {
        int ring = (explorationIndex / 4) + 1;
        int leg = explorationIndex % 4;
        int offset = ring * EXPLORATION_STEP;
        return switch (leg) {
            case 0 -> anchor.offset(offset, 0, 0);
            case 1 -> anchor.offset(0, 0, offset);
            case 2 -> anchor.offset(-offset, 0, 0);
            default -> anchor.offset(0, 0, -offset);
        }.atY(y);
    }

    public void stopAutomation(boolean clearBaritoneGoal) {
        this.lastGoal = null;
        this.anchor = null;
        this.explorationIndex = 0;
        this.ticksUntilRetarget = 0;
        if (clearBaritoneGoal) {
            BaritoneBridge.clearGoal();
        }
    }

    public String getStatus() {
        TimeMachine timeMachine = SeedCracker.get().getDataStorage().getTimeMachine();
        return "enabled=" + Config.get().baritoneAutoEnabled +
                ", baritone=" + BaritoneBridge.isAvailable() +
                ", goal=" + (lastGoal == null ? "none" : (lastGoal.getX() + " " + lastGoal.getY() + " " + lastGoal.getZ())) +
                ", worldSeeds=" + timeMachine.worldSeeds.size();
    }
}
