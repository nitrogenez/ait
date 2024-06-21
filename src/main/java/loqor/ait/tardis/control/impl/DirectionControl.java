package loqor.ait.tardis.control.impl;

import loqor.ait.core.blocks.ExteriorBlock;
import loqor.ait.core.data.AbsoluteBlockPos;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.TardisTravel;
import loqor.ait.tardis.control.Control;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class DirectionControl extends Control {

	public DirectionControl() {
		super("direction");
	}

	@Override
	public boolean runServer(Tardis tardis, ServerPlayerEntity player, ServerWorld world, BlockPos console, boolean leftClick) {
		TardisTravel travel = tardis.travel();
		AbsoluteBlockPos.Directed dest = travel.getDestination();

		if (tardis.sequence().hasActiveSequence() && tardis.sequence().controlPartOfSequence(this)) {
			this.addToControlSequence(tardis, player, console);
			return false;
		}

		int rotation = dest.getRotation();
		rotation = leftClick ? getPreviousGeneralizedRotation(rotation)
				: getNextGeneralizedRotation(rotation);

		rotation = wrap(rotation, ExteriorBlock.MAX_ROTATION_INDEX);

		travel.setDestination(new AbsoluteBlockPos.Directed(dest, rotation), false);
		messagePlayer(player, getNextGeneralizedRotation(dest.getRotation()));
		return true;
	}

	//↖ ↑ ↗
	//← · →
	//↙ ↓ ↘

	private void messagePlayer(ServerPlayerEntity player, int rotation) {
		String arrow = rotationForArrow(rotation);
		player.sendMessage(Text.literal("Rotation Direction: " + rotationToDirection(rotation).substring(0, 1).toUpperCase() + rotationToDirection(rotation).substring(1) + " | " + arrow), true); // fixme translatable is preferred
	}

	public static String rotationToDirection(int currentRot) {
		return switch (currentRot) {
			case 1, 2, 3 -> "North East";
			case 4 -> "East";
			case 5, 6, 7 -> "South East";
			case 8 -> "South";
			case 9, 10, 11 -> "South West";
			case 12 -> "West";
			case 13, 14, 15, 16 -> "North West";
			default -> "North";
		};
	}

	public static int getNextGeneralizedRotation(int rotation) {
		return (rotation + 2) % 16;
	}

	public static int getPreviousGeneralizedRotation(int rotation) {
		return (rotation - 2) % 16;
	}

	public static int getGeneralizedRotation(int rotation) {
		if (rotation % 2 != 0 && rotation < 15) {
			return rotation + 1;
		} else if (rotation == 15) {
			return 0;
		}
		return rotation;
	}

	public static String rotationForArrow(int currentRot) {
		return switch (currentRot) {
			case 1, 2, 3 -> "↗";
			case 4 -> "→";
			case 5, 6, 7 -> "↘";
			case 8 -> "↓";
			case 9, 10, 11 -> "↙";
			case 12 -> "←";
			case 13, 14, 15 -> "↖";
			default -> "↑";
		};
	}

	public static int wrap(int value, int max) {
		return (value % max + max) % max;
	}
}
