package lol.same.pvptest.pvp;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.enums.EntityEffect;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import lol.same.pvptest.pvp.config.AmmoConfig;

import java.util.Locale;
import java.util.Random;

/**
 * Clase que maneja la selección de armas y misiles en combate basado en AmmoConfig.
 */
public class AmmoSelect { // Remover "implements Configurable<AmmoConfig>"
    private AmmoConfig config;

    private final HeroAPI hero;
    private final HeroItemsAPI items;
    private final Random random;
    private long lastRSB75Use;
    private long lastRCB140Use;
    private long lastPLD8Use;
    private long lastRIC3Use;

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
     * Establece la configuración de Ammo.
     *
     * @param config La configuración de Ammo.
     */
    public void setConfig(AmmoConfig config) {
        this.config = config;
    }

    /**
     * Selecciona el arma o misil adecuado según el objetivo y las configuraciones.
     *
     * @param target El objetivo al que se está atacando.
     */
    public void selectWeapon(Object target) {
        if (target == null) return;

        if (target instanceof Player) {
            Player player = (Player) target;
            handlePlayerWeapons(player);
        } else if (target instanceof Npc) {
            Npc npc = (Npc) target;
            handleNpcWeapons(npc);
        }
    }

    /**
     * Maneja el uso de armas contra jugadores.
     *
     * @param player El jugador objetivo.
     */
    private void handlePlayerWeapons(Player player) {
        long currentTime = System.currentTimeMillis();
        // Uso de municiones específicas
        if (config.rsb && (currentTime - lastRSB75Use >= 3000)) {
            useWeapon(SelectableItem.Laser.RSB_75);
            lastRSB75Use = currentTime;
        } else if (config.ucb) {
            useWeapon(SelectableItem.Laser.UCB_100);
        }

        if (config.rcb && (currentTime - lastRCB140Use >= 3000)) {
            useWeapon(SelectableItem.Laser.RCB_140);
            lastRCB140Use = currentTime;
        }

        if (config.pib && !hero.hasEffect(EntityEffect.INFECTION)) {
            useWeapon(SelectableItem.Laser.PIB_100);
        }

        // Uso de PEM-01
        if (config.enablePem && isPemAvailable()) {
            useWeapon(config.pemItem);
        }
    }

    /**
     * Maneja el uso de armas contra NPCs específicos.
     *
     * @param npc El NPC objetivo.
     */
    private void handleNpcWeapons(Npc npc) {
        long currentTime = System.currentTimeMillis();
        if (npc.getEntityInfo().getUsername().toLowerCase(Locale.ROOT).contains("mindfire behemoth") && config.abl) {
            useWeapon(SelectableItem.Laser.A_BL);
        } else if (config.rsb && (currentTime - lastRSB75Use >= 3000)) {
            useWeapon(SelectableItem.Laser.RSB_75);
            lastRSB75Use = currentTime;
        } else if (config.ucb) {
            useWeapon(SelectableItem.Laser.UCB_100);
        } else if (config.rcb && (currentTime - lastRCB140Use >= 3000)) {
            useWeapon(SelectableItem.Laser.RCB_140);
            lastRCB140Use = currentTime;
        }

        // Uso de PEM-01 para NPCs si es necesario
        if (config.enablePem && isPemAvailable()) {
            useWeapon(config.pemItem);
        }
    }

    /**
     * Verifica si PEM-01 está disponible en el inventario.
     *
     * @return true si PEM-01 está disponible, false en caso contrario.
     */
    public boolean isPemAvailable() {
        return config.enablePem && items.getItem(config.pemItem).isPresent();
    }

    /**
     * Usa el arma especificada.
     *
     * @param item El arma a usar.
     */
    private void useWeapon(SelectableItem item) { // Cambiar el tipo a SelectableItem
        try {
            var result = items.useItem(item);
            if (result.isSuccessful()) {
                System.out.println("Cambiando a munición: " + item.getId());
            } else {
                System.err.println("Error al cambiar a munición: " + item.getId());
            }
        } catch (Exception e) {
            System.err.println("Excepción al usar arma: " + e.getMessage());
        }
    }

    /**
     * Usa el misil especificado.
     *
     * @param rocket El misil a usar.
     */
    private void useMissile(SelectableItem.Rocket rocket) {
        try {
            var result = items.useItem(rocket);
            if (result.isSuccessful()) {
                System.out.println("Disparando misil: " + rocket.getId());
            } else {
                System.err.println("Error al disparar misil: " + rocket.getId());
            }
        } catch (Exception e) {
            System.err.println("Excepción al disparar misil: " + e.getMessage());
        }
    }

    /**
     * Usa el misil por defecto (PLT-2026 o PLT-2021).
     */
    private void useDefaultMissile() {
        SelectableItem.Rocket defaultMissile = random.nextBoolean() ? SelectableItem.Rocket.PLT_2026 : SelectableItem.Rocket.PLT_2021;
        try {
            var result = items.useItem(defaultMissile);
            if (result.isSuccessful()) {
                System.out.println("Disparando misil por defecto: " + defaultMissile.getId());
            } else {
                System.err.println("Error al disparar misil por defecto: " + defaultMissile.getId());
            }
        } catch (Exception e) {
            System.err.println("Excepción al disparar misil por defecto: " + e.getMessage());
        }
    }
}
