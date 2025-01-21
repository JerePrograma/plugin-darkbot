package lol.same.pvptest.syncer.messages;

import eu.darkbot.api.game.items.SelectableItem;

public class VantsChanged extends Message {
    private final String formationId;

    public VantsChanged(int fromPlayerId, SelectableItem.Formation formation) {
        super(fromPlayerId);
        formationId = formation.getId();
    }

    public SelectableItem.Formation getFormation() {
        return SelectableItem.Formation.of(formationId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        var other = (VantsChanged) obj;
        return super.equals(obj) && formationId.equals(other.formationId);
    }

    @Override
    public String debug() {
        return "Formación: " + SelectableItem.Formation.of(formationId);
    }
}
