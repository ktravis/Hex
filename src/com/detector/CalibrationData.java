package com.detector;

import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Persister;
import java.io.File;
import java.util.List;

@Root(name="calibrationData",strict=false)
public class CalibrationData {
	
	@Element(name="sourceFile")
	String fileName;
	@Element(name="user")
	String user;
	@Element(name="timestamp")
	String timeStamp;
	@Element(name="kpixAsic")
	KpixAsic kpix;
	
	
	
	public static void main (String[] args) {
		Serializer serializer = new Persister();
		File source = new File("res/2012_10_09_18_22_30.bin.xml");
		try {
			CalibrationData cd = serializer.read(CalibrationData.class, source);
			System.out.println(cd.fileName);
			System.out.println(cd.user);
			System.out.println(cd.timeStamp);
			System.out.println(cd.kpix.channels.get(3).buckets.get(1).baseRms);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

@Root(name="kpixAsic",strict=false)
class KpixAsic {
	@Attribute(name="id")
	String id;
	
	@ElementList(inline=true, entry="Channel", required=false)
	List<Channel> channels;
}

@Root(name="Channel",strict=false)
class Channel {
	@Attribute(name="id")
	String id;
	
	@ElementList(entry="Bucket",inline=true,name="Bucket")
	List<Bucket> buckets;
	
}

@Root(name="Bucket",strict=false) 
class Bucket {
	@Attribute(name="id")
	String id;
	
	@Path("Range") 
	@Element(name="BaseMean",required=false)
	float baseMean;
	@Path("Range")
	@Element(name="BaseRms",required=false)
	float baseRms;
	@Path("Range")
	@Element(name="BaseFitMean",required=false)
	float baseFitMean;
	@Path("Range")
	@Element(name="BaseFitSigma",required=false)
	float baseFitSigma;
	@Path("Range")
	@Element(name="BaseFitMeanErr",required=false)
	float baseFitMeanErr;
	@Path("Range")
	@Element(name="BaseFitSigmaErr",required=false)
	float baseFitSigmaErr;
	@Path("Range")
	@Element(name="CalibGain",required=false)
	float calibGain;
	@Path("Range")
	@Element(name="CalibIntercept",required=false)
	float calibIntercept;
	@Path("Range")
	@Element(name="CalibGainErr",required=false)
	float calibGainErr;
	@Path("Range")
	@Element(name="CalibInterceptErr",required=false)
	float calibInterceptErr;
	@Path("Range")
	@Element(name="CalibGainRms",required=false)
	float calibGainRms;
}