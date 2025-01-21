package lol.same.pvptest.pvp.config;

import eu.darkbot.api.config.annotations.Dropdown;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class TargetSelectModeOptions implements Dropdown.Options<TargetSelectMode> {
    @Override
    public List<TargetSelectMode> options() {
        return Arrays.asList(TargetSelectMode.values());
    }

    @Override
    public @NotNull String getText(TargetSelectMode mode) {
        if (mode == null) return "";
        switch (mode) {
            case HELP_ATTACK_PLAYERS:
                return "Ayudar a atacar jugadores";
            case HELP_ATTACK_NPCS:
                return "Ayudar a atacar NPCs";
            case DEFEND_LEADER_FROM_PLAYERS:
                return "Defender al líder de jugadores";
            case DEFEND_GROUP_FROM_PLAYERS:
                return "Defender a miembros del grupo de jugadores";
            case DEFEND_SELF_FROM_PLAYERS:
                return "Defenderse de jugadores";
            case DEFEND_SELF_FROM_NPCS:
                return "Defenderse de NPCs";
            case ATTACK_NPCS:
                return "Atacar NPCs";
        }
        return "";
    }

    @Override
    public String getTooltip(TargetSelectMode mode) {
        return getText(mode);
    }
}
