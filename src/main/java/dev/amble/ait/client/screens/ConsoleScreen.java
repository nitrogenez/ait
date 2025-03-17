package dev.amble.ait.client.screens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import dev.amble.ait.client.tardis.ClientTardis;

public abstract class ConsoleScreen extends TardisScreen {

    protected final BlockPos console;

    protected ConsoleScreen(Text title, ClientTardis tardis, BlockPos console) {
        super(title, tardis);

        this.console = console;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == MinecraftClient.getInstance().options.inventoryKey.getDefaultKey().getCode()) {
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public BlockPos getConsole() {
        return console;
    }
}
