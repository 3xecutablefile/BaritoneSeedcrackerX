package kaptainwutax.seedcrackerX.baritone;

import net.minecraft.core.BlockPos;

public class BaritoneBridge {

    private static boolean checked = false;
    private static boolean available = false;

    public static boolean isAvailable() {
        if (!checked) {
            checked = true;
            try {
                Class.forName("baritone.api.BaritoneAPI");
                Class.forName("baritone.api.pathing.goals.Goal");
                Class.forName("baritone.api.pathing.goals.GoalBlock");
                available = true;
            } catch (Throwable ignored) {
                available = false;
            }
        }
        return available;
    }

    public static boolean setGoal(BlockPos pos) {
        if (!isAvailable()) {
            return false;
        }
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Object provider = apiClass.getMethod("getProvider").invoke(null);
            Object primaryBaritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object goalProcess = primaryBaritone.getClass().getMethod("getCustomGoalProcess").invoke(primaryBaritone);
            Class<?> goalClass = Class.forName("baritone.api.pathing.goals.Goal");
            Class<?> goalBlockClass = Class.forName("baritone.api.pathing.goals.GoalBlock");
            Object goal = goalBlockClass.getConstructor(int.class, int.class, int.class).newInstance(pos.getX(), pos.getY(), pos.getZ());
            goalProcess.getClass().getMethod("setGoalAndPath", goalClass).invoke(goalProcess, goal);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean clearGoal() {
        if (!isAvailable()) {
            return false;
        }
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Object provider = apiClass.getMethod("getProvider").invoke(null);
            Object primaryBaritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object goalProcess = primaryBaritone.getClass().getMethod("getCustomGoalProcess").invoke(primaryBaritone);
            Class<?> goalClass = Class.forName("baritone.api.pathing.goals.Goal");
            goalProcess.getClass().getMethod("setGoal", goalClass).invoke(goalProcess, new Object[]{null});
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
