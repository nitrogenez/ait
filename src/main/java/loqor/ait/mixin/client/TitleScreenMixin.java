package loqor.ait.mixin.client;

import static loqor.ait.core.AITItems.isInAdvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import loqor.ait.AITMod;

@Mixin(value = TitleScreen.class, priority = 999)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Unique private static boolean isChristmas = isInAdvent();

    @Unique private static final RotatingCubeMapRenderer NEWPANO = new RotatingCubeMapRenderer(
            isChristmas
                    ? new CubeMapRenderer(new Identifier(AITMod.MOD_ID, "textures/gui/title/background/advent/panorama"))
                    : new CubeMapRenderer(new Identifier(AITMod.MOD_ID, "textures/gui/title/background/panorama"))
    );


    // This modifies the panorama in the background
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/RotatingCubeMapRenderer;render(FF)V", ordinal = 0))
    private void something(RotatingCubeMapRenderer instance, float delta, float alpha) {
        boolean isConfigEnabled = AITMod.AIT_CONFIG.CUSTOM_MENU();
        if (isConfigEnabled)
            NEWPANO.render(delta, alpha);
        else
            instance.render(delta, alpha);
    }
}
