package network.model;

import java.util.Objects;

public final class NodeId {

    private final long value;

    public NodeId(long value) {
        this.value = value;
    }

    public long value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeId nodeId)) return false;
        return value == nodeId.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "NodeId[" + value + "]";
    }
}