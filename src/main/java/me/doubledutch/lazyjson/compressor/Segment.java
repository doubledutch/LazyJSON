package me.doubledutch.lazyjson.compressor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Segments are the core components of templates.
 * They consist of a constant string part - typically the field name in a json
 * object - and a type reference to the local data stored in a bytebuffer.
 */
public class Segment{
	private static final byte VOID=-1;
	private static final byte BYTE=0;
	private static final byte SHORT=1;
	private static final byte INT=2;
	private static final byte LONG=3;
	private static final byte FLOAT=4;
	private static final byte DOUBLE=5;
	private static final byte BOOLEAN=6;
	private static final byte STRING=7;
	private static final byte NULL=8;
	private static final byte LOOKUP=9;

	private String pre;
	private byte type;
	private short lookup;

	/**
	 * Read this segment from a byte buffer using the given dictionary for
	 * lookups. The dictionary may be null if no dictionary values were used
	 * when writing data for this segment.
	 *
	 * @param buf the byte buffer holding the raw data
	 * @param dict a dictionary holding lookup values or null if a dictionary was not used for encoding
	 * @return the string constant held in this segment, with the segment value appended
	 */
	public String read(ByteBuffer buf,Dictionary dict){
		if(type==VOID)return pre;
		if(type==NULL)return pre+"null";
		if(type==BYTE)return pre+buf.get();
		if(type==SHORT)return pre+buf.getShort();
		if(type==INT)return pre+buf.getInt();
		if(type==LONG)return pre+buf.getLong();
		if(type==FLOAT)return pre+buf.getFloat();
		if(type==DOUBLE)return pre+buf.getDouble();
		if(type==BOOLEAN)return pre+(buf.get()==0?"false":"true");
		if(type==LOOKUP)return dict.get(lookup);
		if(type==STRING){
			int size=0;
			int val=buf.get() & 0xFF;
			while(val==255){
				size+=val;
				val=buf.get() & 0xFF;
			}
			size+=val;
			byte[] data=new byte[size];
			buf.get(data);
			return pre+new String(data,StandardCharsets.UTF_8);
		}
		return null;
	}
}