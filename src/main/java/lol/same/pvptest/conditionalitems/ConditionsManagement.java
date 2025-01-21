package lol.same.pvptest.conditionalitems;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import eu.darkbot.api.game.items.ItemFlag;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.managers.HeroItemsAPI;
import lol.same.pvptest.utils.ItemUtils;

public class ConditionsManagement {
    private final PluginAPI api;
    private final HeroItemsAPI items;

    public ConditionsManagement(PluginAPI api, HeroItemsAPI heroItems) {
        this.api = api;
        this.items = heroItems;
    }

    public boolean useKeyWithConditions(Condition condition, SelectableItem selectableItem) {
        if (selectableItem != null && (condition == null || condition.get(api).allows())) {
            return useSelectableReadyWhenReady(selectableItem);
        }
        return false;
    }

    public boolean useKeyWithConditions(ExtraKeyConditionsSelectable extra) {
        if (!extra.enable) {
            return false;
        }
        return useKeyWithConditions(extra.condition, ItemUtils.getItemById(extra.item));
    }

    public boolean useKeyWithConditions(ExtraKeyConditionsKey extra) {
        if (!extra.enable) {
            return false;
        }
        SelectableItem selectableItem = items.getItem(extra.Key);
        return useKeyWithConditions(extra.condition, selectableItem);
    }

    public boolean useSelectableReadyWhenReady(SelectableItem selectableItem) {
        if (selectableItem == null) {
            return false;
        }
        return items.useItem(selectableItem, 250, ItemFlag.USABLE, ItemFlag.READY, ItemFlag.AVAILABLE,
                ItemFlag.NOT_SELECTED).isSuccessful();
    }
}
