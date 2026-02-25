package core.systems;

import core.SimulationContext;

public interface System {

    Phase phase();

    void update(SimulationContext context);
}