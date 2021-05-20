package de.timmi6790.discord_framework.utilities.commons;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * UUID utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UUIDUtilities {
    /**
     * Converts the UUID to a byte array
     *
     * @param uuid the uuid
     * @return the uuid byte array
     */
    public static byte[] getBytesFromUUID(final UUID uuid) {
        return ByteBuffer.wrap(new byte[16])
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
    }

    /**
     * Converts the byte array to a UUID
     *
     * @param bytes the byte array
     * @return the uuid
     */
    public static UUID getUUIDFromBytes(final byte[] bytes) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }
}
