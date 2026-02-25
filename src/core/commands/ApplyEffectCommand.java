package core.commands;

import core.states.EffectData;

public record ApplyEffectCommand(
        EffectData effect
) implements Command {
}