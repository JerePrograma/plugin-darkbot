package lol.same.pvptest.pvp;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;

import java.util.Random;

public class WeaponSelector {
    private final HeroAPI hero;
    private final HeroItemsAPI items;
    private final Random random;
    private long lastUCB100Use;
    private long lastRCB140Use;
    private long lastPLD8Use;

    private static final SelectableItem.Laser[] NPC_WEAPONS = {
            SelectableItem.Laser.LCB_10,
            SelectableItem.Laser.MCB_25,
            SelectableItem.Laser.MCB_50,
            SelectableItem.Laser.UCB_100,
            SelectableItem.Laser.RSB_75,
            SelectableItem.Laser.A_BL,
            SelectableItem.Laser.RCB_140
    };

    public WeaponSelector(PluginAPI plugin) {
        this.hero = plugin.requireAPI(HeroAPI.class);
        this.items = plugin.requireAPI(HeroItemsAPI.class);
        this.random = new Random();
        this.lastUCB100Use = 0;
        this.lastRCB140Use = 0;
        this.lastPLD8Use = 0;
    }

    public void selectWeapon(Object target) {
        if (target == null) return;

        long currentTime = System.currentTimeMillis();

        if (target instanceof Player) {
            if (currentTime - lastRCB140Use >= 3000) {
                if (random.nextBoolean()) {
                    useWeapon(SelectableItem.Laser.RCB_140); // Usar RCB140
                } else {
                    useWeapon(SelectableItem.Laser.RSB_75); // Usar RSB-75
                }
                lastRCB140Use = currentTime;
            }
        } else if (target instanceof Npc) {
            Npc npc = (Npc) target;
            if (!npc.getEntityInfo().getUsername().toLowerCase().contains("behemoth")) {
                useWeapon(selectRandomNpcWeapon());
            }
        }

        if (currentTime - lastPLD8Use >= 4000) {
            useMissile(SelectableItem.Rocket.PLD_8); // Usar PLD8 cada 4 segundos
            lastPLD8Use = currentTime;
        } else {
            useDefaultMissile(); // Usar misiles por defecto
        }
    }

    private SelectableItem.Laser selectRandomNpcWeapon() {
        return NPC_WEAPONS[random.nextInt(NPC_WEAPONS.length)];
    }

    private void useWeapon(SelectableItem.Laser laser) {
        var result = items.useItem(laser);
        if (result.isSuccessful()) {
            System.out.println("Cambiando a munición: " + laser.getId());
        } else {
            System.err.println("Error al cambiar a munición: " + laser.getId());
        }
    }

    private void useMissile(SelectableItem.Rocket rocket) {
        var result = items.useItem(rocket);
        if (result.isSuccessful()) {
            System.out.println("Disparando misil: " + rocket.getId());
        } else {
            System.err.println("Error al disparar misil: " + rocket.getId());
        }
    }

    private void useDefaultMissile() {
        SelectableItem.Rocket defaultMissile = random.nextBoolean() ? SelectableItem.Rocket.PLT_2026 : SelectableItem.Rocket.PLT_2021;
        var result = items.useItem(defaultMissile);
        if (result.isSuccessful()) {
            System.out.println("Disparando misil por defecto: " + defaultMissile.getId());
        } else {
            System.err.println("Error al disparar misil por defecto: " + defaultMissile.getId());
        }
    }
}