package network.adapter;

import input.InputSnapshot;

import java.io.*;

public final class InputCodec {

    public byte[] encode(InputSnapshot input) {

        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            out.writeInt(input.tick);
            out.writeLong(input.ownerId);

            out.writeFloat(input.moveX);
            out.writeFloat(input.moveY);

            out.writeBoolean(input.shoot);

            out.writeFloat(input.mouseX);
            out.writeFloat(input.mouseY);

            out.writeBoolean(input.key1Pressed);
            out.writeBoolean(input.key2Pressed);
            out.writeBoolean(input.key3Pressed);

            out.flush();

            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputSnapshot decode(byte[] data) {

        try {

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream in = new DataInputStream(bais);

            int tick = in.readInt();
            long ownerId = in.readLong();

            float moveX = in.readFloat();
            float moveY = in.readFloat();

            boolean shoot = in.readBoolean();

            float mouseX = in.readFloat();
            float mouseY = in.readFloat();

            boolean key1 = in.readBoolean();
            boolean key2 = in.readBoolean();
            boolean key3 = in.readBoolean();

            return new InputSnapshot(
                    tick,
                    ownerId,
                    moveX,
                    moveY,
                    shoot,
                    mouseX,
                    mouseY,
                    key1,
                    key2,
                    key3
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}