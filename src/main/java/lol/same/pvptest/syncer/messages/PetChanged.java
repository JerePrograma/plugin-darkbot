package lol.same.pvptest.syncer.messages;

import eu.darkbot.api.game.enums.PetGear;

import java.util.Optional;

public class PetChanged extends Message {
    private final boolean enabled;
    private final int gearId;

    public PetChanged(int fromPlayerId, boolean enabled, PetGear gear) {
        super(fromPlayerId);
        this.enabled = enabled;
        this.gearId = (gear != null) ? gear.getId() : -1;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Optional<PetGear> getGear() {
        if (gearId == -1)
            return Optional.empty();
        return Optional.of(PetGear.of(gearId));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        var other = (PetChanged) obj;
        return super.equals(obj) && enabled == other.enabled && gearId == other.gearId;
    }

    @Override
    public String debug() {
        return "Pet activo " + enabled + " con gear " + PetGear.of(gearId);
    }
}
