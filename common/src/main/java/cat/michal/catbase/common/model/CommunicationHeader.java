package cat.michal.catbase.common.model;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class CommunicationHeader {
    private static final ThreadLocal<CRC32> crc32 = new ThreadLocal<CRC32>() {
        @Override
        public CRC32 get() {
            return new CRC32();
        }
    };

    private int length;
    private long checksum;

    private CommunicationHeader(int length, long checksum) {
        this.length = length;
        this.checksum = checksum;
    }

    private CommunicationHeader(int length) {
        this.length = length;
    }

    public static CommunicationHeader create(@NotNull ByteBuffer buffer) {
        int length = buffer.getInt();
        long checksum = buffer.getLong();
        CommunicationHeader header = new CommunicationHeader(length, checksum);

        if(header.getChecksum() != header.generateChecksum()) {
            return null;
        }

        return header;
    }

    public long generateChecksum() {
        CRC32 crc32Instance = crc32.get();

        crc32Instance.update(length);

        return crc32Instance.getValue();
    }

    public int getLength() {
        return length;
    }

    public long getChecksum() {
        return checksum;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }
}
