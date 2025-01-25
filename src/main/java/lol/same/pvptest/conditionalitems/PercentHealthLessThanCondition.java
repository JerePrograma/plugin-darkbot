package lol.same.pvptest.conditionalitems;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.game.other.Health;
import org.jetbrains.annotations.NotNull;

/**
 * Condición que verifica si la salud de la nave está por debajo de un porcentaje especificado.
 */
public class PercentHealthLessThanCondition implements Condition {
    private final int percent;

    /**
     * Constructor.
     *
     * @param percent Porcentaje de salud por debajo del cual la condición se cumple.
     */
    public PercentHealthLessThanCondition(int percent) {
        this.percent = percent;
    }

    @Override
    public @NotNull Result get(@NotNull PluginAPI api) {
        if (api instanceof HeroAPI) {
            HeroAPI hero = (HeroAPI) api;
            Health health = hero.getHealth();
            if (health != null) {
                double shieldPercent = health.shieldPercent(); // Obtiene el porcentaje de escudos
                double hullPercent = health.hullPercent();     // Obtiene el porcentaje de casco
                // Puedes ajustar qué porcentaje considerar: escudos, casco o ambos
                if (shieldPercent < percent || hullPercent < percent) {
                    return Result.ALLOW;
                }
            }
        }
        return Result.DENY;
    }
}
