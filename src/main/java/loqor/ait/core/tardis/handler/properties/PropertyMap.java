package loqor.ait.tardis.handler.properties;

import java.util.HashMap;

import loqor.ait.api.Disposable;

public class PropertyMap extends HashMap<String, Value<?>> implements Disposable {

    @SuppressWarnings("unchecked")
    public <T> Value<T> getExact(String key) {
        return (Value<T>) this.get(key);
    }

    @Override
    public void dispose() {
        for (Value<?> value : this.values()) {
            value.dispose();
        }

        this.clear();
    }
}