package dev.pavatus.multidim.mixin;

import java.util.List;
import java.util.Map;

import com.mojang.serialization.Lifecycle;
import dev.pavatus.multidim.api.MutableRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements MutableRegistry<T> {

    @Shadow @Final private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;

    @Shadow @Final private Map<T, RegistryEntry.Reference<T>> valueToEntry;

    @Shadow @Final private Object2IntMap<T> entryToRawId;

    @Shadow @Final private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;

    @Shadow @Final private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;

    @Shadow @Final private Map<T, Lifecycle> entryToLifecycle;

    @Shadow @Nullable private List<RegistryEntry.Reference<T>> cachedEntries;

    @Shadow private boolean frozen;

    @Override
    public boolean multidim$remove(T entry) {
        RegistryEntry.Reference<T> registryEntry = this.valueToEntry.get(entry);
        int rawId = this.entryToRawId.removeInt(entry);

        if (rawId == -1)
            return false;

        try {
            this.rawIdToEntry.set(rawId, null);

            this.idToEntry.remove(registryEntry.registryKey().getValue());
            this.keyToEntry.remove(registryEntry.registryKey());

            this.entryToLifecycle.remove(entry);
            this.valueToEntry.remove(entry);

            if (this.cachedEntries != null)
                this.cachedEntries.remove(registryEntry);

            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean multidim$remove(Identifier key) {
        RegistryEntry.Reference<T> entry = this.idToEntry.get(key);
        return entry != null && entry.hasKeyAndValue() && this.multidim$remove(entry.value());
    }

    @Override
    public void multidim$freeze() {
        this.frozen = true;
    }

    @Override
    public void multidim$unfreeze() {
        this.frozen = false;
    }

    @Override
    public boolean multidim$isFrozen() {
        return this.frozen;
    }
}
