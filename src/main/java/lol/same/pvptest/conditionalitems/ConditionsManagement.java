package lol.same.pvptest.conditionalitems;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import eu.darkbot.api.game.items.ItemFlag;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.managers.HeroItemsAPI;

public class ConditionsManagement {
    private final PluginAPI api;
    private final HeroItemsAPI items;

    public ConditionsManagement(PluginAPI api, HeroItemsAPI heroItems) {
        this.api = api;
        this.items = heroItems;
    }

    /**
     * Usa un ítem si se cumplen las condiciones especificadas.
     *
     * @param condition      Condición que debe cumplirse.
     * @param selectableItem Ítem seleccionable a usar.
     * @return {@code true} si el ítem fue usado correctamente, {@code false} en caso contrario.
     */
    public boolean useKeyWithConditions(Condition condition, SelectableItem selectableItem) {
        if (selectableItem != null && (condition == null || condition.get(api).allows())) {
            return useSelectableReadyWhenReady(selectableItem);
        }
        return false;
    }

    /**
     * Usa un ítem seleccionable si está listo.
     *
     * @param selectableItem Ítem seleccionable a usar.
     * @return {@code true} si el ítem fue usado correctamente, {@code false} en caso contrario.
     */
    private boolean useSelectableReadyWhenReady(SelectableItem selectableItem) {
        if (selectableItem == null) {
            return false;
        }
        return items.useItem(selectableItem, 250, ItemFlag.USABLE, ItemFlag.READY, ItemFlag.AVAILABLE,
                ItemFlag.NOT_SELECTED).isSuccessful();
    }
}
