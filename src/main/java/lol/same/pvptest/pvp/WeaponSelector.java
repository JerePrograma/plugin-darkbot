package lol.same.pvptest.pvp;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.enums.EntityEffect;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import lol.same.pvptest.pvp.config.PvPConfig;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Clase dedicada para la selección y uso de armas y municiones específicas (ISH-01 y PEM-01).
 */
public class WeaponSelector {
    private final HeroAPI hero;
    private final HeroItemsAPI items;
    private final PluginAPI plugin; // Referencia al PluginAPI
    private final Random random;

    // Cooldowns para armas y misiles (en milisegundos)
    private long lastUCB100Use;
    private long lastRCB140Use;
    private long lastRSB75Use;
    private long lastPLD8Use;
    private long lastRCI3Use;
    private long lastABLUse;
    private long lastPIBUse;
    private long lastISHUse;
    private long lastPEMUse;

    private static final SelectableItem.Laser[] NPC_WEAPONS = {
            SelectableItem.Laser.LCB_10,
            SelectableItem.Laser.MCB_25,
            SelectableItem.Laser.MCB_50,
            SelectableItem.Laser.UCB_100,
            SelectableItem.Laser.RSB_75,
            SelectableItem.Laser.A_BL,
            SelectableItem.Laser.RCB_140
    };

    // Condiciones y configuraciones recibidas de PvPConfig
    private Condition ishCondition;
    private Condition pemCondition;
    private SelectableItem.Special pemItem;

    // Constantes
    private static final double HEALTH_THRESHOLD = 0.5; // 50%
    private static final long COOLDOWN_MILLIS = 4000; // 4 segundos
    private static final long RSB75_COOLDOWN = 3000; // 3 segundos
    private static final long RCB140_COOLDOWN = 3000; // 3 segundos
    private static final long ABL_COOLDOWN = 3000; // 3 segundos
    private static final long PIB_COOLDOWN = 3000; // 3 segundos
    private static final long RCI3_COOLDOWN = 3000; // 3 segundos
    private static final long PLD8_COOLDOWN = 4000; // 4 segundos

    // Configuración
    private boolean enableUCB100 = true;
    private boolean enableRSB75 = true;
    private boolean enableRCB140 = true;
    private boolean enableABL = true;
    private boolean enablePIB = true;
    private boolean enableRCI3 = true;
    private boolean enablePLD8 = true;
    private boolean enableNpcWeapons = true;

    // Mapa para rastrear las ubicaciones anteriores de los jugadores
    private final Map<Integer, eu.darkbot.api.game.other.Location> previousPlayerLocations = new HashMap<>();

    // Infection effect ID
    private int infectionEffectId = EntityEffect.INFECTION.getId(); // Usar el ID del enum existente

    public WeaponSelector(PluginAPI plugin) {
        this.plugin = plugin;
        this.hero = plugin.requireAPI(HeroAPI.class);
        this.items = plugin.requireAPI(HeroItemsAPI.class);
        this.random = new Random();

        // Inicializar cooldowns
        this.lastUCB100Use = 0;
        this.lastRCB140Use = 0;
        this.lastRSB75Use = 0;
        this.lastPLD8Use = 0;
        this.lastRCI3Use = 0;
        this.lastABLUse = 0;
        this.lastPIBUse = 0;
        this.lastISHUse = 0;
        this.lastPEMUse = 0;
    }

    /**
     * Establece la condición para usar ISH-01.
     *
     * @param condition La condición a establecer.
     */
    public void setIshCondition(Condition condition) {
        this.ishCondition = condition;
    }

    /**
     * Establece la condición para usar PEM-01.
     *
     * @param condition La condición a establecer.
     */
    public void setPemCondition(Condition condition) {
        this.pemCondition = condition;
    }

    /**
     * Establece el ítem de PEM-01 a usar.
     *
     * @param pemItem El ítem de PEM-01.
     */
    public void setPemItem(SelectableItem.Special pemItem) {
        this.pemItem = pemItem;
    }

    /**
     * Establece las configuraciones de munición desde PvPConfig.
     *
     * @param config PvPConfig con las opciones habilitadas.
     */
    public void setAmmoConfig(PvPConfig config) {
        this.enableUCB100 = config.enableUCB100;
        this.enableRSB75 = config.enableRSB75;
        this.enableRCB140 = config.enableRCB140;
        this.enableABL = config.enableABL;
        this.enablePIB = config.enablePIB;
        this.enableRCI3 = config.enableRCI3;
        this.enablePLD8 = config.enablePLD8;
        this.enableNpcWeapons = config.enableNpcWeapons;

        // Establecer el ID del efecto de infección desde la configuración
        this.infectionEffectId = config.infectionEffect.getId();
    }

    /**
     * Selecciona y usa las armas o municiones según el tipo de objetivo.
     *
     * @param target El objetivo actual (Player o Npc).
     */
    public void selectWeapon(Object target) {
        if (target == null) return;

        long currentTime = System.currentTimeMillis();

        // Manejar el uso de Lasers según el tipo de objetivo
        if (target instanceof Player) {
            Player player = (Player) target;
            handlePlayerWeapons(player, currentTime);
        } else if (target instanceof Npc) {
            Npc npc = (Npc) target;
            handleNpcWeapons(npc, currentTime);
        }

        // Manejar el uso de Rockets
        handleRockets(currentTime, target instanceof Player);

        // Intentar usar ISH-01 o PEM-01
        boolean ishUsed = useISH(currentTime);
        if (!ishUsed) {
            usePEM(currentTime);
        }
    }

    /**
     * Maneja el uso de armas contra jugadores.
     *
     * @param player      El jugador objetivo.
     * @param currentTime El tiempo actual en milisegundos.
     */
    private void handlePlayerWeapons(Player player, long currentTime) {
        // Usar UCB-100 si está habilitado y no está en cooldown
        if (enableUCB100 && (currentTime - lastUCB100Use >= COOLDOWN_MILLIS)) {
            useWeapon(SelectableItem.Laser.UCB_100);
            lastUCB100Use = currentTime;
        }

        // Usar RSB-75 cada 3 segundos si está habilitado
        if (enableRSB75 && (currentTime - lastRSB75Use >= RSB75_COOLDOWN)) {
            useWeapon(SelectableItem.Laser.RSB_75);
            lastRSB75Use = currentTime;
        }

        // Usar RCB-140 cada 3 segundos si está habilitado
        if (enableRCB140 && (currentTime - lastRCB140Use >= RCB140_COOLDOWN)) {
            useWeapon(SelectableItem.Laser.RCB_140);
            lastRCB140Use = currentTime;
        }

        // Usar PIB si el jugador no tiene el efecto del láser (infection laser ammo)
        if (enablePIB && !playerHasInfectionLaserAmmo(player) && (currentTime - lastPIBUse >= PIB_COOLDOWN)) {
            useWeapon(SelectableItem.Laser.PIB_100); // Usar PIB_100
            lastPIBUse = currentTime;
        }
    }

    /**
     * Verifica si el jugador tiene el efecto de infección seleccionado.
     *
     * @param player El jugador a verificar.
     * @return true si tiene el efecto, false en caso contrario.
     */
    private boolean playerHasInfectionLaserAmmo(Player player) {
        // Verifica si el jugador tiene el efecto de infección configurado
        return player.hasEffect(infectionEffectId);
    }

    /**
     * Maneja el uso de armas contra NPCs específicos.
     *
     * @param npc         El NPC objetivo.
     * @param currentTime El tiempo actual en milisegundos.
     */
    private void handleNpcWeapons(Npc npc, long currentTime) {
        // Verificar si el NPC es un "mindifire behemonth"
        if (enableABL && npc.getEntityInfo().getUsername().toLowerCase(Locale.ROOT).contains("mindifire behemonth")) {
            if (currentTime - lastABLUse >= ABL_COOLDOWN) {
                useWeapon(SelectableItem.Laser.A_BL);
                lastABLUse = currentTime;
            }
        }

        // Usar un arma aleatoria para otros NPCs
        if (enableNpcWeapons) {
            useWeapon(selectRandomNpcWeapon());
        }
    }

    /**
     * Maneja el uso de Rockets.
     *
     * @param currentTime El tiempo actual en milisegundos.
     * @param isPlayer    Si el objetivo es un jugador.
     */
    private void handleRockets(long currentTime, boolean isPlayer) {
        if (isPlayer) {
            // Usar PLD-8 cada 4 segundos si está habilitado y no está en cooldown
            if (enablePLD8 && (currentTime - lastPLD8Use >= PLD8_COOLDOWN)) {
                useMissile(SelectableItem.Rocket.PLD_8);
                lastPLD8Use = currentTime;
            }

            // Usar RCI-3 si el jugador se aleja y no está en cooldown
            Player player = (Player) hero.getLocalTargetAs(Player.class);
            if (enableRCI3 && player != null && isPlayerMovingAway(player) && (currentTime - lastRCI3Use >= RCI3_COOLDOWN)) {
                useMissile(SelectableItem.Rocket.R_IC3); // Usar R_IC3
                lastRCI3Use = currentTime;
            }
        } else {
            // Si no es un jugador, usar misiles por defecto
            useDefaultMissile();
        }
    }

    /**
     * Determina si el jugador se está alejando del héroe.
     *
     * @param player El jugador a verificar.
     * @return true si se está alejando, false en caso contrario.
     */
    /**
     * Determina si el jugador se está alejando del héroe.
     *
     * @param player El jugador a verificar.
     * @return true si se está alejando, false en caso contrario.
     */
    private boolean isPlayerMovingAway(Player player) {
        // Obtener la ubicación previa del jugador
        eu.darkbot.api.game.other.Location previousLocation = previousPlayerLocations.get(player.getId());

        // Obtener las coordenadas actuales del jugador
        double currentX = player.getLocationInfo().getX();
        double currentY = player.getLocationInfo().getY();

        // Obtener las coordenadas actuales del héroe
        double heroX = hero.getLocationInfo().getX();
        double heroY = hero.getLocationInfo().getY();

        // Crear objetos Location actuales usando Location.of()
        eu.darkbot.api.game.other.Location currentLocation = Location.of(currentX, currentY);
        eu.darkbot.api.game.other.Location heroLocation = Location.of(heroX, heroY);

        // Si no hay una ubicación previa registrada, almacenarla y retornar false
        if (previousLocation == null) {
            previousPlayerLocations.put(player.getId(), currentLocation);
            return false;
        }

        // Calcular las distancias anterior y actual entre el jugador y el héroe
        double previousDistance = previousLocation.distanceTo(heroLocation);
        double currentDistance = currentLocation.distanceTo(heroLocation);

        // Actualizar la ubicación previa con la ubicación actual
        previousPlayerLocations.put(player.getId(), currentLocation);

        // Retornar true si la distancia actual es mayor que la anterior (el jugador se está alejando)
        return currentDistance > previousDistance;
    }


    /**
     * Intenta usar ISH-01 si está disponible y se cumplen las condiciones.
     *
     * @param currentTime El tiempo actual en milisegundos.
     * @return true si ISH-01 fue usado correctamente, false en caso contrario.
     */
    public boolean useISH(long currentTime) {
        // Verificar cooldown para ISH-01
        if (currentTime - lastISHUse < COOLDOWN_MILLIS) {
            // Aún en cooldown
            return false;
        }

        // Verificar cooldown para PEM-01 para evitar uso simultáneo
        if (currentTime - lastPEMUse < COOLDOWN_MILLIS) {
            // PEM-01 fue usado recientemente, esperar
            return false;
        }

        // Verificar condiciones para usar ISH-01
        if (ishCondition != null && ishCondition.get(plugin) == Condition.Result.ALLOW) {
            try {
                var result = items.useItem(SelectableItem.Special.ISH_01);
                if (result.isSuccessful()) {
                    System.out.println("Usando munición: " + SelectableItem.Special.ISH_01.getId());
                    lastISHUse = currentTime;
                    return true;
                } else {
                    System.err.println("Error al usar munición: " + SelectableItem.Special.ISH_01.getId());
                }
            } catch (Exception e) {
                System.err.println("Excepción al usar ISH-01: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Intenta usar PEM-01 si está disponible y se cumplen las condiciones.
     *
     * @param currentTime El tiempo actual en milisegundos.
     * @return true si PEM-01 fue usado correctamente, false en caso contrario.
     */
    public boolean usePEM(long currentTime) {
        // Verificar cooldown para PEM-01
        if (currentTime - lastPEMUse < COOLDOWN_MILLIS) {
            // Aún en cooldown
            return false;
        }

        // Verificar cooldown para ISH-01 para evitar uso simultáneo
        if (currentTime - lastISHUse < COOLDOWN_MILLIS) {
            // ISH-01 fue usado recientemente, esperar
            return false;
        }

        // Verificar si PEM-01 está disponible en el inventario
        if (pemItem == null || !isPemAvailable()) {
            System.err.println("PEM-01 no está disponible en el inventario.");
            return false;
        }

        // Verificar condiciones para usar PEM-01
        if (pemCondition != null && pemCondition.get(plugin) == Condition.Result.ALLOW) {
            try {
                var result = items.useItem(pemItem);
                if (result.isSuccessful()) {
                    System.out.println("Usando munición: " + pemItem.getId());
                    lastPEMUse = currentTime;
                    return true;
                } else {
                    System.err.println("Error al usar munición: " + pemItem.getId());
                }
            } catch (Exception e) {
                System.err.println("Excepción al usar PEM-01: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Verifica si PEM-01 está disponible en el inventario.
     *
     * @return true si PEM-01 está disponible, false en caso contrario.
     */
    private boolean isPemAvailable() {
        return pemItem != null && items.getItem(pemItem).isPresent();
    }

    /**
     * Selecciona un arma aleatoria para Npc.
     *
     * @return Un arma aleatoria de la lista NPC_WEAPONS.
     */
    private SelectableItem.Laser selectRandomNpcWeapon() {
        return NPC_WEAPONS[random.nextInt(NPC_WEAPONS.length)];
    }

    /**
     * Intenta usar una munición (Laser).
     *
     * @param laser El arma a usar.
     */
    public void useWeapon(SelectableItem.Laser laser) {
        try {
            var result = items.useItem(laser);
            if (result.isSuccessful()) {
                System.out.println("Cambiando a munición: " + laser.getId());
            } else {
                System.err.println("Error al cambiar a munición: " + laser.getId());
            }
        } catch (Exception e) {
            System.err.println("Excepción al usar arma: " + e.getMessage());
        }
    }

    /**
     * Intenta usar un misil (Rocket).
     *
     * @param rocket El misil a usar.
     */
    public void useMissile(SelectableItem.Rocket rocket) {
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
     * Intenta usar un misil por defecto (PLT-2026 o PLT-2021).
     */
    public void useDefaultMissile() {
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
