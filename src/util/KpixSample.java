package util;

/**
 *
 * @author tonyj
 */
public class KpixSample {
    private final int address;
    private final boolean empty;
    private final boolean badEvent;
    private final ADCRange adcRange;
    private final TriggerSource triggerSource;
    private final int bucket;
    private final int channel;
    private final int time;
    private final int adc;

    public enum KpixSampleType {

        KPIX, TEMPERATURE, SAMPLE
    };
    public enum ADCRange {

        NORMAL, LOWGAIN
    };
    public enum TriggerSource {

        EXTERNAL, SELF
    };
    private KpixSampleType type;

    KpixSample(int type, int data0, int data1) {
        this.type = KpixSampleType.values()[type];
        address = KpixFileReader.bitMask(data0, 27, 16);
        empty = KpixFileReader.bitMask(data0, 15, 15) != 0;
        badEvent = KpixFileReader.bitMask(data0, 14, 14) != 0;
        adcRange = ADCRange.values()[KpixFileReader.bitMask(data0, 13, 13)];
        triggerSource = TriggerSource.values()[KpixFileReader.bitMask(data0, 12, 12)];
        bucket = KpixFileReader.bitMask(data0, 11, 10);
        channel = KpixFileReader.bitMask(data0, 9, 0);
        time = KpixFileReader.bitMask(data1, 28, 16);
        adc = KpixFileReader.bitMask(data1, 12, 0);
    }

    public KpixSampleType getType() {
        return type;
    }

    public int getAdc() {
        return adc;
    }

    public ADCRange getAdcRange() {
        return adcRange;
    }

    public int getAddress() {
        return address;
    }

    public boolean isBadEvent() {
        return badEvent;
    }

    public int getBucket() {
        return bucket;
    }

    public int getChannel() {
        return channel;
    }

    public boolean isEmpty() {
        return empty;
    }

    public int getTime() {
        return time;
    }

    public TriggerSource getTriggerSource() {
        return triggerSource;
    }
    
}
