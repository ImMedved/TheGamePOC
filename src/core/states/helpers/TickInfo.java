package core.states.helpers;

public final class TickInfo {

    public long tickIndex;
    public float deltaTime;
    public float interpolationAlpha;

    public TickInfo(long tickIndex, float deltaTime, float interpolationAlpha) {
        this.tickIndex = tickIndex;
        this.deltaTime = deltaTime;
        this.interpolationAlpha = interpolationAlpha;
    }
}