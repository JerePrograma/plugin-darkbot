package lol.same.pvptest.syncer;

import eu.darkbot.api.game.enums.PetGear;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public class SyncerConfig {
    public int leaderId;
    public boolean canBeLeader;
    public int leaderPriority;
    public @Nullable PetGear defaultPetGear;
    public Set<PetGear> syncedPetGear = Collections.emptySet();
    public boolean syncLaser;
    public boolean syncRocket;
    public boolean syncVants;
    public boolean syncEnergyLeech;

    public SyncerConfig(int leaderId) {
        this.leaderId = leaderId;
    }
}
