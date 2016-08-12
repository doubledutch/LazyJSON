package me.doubledutch.lazyjson.compressor;

import java.nio.ByteBuffer;
import java.util.*;

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

	public void addNull(){
		addSegment(new Segment(null,Segment.NULL));
	}

	public void addBoolean(){
		addSegment(new Segment(null,Segment.BOOLEAN));
	}

	public void addString(){
		addSegment(new Segment(null,Segment.STRING));
	}

	public void compact(){
		List<Segment> compactList=new ArrayList<Segment>();
		StringBuilder buf=new StringBuilder();
		for(Segment segment:segmentList){
			if(segment.type==Segment.VOID){
				buf.append(segment.pre);
			}else{
				if(buf.length()>0){
					buf.append(segment.pre);
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
}