package lol.same.pvptest.utils;

import eu.darkbot.api.game.items.ItemCategory;
import eu.darkbot.api.game.items.SelectableItem;

import java.util.List;

public class ItemUtils {
    public static SelectableItem getItemById(String id) {
        for (ItemCategory key : SelectableItem.ALL_ITEMS.keySet()) {
            List<SelectableItem> selectableItemList = SelectableItem.ALL_ITEMS.get(key);
            for (SelectableItem next : selectableItemList) {
                if (next.getId().equals(id)) {
                    return next;
                }
            }
        }
        return null;
    }
}
