package loqor.ait.api;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import loqor.ait.AITMod;
import loqor.ait.client.tardis.ClientTardis;
import loqor.ait.core.tardis.*;
import loqor.ait.core.tardis.ServerTardis;
import loqor.ait.core.tardis.control.impl.pos.IncrementManager;
import loqor.ait.core.tardis.control.sequences.SequenceHandler;
import loqor.ait.core.tardis.handler.landing.LandingPadHandler;
import loqor.ait.core.tardis.handler.loyalty.LoyaltyHandler;
import loqor.ait.core.tardis.handler.mood.MoodHandler;
import loqor.ait.core.tardis.handler.permissions.PermissionHandler;
import loqor.ait.core.tardis.handler.travel.TravelHandler;
import loqor.ait.core.tardis.manager.ServerTardisManager;
import loqor.ait.data.DirectedGlobalPos;
import loqor.ait.data.bsp.Exclude;
import loqor.ait.data.enummap.Ordered;

/**
 * Base class for all tardis components.
 *
 * @implNote There should be NO logic run in the constructor. If you need to
 * have such logic, implement it in an appropriate init method!
 */
public abstract class TardisComponent extends Initializable<TardisComponent.InitContext> implements Disposable {

    @Exclude protected Tardis tardis;

    @Exclude(strategy = Exclude.Strategy.NETWORK) private final IdLike id;

    /**
     * Do NOT under any circumstances run logic in this constructor. Default field
     * values should be inlined. All logic should be done in an appropriate init
     * method.
     *
     * @implNote The {@link TardisComponent#tardis()} will always be null at the
     * time this constructor gets called.
     */
    public TardisComponent(IdLike id) {
        this.id = id;
    }

    public void postInit(InitContext ctx) {
    }

    /**
     * Syncs this object and all its properties to the client.
     * @implNote Server-side only.
     */
    protected void sync() {
        if (this.isClient()) {
            AITMod.LOGGER.warn("Attempted to sync a component ON a client!", new IllegalAccessException());
            return;
        }

        ServerTardisManager.getInstance().markComponentDirty(this);
    }

    public Tardis tardis() {
        return this.tardis;
    }

    public IdLike getId() {
        return id;
    }

    public void setTardis(Tardis tardis) {
        this.tardis = tardis;
    }

    public boolean isClient() {
        return this.tardis() instanceof ClientTardis;
    }

    public boolean isServer() {
        return this.tardis() instanceof ServerTardis;
    }

    public TardisManager<?, ?> parentManager() {
        return TardisManager.getInstance(this.tardis);
    }

    @Override
    public void dispose() {
        this.tardis = null;
    }

    public static void init(TardisComponent component, Tardis tardis, InitContext context) {
        component.setTardis(tardis);
        Initializable.init(component, context);
    }

    public enum Id implements IdLike {
        // Base parts.
        DESKTOP(TardisDesktop.class, null),
        EXTERIOR(TardisExterior.class, null),
        HANDLERS(TardisHandlersManager.class, null),

        // Modular componenet "handlers"
        TRAVEL(TravelHandler.class, TravelHandler::new),
        DOOR(DoorHandler.class, DoorHandler::new),
        SONIC(SonicHandler.class, SonicHandler::new),
        PERMISSIONS(PermissionHandler.class, PermissionHandler::new),
        LOYALTY(LoyaltyHandler.class, LoyaltyHandler::new),
        ENGINE(EngineHandler.class, EngineHandler::new),
        FLIGHT(RealFlightHandler.class, RealFlightHandler::new),
        BIOME(BiomeHandler.class, BiomeHandler::new),
        SHIELDS(ShieldHandler.class, ShieldHandler::new),
        STATS(StatsHandler.class, StatsHandler::new),
        CRASH_DATA(TardisCrashHandler.class, TardisCrashHandler::new),
        WAYPOINTS(WaypointHandler.class, WaypointHandler::new),
        OVERGROWN(OvergrownHandler.class, OvergrownHandler::new),
        HUM(ServerHumHandler.class, ServerHumHandler::new),
        ALARMS(ServerAlarmHandler.class, ServerAlarmHandler::new),
        ENVIRONMENT(ExteriorEnvironmentHandler.class, ExteriorEnvironmentHandler::new),
        INTERIOR(InteriorChangingHandler.class, InteriorChangingHandler::new),
        SEQUENCE(SequenceHandler.class, SequenceHandler::new),
        MOOD(MoodHandler.class, MoodHandler::new),
        FUEL(FuelHandler.class, FuelHandler::new),
        HADS(HadsHandler.class, HadsHandler::new),
        SIEGE(SiegeHandler.class, SiegeHandler::new),
        CLOAK(CloakHandler.class, CloakHandler::new),
        INCREMENT(IncrementManager.class, IncrementManager::new),
        LANDING_PAD(LandingPadHandler.class, LandingPadHandler::new);

        private final Supplier<TardisComponent> creator;

        private final Class<? extends TardisComponent> clazz;

        private Integer index = null;

        @SuppressWarnings("unchecked")
        <T extends TardisComponent> Id(Class<T> clazz, Supplier<T> creator) {
            this.clazz = clazz;
            this.creator = (Supplier<TardisComponent>) creator;
        }

        @Override
        public Class<? extends TardisComponent> clazz() {
            return clazz;
        }

        @Override
        public void set(ClientTardis tardis, TardisComponent component) {
            switch (this) {
                case DESKTOP -> tardis.setDesktop((TardisDesktop) component);
                case EXTERIOR -> tardis.setExterior((TardisExterior) component);
                case HANDLERS -> {}
                default -> tardis.getHandlers().set(component);
            }
        }

        @Override
        public TardisComponent get(ClientTardis tardis) {
            return switch (this) {
                case DESKTOP -> tardis.getDesktop();
                case EXTERIOR -> tardis.getExterior();
                case HANDLERS -> tardis.getHandlers();
                default -> tardis.handler(this);
            };
        }

        @Override
        public TardisComponent create() {
            return this.creator.get();
        }

        @Override
        public boolean creatable() {
            return this.creator != null;
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public void index(int i) {
            this.index = i;
        }

        public static IdLike[] ids() {
            return Id.values();
        }
    }

    public interface IdLike extends Ordered {

        Class<? extends TardisComponent> clazz();

        default void set(ClientTardis tardis, TardisComponent component) {
            tardis.getHandlers().set(component);
        }

        default TardisComponent get(ClientTardis tardis) {
            return tardis.handler(this);
        }

        TardisComponent create();

        boolean creatable();

        String name();

        int index();

        void index(int i);
    }

    public static class AbstractId<T extends TardisComponent> implements IdLike {

        private final String name;
        private final Supplier<T> creator;
        private final Class<T> clazz;

        private int index;

        public AbstractId(String name, Supplier<T> creator, Class<T> clazz) {
            this.name = name;
            this.creator = creator;
            this.clazz = clazz;
        }

        @Override
        public Class<T> clazz() {
            return this.clazz;
        }

        @Override
        public TardisComponent create() {
            return this.creator.get();
        }

        @Override
        public boolean creatable() {
            return true;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public int index() {
            return this.index;
        }

        @Override
        public void index(int i) {
            this.index = i;
        }
    }

    public record InitContext(@Nullable DirectedGlobalPos.Cached pos,
                              boolean deserialized) implements Initializable.Context {

        public static InitContext createdAt(DirectedGlobalPos.Cached pos) {
            return new InitContext(pos, false);
        }

        public static InitContext deserialize() {
            return new InitContext(null, true);
        }

        @Override
        public boolean created() {
            return !deserialized;
        }
    }
}