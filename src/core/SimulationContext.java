package core;

import core.commands.Command;
import core.states.WorldState;
import input.InputSnapshot;

import java.util.List;
import java.util.function.LongSupplier;

public final class SimulationContext {

    private final WorldState snapshot;
    private final InputSnapshot input;
    private final float dt;
    private final LongSupplier idSupplier;
    private final List<Command> localCommands;

    public SimulationContext(
            WorldState snapshot,
            InputSnapshot input,
            float dt,
            LongSupplier idSupplier,
            List<Command> localCommands
    ) {
        this.snapshot = snapshot;
        this.input = input;
        this.dt = dt;
        this.idSupplier = idSupplier;
        this.localCommands = localCommands;
    }

    public WorldState snapshot() {
        return snapshot;
    }

    public InputSnapshot input() {
        return input;
    }

    public float dt() {
        return dt;
    }

    public long nextId() {
        return idSupplier.getAsLong();
    }

    public void addCommand(Command command) {
        localCommands.add(command);
    }
}