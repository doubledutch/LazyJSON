package me.doubledutch.lazyjson.compressor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.*;

/**
 * Segments are the core components of templates.
 * They consist of a constant string part - typically the field name in a json
 * object - and a type reference to the local data stored in a bytebuffer.
 */
public class Segment{
	public static final byte VOID=-1;
	public static final byte BYTE=0;
	public static final byte SHORT=1;
	public static final byte INT=2;
	public static final byte LONG=3;
	// public static final byte FLOAT=4; Needs validation for fit
	public static final byte DOUBLE=5;
	public static final byte BOOLEAN=6;
	public static final byte STRING=7;
	public static final byte NULL=8;

	protected String pre;
	protected byte type;

	public Segment(String pre){
		this.pre=pre;
		this.type=VOID;
	}

	public Segment(String pre,byte type){
		this.pre=pre;
		this.type=type;
	}

	protected static Segment fromDataInput(DataInput din) throws IOException{
		byte type=din.readByte();
		String pre=null;
		int val=0;
		int read=din.readUnsignedByte();
		while(read==255){
			val+=read;
			read=din.readUnsignedByte();
		}
		val+=read;
		if(val>0){
			byte[] raw=new byte[val];
			din.readFully(raw);
			pre=new String(raw,StandardCharsets.UTF_8);
		}
		return new Segment(pre,type);
	}

	protected void toDataOutput(DataOutput dout) throws IOException{
		dout.writeByte(type);
		if(pre==null){
			dout.writeByte(0);
		}else{
			byte[] encoded=pre.getBytes(StandardCharsets.UTF_8);
			int length=encoded.length;
			if(length==0)System.out.println("found 0");
			while(length>0){
				if(length>255){
					dout.writeByte(255);
					length=length-255;
				}else{
					dout.writeByte(length);
					length=0;
				}
			}
			dout.write(encoded);
		}
	}

	/**
	 * Read this segment from a byte buffer using the given dictionary for
	 * lookups. The dictionary may be null if no dictionary values were used
	 * when writing data for this segment.
	 *
	 * @param buf the byte buffer holding the raw data
	 * @param dict a dictionary holding lookup values or null if a dictionary was not used for encoding
	 * @return the string constant held in this segment, with the segment value appended
	 */
	public String read(ByteBuffer buf,DictionaryCache dict){
		StringBuilder out=new StringBuilder();
		if(pre!=null)out.append(pre);
		if(type==VOID)return out.toString();
		if(type==NULL){
			out.append("null");
			return out.toString();
		}
		if(type==BYTE){
			out.append(buf.get());
			return out.toString();
		}
		if(type==SHORT){
			out.append(buf.getShort());
			return out.toString();
		}
		if(type==INT){
			out.append(buf.getInt());
			return out.toString();
		}
		if(type==LONG){
			out.append(buf.getLong());
			return out.toString();
		}
		//if(type==FLOAT){
		//	out.append(buf.getFloat());
		//	return out.toString();
		//}
		if(type==DOUBLE){
			out.append(buf.getDouble());
			return out.toString();
		}
		if(type==BOOLEAN){
			out.append((buf.get()==0?"false":"true"));
			return out.toString();
		}
		if(type==STRING){
			short pos=buf.getShort();
			if(pos>-1){
				out.append("\"");
				out.append(dict.get(pos));
				out.append("\"");
			}else{
				int size=0;
				int val=buf.get() & 0xFF;
				while(val==255){
					size+=val;
					val=buf.get() & 0xFF;
				}
				size+=val;
				byte[] data=new byte[size];
				buf.get(data);
				out.append("\"");
				out.append(new String(data,StandardCharsets.UTF_8));
				out.append("\"");
			}
			return out.toString();
		}
		return null;
	}

	public boolean equals(Object obj){
		if(!(obj instanceof Segment))return false;
		Segment s=(Segment)obj;
		if(s.type!=type)return false;
		if(pre==null){
			if(s.pre!=null)return false;
		}else{
			if(!pre.equals(s.pre)){
				return false;
			}
		}
		return true;
	}

}