package util;

/**
 *
 * @author tonyj
 */
public abstract class KpixRecord {

    public enum KpixRecordType {

        DATA, CONFIG, STATUS,RUNSTART, RUNSTOP
    };
    private KpixRecordType recordType;
    private int recordLength;

    KpixRecord(int recordType, int recordLength) {
        this.recordType = KpixRecordType.values()[recordType];
        this.recordLength = recordLength;
    }

    public int getRecordLength() {
        return recordLength;
    }

    public KpixRecordType getRecordType() {
        return recordType;
    }
    
    @Override
    public String toString() {
        return "KpixRecord{" + "recordType=" + recordType + ", recordLength=" + recordLength + '}';
    }
}
