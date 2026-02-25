package core.commands;

import java.util.Map;

public record CustomCommand(
        String type,
        Map<String, Object> payload
) implements Command {
}