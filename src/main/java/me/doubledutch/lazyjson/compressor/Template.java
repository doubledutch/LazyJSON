package me.doubledutch.lazyjson.compressor;

import java.nio.ByteBuffer;
import java.util.*;
import java.io.*;

public class Template{
	private List<Segment> segmentList;

	public Template(){
		segmentList=new ArrayList<Segment>();
	}

	public void addSegment(Segment seg){
		segmentList.add(seg);
	}

	public void addConstant(String pre){
		addSegment(new Segment(pre));
	}

	public void addNull(String pre){
		addSegment(new Segment(pre,Segment.NULL));
	}

	public void addBoolean(String pre){
		addSegment(new Segment(pre,Segment.BOOLEAN));
	}

	public void addString(String pre){
		addSegment(new Segment(pre,Segment.STRING));
	}

	public void addByte(String pre){
		addSegment(new Segment(pre,Segment.BYTE));
	}

	public void addShort(String pre){
		addSegment(new Segment(pre,Segment.SHORT));
	}

	public void addInt(String pre){
		addSegment(new Segment(pre,Segment.INT));
	}

	public void addLong(String pre){
		addSegment(new Segment(pre,Segment.LONG));
	}

	// public void addFloat(String pre){
	//	addSegment(new Segment(pre,Segment.FLOAT));
	// }

	public void addDouble(String pre){
		addSegment(new Segment(pre,Segment.DOUBLE));
	}

	public void addNull(){
		addSegment(new Segment(null,Segment.NULL));
	}

	public void addBoolean(){
		addSegment(new Segment(null,Segment.BOOLEAN));
	}

	public void addString(){
		addSegment(new Segment(null,Segment.STRING));
	}

	public void addByte(){
		addSegment(new Segment(null,Segment.BYTE));
	}

	public void addShort(){
		addSegment(new Segment(null,Segment.SHORT));
	}

	public void addInt(){
		addSegment(new Segment(null,Segment.INT));
	}

	public void addLong(){
		addSegment(new Segment(null,Segment.LONG));
	}

	// public void addFloat(){
	//	addSegment(new Segment(null,Segment.FLOAT));
	// }

	public void addDouble(){
		addSegment(new Segment(null,Segment.DOUBLE));
	}

	public void compact(){
		List<Segment> compactList=new ArrayList<Segment>();
		StringBuilder buf=new StringBuilder();
		for(Segment segment:segmentList){
			if(segment.type==Segment.VOID){
				if(segment.pre!=null){
					buf.append(segment.pre);
				}
			}else{
				if(buf.length()>0){
					if(segment.pre!=null){
						buf.append(segment.pre);
					}
					segment.pre=buf.toString();
					buf=new StringBuilder();
				}
				compactList.add(segment);
			}
		}
		if(buf.length()>0){
			compactList.add(new Segment(buf.toString()));
		}
		segmentList=compactList;
	}

	public String read(ByteBuffer buf,DictionaryCache dict){
		StringBuilder data=new StringBuilder();
		for(Segment segment:segmentList){
			data.append(segment.read(buf,dict));
		}
		return data.toString();
	}

	public boolean equals(Object obj){
		if(!(obj instanceof Template))return false;
		Template t=(Template)obj;
		if(t.segmentList.size()!=segmentList.size())return false;
		for(int i=0;i<segmentList.size();i++){
			if(!segmentList.get(i).equals(t.segmentList.get(i))){
				return false;
			}
		}
		return true;
	}

	public static Template fromDataInput(DataInput din) throws IOException{
		Template t=new Template();
		int size=din.readInt();
		for(int i=0;i<size;i++){
			t.addSegment(Segment.fromDataInput(din));
		}
		return t;
	}

	public void toDataOutput(DataOutput dout) throws IOException{
		dout.writeInt(segmentList.size());
		for(Segment s:segmentList){
			s.toDataOutput(dout);
		}
	}

	public int hashCode(){
		int acc=0;
		for(Segment s:segmentList){
			if(s.pre!=null){
				acc+=s.pre.length();
			}
			if(s.type>0){
				acc*=s.type;
			}
		}
		return acc;
	}

}