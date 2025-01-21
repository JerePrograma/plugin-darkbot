package lol.same.pvptest;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.managers.EntitiesAPI;

import java.util.Collection;
import java.util.Random;

public class PlayerEquipment {
    private final Collection<? extends Player> players;
    public final int id;

    public PlayerEquipment(PluginAPI plugin) {
        var entities = plugin.requireAPI(EntitiesAPI.class);
        players = entities.getPlayers();
        id = new Random().nextInt();
    }
}
