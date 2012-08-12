package util;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author tonyj
 */
public class KpixFileReader implements Closeable {

    private final FileChannel channel;
    private final MappedByteBuffer mapFile;

    public KpixFileReader(File file) throws FileNotFoundException, IOException {
        channel = new RandomAccessFile(file, "r").getChannel();
        mapFile = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        mapFile.order(ByteOrder.LITTLE_ENDIAN);
    }

    public KpixRecord readRecord() throws IOException {
        if (!mapFile.hasRemaining()) return null;
        int marker = mapFile.getInt();
        int recordType = bitMask(marker, 31, 28);
        int recordLength = bitMask(marker, 27, 0);
        switch (recordType) {
            case 0:
                return readDataRecord(mapFile, recordType, recordLength);
            case 1:
            case 2:
            case 3:
            case 4:
                return readXMLRecord(mapFile, recordType, recordLength);
            default:
                throw new IOException("Unknown record type " + recordType);
        }
    }
    
    public boolean hasNextRecord() {
        return mapFile.hasRemaining();
    }

    public void rewind() {
        mapFile.position(0);
    }

    public void close() throws IOException {
        channel.close();
    }

    static int bitMask(int marker, int high, int low) {
        int mask = (2 << (high - low)) - 1;
        return (marker >> low) & mask;
    }

    private KpixRecord readDataRecord(ByteBuffer buffer, int recordType, int recordLength) throws IOException {
        IntBuffer intBuffer = buffer.asIntBuffer();
        int[] header = new int[8];
        intBuffer.get(header);
        int eventNumber = header[0];
        int timestamp = header[1];
        for (int i=2;i<header.length;i++) {
            if (header[i] != 0) throw new IOException("Unexpected data in event header"); 
        }
        int[] data = new int[recordLength-9];
        intBuffer.get(data);
        int trailer = intBuffer.get();
        if (trailer != 0) throw new IOException("Unexpected data in event trailer");
        buffer.position(buffer.position()+recordLength*4);
        return new KpixDataRecord(recordType, recordLength,eventNumber,timestamp,data);
    }

    private KpixRecord readXMLRecord(ByteBuffer buffer, int recordType, int recordLength) {
        byte[] bytes = new byte[recordLength];
        buffer.get(bytes);
        return new KpixXMLRecord(recordType, recordLength,new String(bytes));
    }
}
