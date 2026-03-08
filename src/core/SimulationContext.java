package core;

import core.commands.Command;
import core.states.WorldState;
import input.InputFrame;
import input.InputSnapshot;

import java.util.List;
import java.util.function.LongSupplier;

public final class SimulationContext {

    private final WorldState snapshot;
    private final InputFrame inputFrame;
    private final float dt;
    private final LongSupplier idSupplier;
    private final List<Command> localCommands;
    private final long localPlayerId;

    public SimulationContext(
            WorldState snapshot,
            InputFrame inputFrame,
            float dt,
            LongSupplier idSupplier,
            List<Command> localCommands,
            long localPlayerId
    ) {
        this.snapshot = snapshot;
        this.inputFrame = inputFrame;
        this.dt = dt;
        this.idSupplier = idSupplier;
        this.localCommands = localCommands;
        this.localPlayerId = localPlayerId;
    }

    public WorldState snapshot() {
        return snapshot;
    }

    public InputSnapshot input(long playerId) {
        return inputFrame.get(playerId);
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

    public long localPlayerId() {
        return localPlayerId;
    }
}