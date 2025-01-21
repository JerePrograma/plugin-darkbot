package lol.same.pvptest;

import eu.darkbot.api.extensions.Behavior;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.entities.Portal;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.GroupAPI;
import eu.darkbot.api.managers.StarSystemAPI;
import lol.same.pvptest.utils.LogIfChanged;

import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;

@Feature(name = "Player Map Tracker",
        description = "Recuerda en que mapa fueron vistos los jugadores por ultima vez",
        enabledByDefault = true)
public class PlayerMapTracker implements Behavior {
    private final static Map<Integer, Integer> playersLastMapId = new HashMap<>();
    private final static Map<Integer, Location> playersLastLocation = new HashMap<>();

    private final StarSystemAPI starSystem;
    private final GroupAPI groups;
    private final Collection<? extends Player> players;
    private final Collection<? extends Portal> portals;
    private long lastTickTime;
    private int lastTickGameMap;
    private final Collection<Player> nearPortalsLastTick;

    private static final List<Integer> debugLogMapOfThesePlayers = new ArrayList<>();

    public PlayerMapTracker(StarSystemAPI starSystem, GroupAPI groups, EntitiesAPI entities) {
        this.starSystem = starSystem;
        this.groups = groups;
        this.players = entities.getPlayers();
        this.portals = entities.getPortals();
        this.nearPortalsLastTick = new ArrayList<>();
    }

    @Override
    public void onTickBehavior() {
        var longPause = System.nanoTime() - lastTickTime > SECONDS.toNanos(1);
        lastTickTime = System.nanoTime();
        var currentMap = starSystem.getCurrentMap();
        var changedMap = lastTickGameMap != currentMap.getId();
        lastTickGameMap = currentMap.getId();

        if (!longPause && !changedMap)
            for (var player: nearPortalsLastTick)
                if (!player.isValid())
                    portals.stream()
                            .filter(p -> p.distanceTo(player) < 1000)
                            .findAny()
                            .flatMap(Portal::getTargetMap)
                            .ifPresent(destination -> playersLastMapId.put(player.getId(), destination.getId()));
        nearPortalsLastTick.clear();

        for (var player: players) {
            if (!player.isValid())
                System.out.println("players contains invalid player");
            playersLastMapId.put(player.getId(), currentMap.getId());
            PlayerNames.setName(player.getId(), player.getEntityInfo().getUsername());
            if (portals.stream().anyMatch(p -> p.distanceTo(player) < 1000))
                nearPortalsLastTick.add(player);
        }

        if (groups.hasGroup())
            for (var member: groups.getMembers()) {
                playersLastMapId.put(member.getId(), member.getMapId());
                PlayerNames.setName(member.getId(), member.getUsername());
            }

        for (var player: debugLogMapOfThesePlayers)
            LogIfChanged.log("Mapa de " + player, starSystem.findMap(playersLastMapId.get(player))
                            .map(GameMap::getName)
                            .orElse("null"));
        debugLogMapOfThesePlayers.clear();
    }

    public static void updateLastPlayerLocation(int playerId, int mapId, Location location) {
        debugLogMapOfThesePlayers.add(playerId);
        playersLastMapId.put(playerId, mapId);
        playersLastLocation.put(playerId, location);
    }

    public static Optional<Integer> guessPlayerMapId(int playerId) {
        return Optional.ofNullable(playersLastMapId.get(playerId));
    }

    public static Optional<Location> guessPlayerLocation(int playerId) {
        return Optional.ofNullable(playersLastLocation.get(playerId));
    }
}
