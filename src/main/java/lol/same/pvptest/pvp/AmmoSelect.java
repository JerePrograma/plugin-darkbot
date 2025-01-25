package lol.same.pvptest.pvp;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.game.enums.EntityEffect;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import lol.same.pvptest.pvp.config.AmmoConfig;

import java.util.Random;

/**
 * Clase que maneja la selección de armas y misiles en combate.
 */
public class AmmoSelect implements Configurable<AmmoConfig> {
    private AmmoConfig config;

    private final HeroAPI hero;
    private final HeroItemsAPI items;
    private final Random random;
    private long lastRSB75Use;
    private long lastRCB140Use;
    private long lastPLD8Use;
    private long lastRIC3Use;

    @Override
    public void setConfig(ConfigSetting<AmmoConfig> configSetting) {
        this.config = configSetting.getValue();
    }

    public AmmoSelect(PluginAPI plugin) {
        this.hero = plugin.requireAPI(HeroAPI.class);
        this.items = plugin.requireAPI(HeroItemsAPI.class);
        this.random = new Random();
        this.lastRSB75Use = 0;
        this.lastRCB140Use = 0;
        this.lastPLD8Use = 0;
        this.lastRIC3Use = 0;
    }

    /**
     * Selecciona el arma o misil adecuado según el objetivo y las configuraciones.
     *
     * @param target El objetivo al que se está atacando.
     */
    public void selectWeapon(Object target) {
        if (target == null) return;

        long currentTime = System.currentTimeMillis();

        if (target instanceof Player) {
            // Uso de munición para jugadores
            if (config.rsb && currentTime - lastRSB75Use >= 3000) {
                useWeapon(SelectableItem.Laser.RSB_75);
                lastRSB75Use = currentTime;
            } else if (config.ucb) {
                useWeapon(SelectableItem.Laser.UCB_100);
            } else if (config.rcb && currentTime - lastRCB140Use >= 3000) {
                useWeapon(SelectableItem.Laser.RCB_140);
                lastRCB140Use = currentTime;
            } else if (config.pib && !hero.hasEffect(EntityEffect.INFECTION)) {
                useWeapon(SelectableItem.Laser.PIB_100);
            }

            // Uso de misiles para jugadores
            if (config.usePLD8 && currentTime - lastPLD8Use >= config.pld8Cooldown * 1000) {
                useMissile(SelectableItem.Rocket.PLD_8);
                lastPLD8Use = currentTime;
            } else if (config.useRIC3 && currentTime - lastRIC3Use >= config.ric3Cooldown * 1000 && targetIsMovingAway((Player) target)) {
                useMissile(SelectableItem.Rocket.R_IC3);
                lastRIC3Use = currentTime;
            } else {
                useDefaultMissile();
            }
        } else if (target instanceof Npc) {
            // Uso de munición para NPC,
            Npc npc = (Npc) target;
            if (npc.getEntityInfo().getUsername().toLowerCase().contains("mindfire behemoth") && config.abl) {
                useWeapon(SelectableItem.Laser.A_BL);
            } else if (config.rsb && currentTime - lastRSB75Use >= 3000) {
                useWeapon(SelectableItem.Laser.RSB_75);
                lastRSB75Use = currentTime;
            } else if (config.ucb) {
                useWeapon(SelectableItem.Laser.UCB_100);
            } else if (config.rcb && currentTime - lastRCB140Use >= 3000) {
                useWeapon(SelectableItem.Laser.RCB_140);
                lastRCB140Use = currentTime;
            }
        }
    }

    /**
     * Usa el arma especificada.
     *
     * @param laser El arma a usar.
     */
    private void useWeapon(SelectableItem.Laser laser) {
        var result = items.useItem(laser);
        if (result.isSuccessful()) {
            System.out.println("Cambiando a munición: " + laser.getId());
        } else {
            System.err.println("Error al cambiar a munición: " + laser.getId());
        }
    }

    /**
     * Usa el misil especificado.
     *
     * @param rocket El misil a usar.
     */
    private void useMissile(SelectableItem.Rocket rocket) {
        var result = items.useItem(rocket);
        if (result.isSuccessful()) {
            System.out.println("Disparando misil: " + rocket.getId());
        } else {
            System.err.println("Error al disparar misil: " + rocket.getId());
        }
    }

    /**
     * Usa el misil por defecto (PLT-2026 o PLT-2021).
     */
    private void useDefaultMissile() {
        SelectableItem.Rocket defaultMissile = random.nextBoolean() ? SelectableItem.Rocket.PLT_2026 : SelectableItem.Rocket.PLT_2021;
        var result = items.useItem(defaultMissile);
        if (result.isSuccessful()) {
            System.out.println("Disparando misil por defecto: " + defaultMissile.getId());
        } else {
            System.err.println("Error al disparar misil por defecto: " + defaultMissile.getId());
        }
    }

    /**
     * Determina si el objetivo se está alejando del héroe.
     *
     * @param target El objetivo a evaluar.
     * @return true si el objetivo se está alejando, false en caso contrario.
     */
    private boolean targetIsMovingAway(Player target) {
        // Implementa la lógica para determinar si el objetivo se está alejando
        // Puedes usar la velocidad y la dirección del objetivo en comparación con la posición del héroe
        return target.getLocationInfo().distanceTo(hero) > 600; // Ejemplo simple
    }
}