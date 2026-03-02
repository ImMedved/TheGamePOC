package core.commands;

public record ChangeCameraCommand(
        float x,
        float y
) implements Command {}