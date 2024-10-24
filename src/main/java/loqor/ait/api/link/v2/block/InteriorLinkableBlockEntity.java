package loqor.ait.api.link.v2.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import loqor.ait.core.tardis.Tardis;
import loqor.ait.core.tardis.dim.TardisDimension;

public abstract class InteriorLinkableBlockEntity extends AbstractLinkableBlockEntity {

    public InteriorLinkableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);

        if (this.ref != null)
            return;

        Tardis tardis = TardisDimension.get(world).orElse(null);

        if (tardis == null)
            return;

        this.link(tardis);
    }
}
