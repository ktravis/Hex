package com.detector;

import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.convert.Convert;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.io.File;
import java.util.List;

@Root(name="calibrationData",strict=false)
public class CalibrationData {
	public static final float COEFF_P = 1.0f;
	
	@Element(name="sourceFile")
	public String fileName;
	@Element(name="user")
	public String user;
	@Element(name="timestamp")
	public String timeStamp;
	@Element(name="kpixAsic")
	public KpixAsic kpix;
	
	public double calibrate(int channelIndex, int bucketIndex, float adc) {
		Bucket bucket;
		try {
			bucket = kpix.channels.get(channelIndex).buckets.get(bucketIndex);
		} catch (IndexOutOfBoundsException e) {
			return -1;
		}
		if (bucket.calibGain == null || bucket.calibIntercept == null || bucket.calibGain == -1 || bucket.calibIntercept == -1) return -1;
		return (Math.PI/2 + Math.atan((adc - bucket.calibIntercept)/(bucket.calibGain/Math.pow(10, 15))))*(10/Math.PI);	
	}
	
	public Float[] getData(int channel, int bucket) {
		Bucket b;
		try {
		b = kpix.channels.get(channel).buckets.get(bucket);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return new Float[] {b.baseMean, b.baseRms, b.baseFitMean, b.baseFitSigma, b.baseFitMeanErr, b.baseFitSigmaErr, b.calibGain, b.calibIntercept, b.calibGainErr, b.calibInterceptErr, b.calibGainRms };
	}
	public int getChannelCount() { 
		return kpix.channels.size();
	}
	
	public static CalibrationData getInstance(String path) throws Exception {
		Serializer serializer = new Persister(new AnnotationStrategy());
		File source = new File(path);
		return serializer.read(CalibrationData.class, source);
	}
	public static CalibrationData getInstance(File source) throws Exception {
		Serializer serializer = new Persister(new AnnotationStrategy());
		return serializer.read(CalibrationData.class, source);
	}
}

class FloatConverter implements Converter<Float> {
	@Override
	public Float read(InputNode node) throws Exception {
		String val = node.getValue();
		if (node == null || val.contains("nan") || val == null) {
			return -1f;
		}
		else {
			try { 
				return Float.valueOf(val); }
			catch (NumberFormatException e) {
				e.printStackTrace();
				return -1f;
			}
		}
	}
	@Override
	public void write(OutputNode node, Float value) throws Exception { node.setValue(String.valueOf(value)); }
}

@Root(name="kpixAsic",strict=false)
class KpixAsic {
	@Attribute(name="id")
	String id;
	
	@ElementList(inline=true, entry="Channel", required=false)
	public List<Channel> channels;
}

@Root(name="Channel",strict=false)
class Channel {
	@Attribute(name="id")
	public String id;
	
	@ElementList(entry="Bucket",inline=true,name="Bucket")
	public List<Bucket> buckets;
	
}

@Root(name="Bucket",strict=false) 
class Bucket {
	@Attribute(name="id")
	public String id;
	
	@Path("Range") 
	@Element(name="BaseMean",required=false)
	@Convert(FloatConverter.class)
	public Float baseMean;
	@Path("Range")
	@Element(name="BaseRms",required=false)
	@Convert(FloatConverter.class)
	public Float baseRms;
	@Path("Range")
	@Element(name="BaseFitMean",required=false)
	@Convert(FloatConverter.class)
	public Float baseFitMean;
	@Path("Range")
	@Element(name="BaseFitSigma",required=false)
	@Convert(FloatConverter.class)
	public Float baseFitSigma;
	@Path("Range")
	@Element(name="BaseFitMeanErr",required=false)
	@Convert(FloatConverter.class)
	public Float baseFitMeanErr;
	@Path("Range")
	@Element(name="BaseFitSigmaErr",required=false)
	@Convert(FloatConverter.class)
	public Float baseFitSigmaErr;
	@Path("Range")
	@Element(name="CalibGain",required=false)
	@Convert(FloatConverter.class)
	public Float calibGain;
	@Path("Range")
	@Element(name="CalibIntercept",required=false)
	@Convert(FloatConverter.class)
	public Float calibIntercept;
	@Path("Range")
	@Element(name="CalibGainErr",required=false)
	@Convert(FloatConverter.class)
	public Float calibGainErr;
	@Path("Range")
	@Element(name="CalibInterceptErr",required=false)
	@Convert(FloatConverter.class)
	public Float calibInterceptErr;
	@Path("Range")
	@Element(name="CalibGainRms",required=false)
	@Convert(FloatConverter.class)
	public Float calibGainRms;
}