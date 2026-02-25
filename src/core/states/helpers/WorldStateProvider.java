package core.states.helpers;

import core.states.WorldState;

public interface WorldStateProvider {
    WorldState getLatestWorldState();
}
