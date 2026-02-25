package core;

import core.commands.Command;

import java.util.ArrayList;
import java.util.List;

public final class CommandCollector {

    public static List<Command> merge(List<List<Command>> perSystemLists) {

        List<Command> result = new ArrayList<>();

        for (List<Command> list : perSystemLists) {
            result.addAll(list);
        }

        return result;
    }
}