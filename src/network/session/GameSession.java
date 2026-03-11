package network.session;

import java.util.UUID;

public final class GameSession {

    public final UUID gameId;

    public long playerA;
    public long playerB;
    public long validator;

    public GameSession(UUID gameId) {
        this.gameId = gameId;
    }
}
