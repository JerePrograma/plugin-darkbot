package lol.same.pvptest.conditionalitems;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Option;

@Configuration("conditional_items")
public class ConditionalItemsConfig {
    public boolean tickStopped = false;

    public ExtraKeyConditionsKey key1 = new ExtraKeyConditionsKey();
    public ExtraKeyConditionsKey key2 = new ExtraKeyConditionsKey();
    public ExtraKeyConditionsKey key3 = new ExtraKeyConditionsKey();
    public ExtraKeyConditionsKey key4 = new ExtraKeyConditionsKey();
    public ExtraKeyConditionsKey key5 = new ExtraKeyConditionsKey();

    public ExtraKeyConditionsSelectable selectable1 = new ExtraKeyConditionsSelectable();
    public ExtraKeyConditionsSelectable selectable2 = new ExtraKeyConditionsSelectable();
    public ExtraKeyConditionsSelectable selectable3 = new ExtraKeyConditionsSelectable();
    public ExtraKeyConditionsSelectable selectable4 = new ExtraKeyConditionsSelectable();
    public ExtraKeyConditionsSelectable selectable5 = new ExtraKeyConditionsSelectable();
    public ExtraKeyConditionsSelectable selectable6 = new ExtraKeyConditionsSelectable();
    public ExtraKeyConditionsSelectable selectable7 = new ExtraKeyConditionsSelectable();
    public ExtraKeyConditionsSelectable selectable8 = new ExtraKeyConditionsSelectable();
    public ExtraKeyConditionsSelectable selectable9 = new ExtraKeyConditionsSelectable();
    public ExtraKeyConditionsSelectable selectable10 = new ExtraKeyConditionsSelectable();
}
