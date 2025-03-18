package dev.amble.ait.data;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dev.amble.ait.core.tardis.Tardis;
import org.jetbrains.annotations.Nullable;

public class TardisMap<T extends Tardis> extends ConcurrentHashMap<UUID, T> {

    @Nullable
    public T get(UUID id) {
        if (id == null)
            return null;

        return super.get(id);
    }

    public T put(T t) {
        return this.put(t.getUuid(), t);
    }
}
