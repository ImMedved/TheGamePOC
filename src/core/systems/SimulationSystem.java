package core.systems;

import core.states.SimulationContext;

public interface SimulationSystem {

    void update(SimulationContext context);
}