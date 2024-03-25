package mdteam.ait.tardis.control;

import mdteam.ait.AITMod;
import mdteam.ait.core.util.DeltaTimeManager;
import mdteam.ait.registry.datapack.Identifiable;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisConsole;
import mdteam.ait.tardis.control.impl.SecurityControl;
import mdteam.ait.tardis.data.properties.PropertiesHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class Control implements Identifiable {

    /*public static HashMap<Integer, ControlAnimationState> animationStates = HartnellAnimations.animationStatePerControl(listOfControlAnimations());

// Start animation for control with ID 0
    public void start(int id, int age) {
        animationStates.get(id).start(age);
    }

    public AnimationState getAnimationState(int id) {
        return animationStates.get(id).getAnimationState();
    }

    public Animation getAnimation(int id) {
        return animationStates.get(id).getAnimation();
    }

    public void startIfNotRunning(int id, int age) {
        animationStates.get(id).startIfNotRunning(age);
    }

// Stop animation for control with ID 1
    public void stop(int id) {
        animationStates.get(id).stop();
    }*/


	public String id; // a name to represent the control

	public Control(String id) {
		this.setId(id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Identifier id() {
		// Temporary - should be changed in future
		return new Identifier(AITMod.MOD_ID, this.getId());
	}

	public boolean runServer(Tardis tardis, ServerPlayerEntity player, ServerWorld world) {
		return false;
	}

	public boolean runServer(Tardis tardis, ServerPlayerEntity player, ServerWorld world, boolean leftClick) {
		return runServer(tardis, player, world);
	}

	public boolean runServer(Tardis tardis, ServerPlayerEntity player, ServerWorld world, boolean leftClick, TardisConsole console) {
		return runServer(tardis, player, world, leftClick);
	}

	public void addToControlSequence(Tardis tardis) {
		tardis.getHandlers().getSequenceHandler().add(this);
	}

	public SoundEvent getSound() {
		return SoundEvents.BLOCK_NOTE_BLOCK_BIT.value();
	}

	public boolean shouldFailOnNoPower() {
		return true;
	}

	public void runAnimation(Tardis tardis, ServerPlayerEntity player, ServerWorld world) {
		// no animation
	}

	@Override
	public String toString() {
		return "Control{" +
				"id='" + id + '\'' +
				'}';
	}

	public long getDelayLength() {
		return 250L;
	}

	public boolean shouldHaveDelay() {
		return true;
	}

	public boolean shouldHaveDelay(Tardis tardis) {
		return this.shouldHaveDelay();
	}

	public boolean ignoresSecurity() {
		return false;
	}

	public static String createDelayId(Control control, Tardis tardis) {
		return "delay-" + control.id + "-" + tardis.getUuid();
	}

	public static void createDelay(Control control, Tardis tardis, long millis) {
		DeltaTimeManager.createDelay(createDelayId(control, tardis), millis);
	}
	public static void createDelay(Control control, Tardis tardis) {
		createDelay(control, tardis, control.getDelayLength());
	}

	public static boolean isOnDelay(Control control, Tardis tardis) {
		return DeltaTimeManager.isStillWaitingOnDelay(createDelayId(control, tardis));
	}

	public boolean canRun(Tardis tardis, ServerPlayerEntity user) {
		if ((this.shouldFailOnNoPower() && !tardis.hasPower()) || tardis.getHandlers().getSequenceHandler().isConsoleDisabled()) {
			return false;
		}

		if (isOnDelay(this, tardis)) return false;

		boolean security = PropertiesHandler.getBool(tardis.getHandlers().getProperties(), SecurityControl.SECURITY_KEY);
		if (!this.ignoresSecurity() && security) {
			return SecurityControl.hasMatchingKey(user, tardis);
		}

		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Control control = (Control) o;

		return getId().equals(control.getId());
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}
}
