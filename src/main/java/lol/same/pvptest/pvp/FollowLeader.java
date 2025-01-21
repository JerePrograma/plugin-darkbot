package lol.same.pvptest.pvp;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.group.GroupMember;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.api.managers.*;
import eu.darkbot.shared.utils.MapTraveler;
import lol.same.pvptest.PlayerMapTracker;
import lol.same.pvptest.utils.LocationAndMap;
import lol.same.pvptest.utils.LogIfChanged;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class FollowLeader {
    private final GroupAPI group;
    private final StarSystemAPI starSystem;
    private final HeroAPI hero;
    private final MovementAPI movement;
    private final I18nAPI i18n;
    private final Collection<? extends Player> players;
    private final ConfigSetting<Integer> workingMap;
    private final MapTraveler traveler;

    private int leaderId;
    private @Nullable Player leader;
    private @Nullable GroupMember leaderGroupMember;
    private boolean selfIsLeader;

    public FollowLeader(PluginAPI plugin) {
        this.group = plugin.requireAPI(GroupAPI.class);
        this.starSystem = plugin.requireAPI(StarSystemAPI.class);
        this.hero = plugin.requireAPI(HeroAPI.class);
        this.movement = plugin.requireAPI(MovementAPI.class);
        this.i18n = plugin.requireAPI(I18nAPI.class);

        var entities = plugin.requireAPI(EntitiesAPI.class);
        this.players = entities.getPlayers();

        var configApi = plugin.requireAPI(ConfigAPI.class);
        this.workingMap = configApi.requireConfig("general.working_map");
        this.traveler = plugin.requireInstance(MapTraveler.class);
    }

    /**
     * Determina quién es el líder según las prioridades, ya sea por config o por grupo.
     */
    public void determineLeader() {
        leaderId = 0;
        selfIsLeader = false;

        var leaderPriority = LeaderPriority.getHighestPriorityId();
        if (leaderPriority.isPresent()) {
            if (leaderPriority.get() == hero.getId()) {
                selfIsLeader = true;
                leader = null;
                leaderGroupMember = null;
                LogIfChanged.log("Líder", "Esta cuenta");
                return;
            }
            leaderId = leaderPriority.get();
            leaderGroupMember = (group.hasGroup())
                    ? group.getMembers().stream().filter(m -> m.getId() == leaderId).findAny().orElse(null)
                    : null;
            LogIfChanged.log("Líder", "De la config, id: " + leaderId);
        } else {
            leaderGroupMember = (group.hasGroup())
                    ? group.getMembers().stream().filter(GroupMember::isLeader).findAny().orElse(null)
                    : null;
            if (leaderGroupMember != null) {
                leaderId = leaderGroupMember.getId();
                LogIfChanged.log("Líder", "Del grupo, id: " + leaderId);
            }
        }

        if (leaderId == 0) {
            LogIfChanged.log("Líder", "Sin líder");
        }

        // Asignamos el "leader" Player si existe en la lista actual
        if (leader == null || !leader.isValid() || leader.getId() != leaderId) {
            var id = leaderId;
            leader = players.stream()
                    .filter(p -> p.getId() == id)
                    .findAny()
                    .orElse(null);
        }
    }

    /**
     * Llamado en cada tick desde el PvPModule para seguir al líder.
     * @param distance No lo usaremos prácticamente, porque queremos ir encima. Pero se mantiene para compatibilidad.
     * @return FollowLeaderResult indicando en qué estado estamos (y lo logueamos en PvPModule).
     */
    public FollowLeaderResult follow(int distance) {
        // 1) Tomar ubicación y mapa donde está (o creemos que está) el líder
        var leaderLocation = getLeaderLocation();

        // 2) Si estamos en el mapa equivocado, viajar a su mapa
        if (goToMap(leaderLocation.mapId)) {
            return FollowLeaderResult.GOING_TO_MAP;
        }

        // 3) Si ya estamos en su mapa, desplazarnos a su ubicación de forma continua
        if (goToLocation(leaderLocation.location)) {
            return FollowLeaderResult.GOING_TO_LOCATION;
        }

        // 4) Si no hay nada que hacer, quedamos IDLE
        return FollowLeaderResult.IDLE;
    }

    /**
     * @return ID del líder.
     */
    public int getLeaderId() {
        return leaderId;
    }

    /**
     * @return El Player que se considera líder, si está en la lista de EntitiesAPI.
     */
    public Optional<Player> getLeader() {
        return Optional.ofNullable(leader);
    }

    /**
     * @return El GroupMember líder, si existe en el grupo.
     */
    public Optional<GroupMember> getLeaderGroupMember() {
        return Optional.ofNullable(leaderGroupMember);
    }

    /**
     * @return true si esta cuenta es la líder (coincide con priority ID o es la "group leader").
     */
    public boolean isSelfLeader() {
        return selfIsLeader;
    }

    /**
     * @return Mensaje textual del MapTraveler sobre si está viajando al siguiente portal.
     */
    public String getMapTravelerStatus() {
        return traveler.current == null
                ? i18n.get("module.map_travel.status.no_next", traveler.target.getName())
                : i18n.get("module.map_travel.status.has_next", traveler.target.getName(),
                traveler.current.getTargetMap().map(GameMap::getName).orElse("unknown"));
    }

    /**
     * Obtiene la ubicación y el mapa del líder. Si el líder no está visible como Player, prueba a sacarlo
     * de la info del GroupMember, si no, de un tracker (PlayerMapTracker), etc.
     */
    private LocationAndMap getLeaderLocation() {
        if (leaderId == 0) {
            return new LocationAndMap(null, -1);
        }

        Location leaderLocation = null;
        int leaderMapId = -1;

        if (leader != null) {
            // Predicción muy baja para no quedarnos atrás
            leaderLocation = leader.getLocationInfo().destinationInTime(100);
            leaderMapId = starSystem.getCurrentMap().getId();
            LogIfChanged.log("Obteniendo ubicación", "De la nave visible");
        } else if (leaderGroupMember != null) {
            leaderLocation = leaderGroupMember.getLocation();
            leaderMapId = leaderGroupMember.getMapId();
            LogIfChanged.log("Obteniendo ubicación", "Del grupo");
        } else {
            leaderMapId = PlayerMapTracker.guessPlayerMapId(leaderId).orElse(-1);
            leaderLocation = PlayerMapTracker.guessPlayerLocation(leaderId).orElse(null);
            LogIfChanged.log("Obteniendo ubicación", "De la conexión");
        }

        LogIfChanged.log("Mapa del líder: ", "" + leaderMapId);
        return new LocationAndMap(leaderLocation, leaderMapId);
    }

    /**
     * En caso de que estemos en un mapa distinto, forzamos el seteo del working_map y usamos MapTraveler para llegar.
     * @return true si estamos viajando todavía al mapa, false si ya estamos en el mismo.
     */
    private boolean goToMap(int leaderMap) {
        if (leaderMap == -1) return false;

        if (leaderMap == starSystem.getCurrentMap().getId()) {
            // Ya estamos en el mismo mapa, no necesitamos viajar
            return false;
        }

        // Ajustamos el working map para forzar que el bot intente viajar a ese mapa
        workingMap.setValue(leaderMap);

        // Le decimos a traveler que vaya a ese mapa
        if (traveler.target == null || traveler.target.getId() != leaderMap) {
            traveler.setTarget(starSystem.findMap(leaderMap).orElse(null));
        }

        traveler.tick();
        return true; // seguimos viajando
    }

    /**
     * Lógica principal de acercamiento total.
     * @return true si estamos moviéndonos, false si ya no hace falta.
     */
    private boolean goToLocation(@Nullable Location location) {
        if (location == null) return false;

        double distance = location.distanceTo(hero);
        // Solo paramos si estamos prácticamente encima, por ejemplo a menos de 1 unidad
        if (distance < 1) {
            return false;
        }

        // Recalcular la posición predicha si hace falta
        Location destination = location;
        if (leader != null) {
            // Forzamos una predicción muy pequeña para que no se retrase
            destination = leader.getDestination().orElse(leader.getLocationInfo().destinationInTime(100));
        }

        // Usamos "radius = 0" para quedarnos EXACTAMENTE en esa posición
        int radius = 0;

        // Movemos la nave a esa posición
        movement.moveTo(Location.of(destination, destination.angleTo(hero), radius));

        return true;
    }
}
