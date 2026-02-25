package core.commands;

import java.util.ArrayList;
import java.util.List;

public final class CommandBuffer {

    private final List<Command> commands = new ArrayList<>();

    public void add(Command command) {
        commands.add(command);
    }

    public List<Command> all() {
        return commands;
    }

    public void clear() {
        commands.clear();
    }
}