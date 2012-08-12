package util;

import java.util.AbstractList;
import java.util.List;

/**
 *
 * @author tonyj
 */
public class KpixDataRecord extends KpixRecord {

    private final int eventNumber;
    private final int timestamp;
    private final int[] data;

    KpixDataRecord(int recordType, int recordLength, int eventNumber, int timestamp, int[] data) {
        super(recordType, recordLength);
        this.eventNumber = eventNumber;
        this.timestamp = timestamp;
        this.data = data;
    }
    /**
     * Get the list of samples in this record
     * @return A list of samples
     */
    public List<KpixSample> getSamples() {
        return new AbstractList<KpixSample>() {

            @Override
            public KpixSample get(int index) {
                int type = KpixFileReader.bitMask(data[2 * index], 31, 28);
                switch (type) {
                    case 0:
                    case 1:
                    case 2:
                        return new KpixSample(type, data[2 * index], data[2 * index + 1]);
                    default:
                        throw new RuntimeException("Unknown sample type: " + type);
                }
            }

            @Override
            public int size() {
                return data.length / 2;
            }
        };
    }
    /**
     * Get the raw data (not including header and trailer) for this record.
     * @return 
     */
    public int[] getRawSampleData() {
        return data;
    }

    public int getEventNumber() {
        return eventNumber;
    }

    public int getTimestamp() {
        return timestamp;
    }

}
