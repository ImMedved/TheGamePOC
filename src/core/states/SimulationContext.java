package core.states;

import core.commands.CommandBuffer;
import input.InputSnapshot;

import java.util.function.LongSupplier;

public final class SimulationContext {

    private final WorldState snapshot;
    private final CommandBuffer commandBuffer;
    private final InputSnapshot input;
    private final float dt;
    private final LongSupplier idSupplier;

    public SimulationContext(
            WorldState snapshot,
            CommandBuffer commandBuffer,
            InputSnapshot input,
            float dt,
            LongSupplier idSupplier
    ) {
        this.snapshot = snapshot;
        this.commandBuffer = commandBuffer;
        this.input = input;
        this.dt = dt;
        this.idSupplier = idSupplier;
    }

    public WorldState snapshot() {
        return snapshot;
    }

    public CommandBuffer commands() {
        return commandBuffer;
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
}