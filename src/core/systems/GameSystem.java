package core.systems;

import core.SimulationContext;

public interface GameSystem {

    Phase phase();

    void update(SimulationContext context);
}