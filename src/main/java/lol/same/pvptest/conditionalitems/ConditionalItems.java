package lol.same.pvptest.conditionalitems;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Behavior;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.extensions.Feature;

@Feature(name = "Items condicionales", description = "Definir cuando usar items automáticamente", enabledByDefault = true)
public class ConditionalItems implements Behavior, Configurable<ConditionalItemsConfig> {
    private final ConditionsManagement conditionsManagement;

    private ConditionalItemsConfig config;

    public ConditionalItems(PluginAPI plugin) {
        this.conditionsManagement = plugin.requireInstance(ConditionsManagement.class);
    }

    @Override
    public void setConfig(ConfigSetting<ConditionalItemsConfig> arg0) {
        this.config = arg0.getValue();
    }

    @Override
    public void onStoppedBehavior() {
        if (config.tickStopped) {
            onTickBehavior();
        }
    }

    @Override
    public void onTickBehavior() {
        conditionsManagement.useKeyWithConditions(config.key1);
        conditionsManagement.useKeyWithConditions(config.key2);
        conditionsManagement.useKeyWithConditions(config.key3);
        conditionsManagement.useKeyWithConditions(config.key4);
        conditionsManagement.useKeyWithConditions(config.key5);
        conditionsManagement.useKeyWithConditions(config.selectable1);
        conditionsManagement.useKeyWithConditions(config.selectable2);
        conditionsManagement.useKeyWithConditions(config.selectable3);
        conditionsManagement.useKeyWithConditions(config.selectable4);
        conditionsManagement.useKeyWithConditions(config.selectable5);
        conditionsManagement.useKeyWithConditions(config.selectable6);
        conditionsManagement.useKeyWithConditions(config.selectable7);
        conditionsManagement.useKeyWithConditions(config.selectable8);
        conditionsManagement.useKeyWithConditions(config.selectable9);
        conditionsManagement.useKeyWithConditions(config.selectable10);
    }
}
