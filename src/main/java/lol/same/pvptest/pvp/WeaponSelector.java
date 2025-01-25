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
import java.util.Map;
import java.util.Random;

/**
 * Clase principal que maneja la selección de armas y otras lógicas de combate.
 */
public class WeaponSelector {
    private final HeroAPI hero;
    private final HeroItemsAPI items;
    private final PluginAPI plugin; // Referencia al PluginAPI
    private final Random random;
    private final AmmoSelect ammoSelect; // Instancia de AmmoSelect

    // Cooldowns para armas y misiles (en milisegundos)
    private long lastPLD8Use;
    private long lastRCI3Use;
    private long lastISHUse; // Añadir
    private long lastPEMUse; // Añadir

    // Configuración
    private PvPConfig config;

    // Mapa para rastrear las ubicaciones anteriores de los jugadores
    private final Map<Integer, Location> previousPlayerLocations = new HashMap<>();

    // Infection effect ID
    private int infectionEffectId = EntityEffect.INFECTION.getId(); // Usar el ID del enum existente

    public WeaponSelector(PluginAPI plugin, AmmoSelect ammoSelect) {
        this.plugin = plugin;
        this.hero = plugin.requireAPI(HeroAPI.class);
        this.items = plugin.requireAPI(HeroItemsAPI.class);
        this.random = new Random();
        this.ammoSelect = ammoSelect;

        // Inicializar cooldowns
        this.lastPLD8Use = 0;
        this.lastRCI3Use = 0;
        this.lastISHUse = 0; // Añadir
        this.lastPEMUse = 0; // Añadir
    }

    /**
     * Establece la configuración de PvP y actualiza las configuraciones de AmmoSelect.
     *
     * @param config La configuración de PvP.
     */
    public void setConfig(PvPConfig config) {
        this.config = config;
        this.infectionEffectId = config.infectionEffect.getId();
        // Pasar la configuración a AmmoSelect si es necesario
        ammoSelect.setConfig(config.ammoConfig);
    }

    /**
     * Selecciona y usa las armas o municiones según el tipo de objetivo.
     *
     * @param target El objetivo actual (Player o Npc).
     */
    public void selectWeapon(Object target) {
        if (target == null) return;

        long currentTime = System.currentTimeMillis();

        if (target instanceof Player) {
            Player player = (Player) target;
            // Uso de munición específica manejada por AmmoSelect
            ammoSelect.selectWeapon(player);
        } else if (target instanceof Npc) {
            Npc npc = (Npc) target;
            // Uso de munición específica manejada por AmmoSelect
            ammoSelect.selectWeapon(npc);
        }

        // Manejar el uso de Rockets
        handleRockets(currentTime, target instanceof Player);

        // Intentar usar ISH-01 o PEM-01
        boolean ishUsed = useISH(currentTime);
        if (!ishUsed) {
            usePEM(currentTime);
        }

        // Lógica adicional de WeaponSelector (como Auto Cloak)
        handleAutoCloak(currentTime);
    }

    /**
     * Maneja el uso de Rockets.
     *
     * @param currentTime El tiempo actual en milisegundos.
     * @param isPlayer    Si el objetivo es un jugador.
     */
    private void handleRockets(long currentTime, boolean isPlayer) {
        if (isPlayer) {
            // Usar PLD-8 cada X segundos si está habilitado y no está en cooldown
            if (config.ammoConfig.usePLD8 && (currentTime - lastPLD8Use >= config.ammoConfig.pld8Cooldown * 1000)) {
                useMissile(SelectableItem.Rocket.PLD_8);
                lastPLD8Use = currentTime;
            }

            // Usar RCI-3 si el jugador se aleja y no está en cooldown
            Player player = (Player) hero.getLocalTargetAs(Player.class);
            if (config.ammoConfig.useRIC3 && player != null && isPlayerMovingAway(player) && (currentTime - lastRCI3Use >= config.ammoConfig.ric3Cooldown * 1000)) {
                useMissile(SelectableItem.Rocket.R_IC3);
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
    private boolean isPlayerMovingAway(Player player) {
        // Obtener la ubicación previa del jugador
        Location previousLocation = previousPlayerLocations.get(player.getId());

        // Obtener las coordenadas actuales del jugador
        double currentX = player.getLocationInfo().getX();
        double currentY = player.getLocationInfo().getY();

        // Obtener las coordenadas actuales del héroe
        double heroX = hero.getLocationInfo().getX();
        double heroY = hero.getLocationInfo().getY();

        // Crear objetos Location actuales usando Location.of()
        Location currentLocation = Location.of(currentX, currentY);
        Location heroLocation = Location.of(heroX, heroY);

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
     * Maneja el uso automático del Cloak según la configuración.
     *
     * @param currentTime El tiempo actual en milisegundos.
     */
    private void handleAutoCloak(long currentTime) {
        // Implementar la lógica de Auto Cloak aquí
        // Ejemplo simplificado:
        if (config.autoCloak.autoCloakShip && !hero.isInvisible()
                && (currentTime - lastPEMUse > config.autoCloak.secondsOfWaiting * 1000)) {
            if (config.autoCloak.onlyPvpMaps && !hero.getMap().isPvp()) {
                return;
            }
            // Remover o comentar la lógica relacionada con Cloak si no está definido
        /*
        SelectableItem.Special cloakItem = SelectableItem.Special.CLOAK;
        if (cloakItem != null) {
            try {
                var result = items.useItem(cloakItem);
                if (result.isSuccessful()) {
                    System.out.println("Cloak aplicado automáticamente, resultado: " + result.getStatus());
                } else {
                    System.err.println("Error al aplicar Cloak: " + result.getStatus());
                }
            } catch (Exception e) {
                System.err.println("Excepción al usar Cloak: " + e.getMessage());
            }
        } else {
            System.err.println("CLOAK no está definido en SelectableItem.Special");
        }
        */
        }
    }



    /**
     * Intenta usar ISH-01 si está disponible y se cumplen las condiciones.
     *
     * @param currentTime El tiempo actual en milisegundos.
     * @return true si ISH-01 fue usado correctamente, false en caso contrario.
     */
    public boolean useISH(long currentTime) {
        // Verificar cooldown para ISH-01
        if (currentTime - lastISHUse < config.ammoConfig.ishCooldown * 1000) {
            // Aún en cooldown
            return false;
        }

        // Verificar cooldown para PEM-01 para evitar uso simultáneo
        if (currentTime - lastPEMUse < config.ammoConfig.pemCooldown * 1000) {
            // PEM-01 fue usado recientemente, esperar
            return false;
        }

        // Verificar condiciones para usar ISH-01
        if (config.ammoConfig.enableIsh && config.ammoConfig.ishCondition != null && config.ammoConfig.ishCondition.get(plugin) == Condition.Result.ALLOW) {
            try {
                var result = items.useItem(config.ammoConfig.ishItem);
                if (result.isSuccessful()) {
                    System.out.println("Usando munición: " + config.ammoConfig.ishItem.getId());
                    lastISHUse = currentTime;
                    return true;
                } else {
                    System.err.println("Error al usar munición: " + config.ammoConfig.ishItem.getId());
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
        if (currentTime - lastPEMUse < config.ammoConfig.pemCooldown * 1000) {
            // Aún en cooldown
            return false;
        }

        // Verificar cooldown para ISH-01 para evitar uso simultáneo
        if (currentTime - lastISHUse < config.ammoConfig.ishCooldown * 1000) {
            // ISH-01 fue usado recientemente, esperar
            return false;
        }

        // Verificar si PEM-01 está disponible en el inventario
        if (config.ammoConfig.enablePem && ammoSelect.isPemAvailable()) {
            // Verificar condiciones para usar PEM-01
            if (config.ammoConfig.pemCondition != null && config.ammoConfig.pemCondition.get(plugin) == Condition.Result.ALLOW) {
                try {
                    var result = items.useItem(config.ammoConfig.pemItem);
                    if (result.isSuccessful()) {
                        System.out.println("Usando munición: " + config.ammoConfig.pemItem.getId());
                        lastPEMUse = currentTime;
                        return true;
                    } else {
                        System.err.println("Error al usar munición: " + config.ammoConfig.pemItem.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Excepción al usar PEM-01: " + e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * Usa el misil especificado.
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
     * Usa el misil por defecto (PLT-2026 o PLT-2021).
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
