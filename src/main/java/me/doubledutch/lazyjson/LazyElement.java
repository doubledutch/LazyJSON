package me.doubledutch.lazyjson;

import me.doubledutch.lazyjson.compressor.*;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;

public class LazyElement{
	protected LazyNode root;
	protected char[] cbuf;

	// Cache value for length
	private int length=-1;

	protected LazyElement(LazyNode root,char[] source){
		this.root=root;
		this.cbuf=source;
	}

	protected LazyElement() throws LazyException{

	}

	protected char[] getCharBuffer(){
		return cbuf;
	}

	public Template extractTemplate(){
		Template t=new Template();
		root.addSegments(cbuf,t);
		t.compact();
		return t;
	}

	public void writeTemplateValues(ByteBuffer buf,DictionaryCache dict) throws BufferOverflowException{
		root.writeSegmentValues(cbuf,buf,dict);
	}

	/**
	 * Parses a string and returns either a LazyObject or LazyArray
	 *
	 * @param str the source json data
	 * @return either a LazyObject or LazyArray instance
	 * @throws LazyException if the string could not be parsed
	 */
	public static LazyElement parse(String str) throws LazyException{
		if(str.startsWith("[")){
			return new LazyArray(str);
		}else{
			return new LazyObject(str);
		}
	}

	public static LazyElement readFromTemplate(Template t,ByteBuffer buf,DictionaryCache dict) throws LazyException{
		String str=t.read(buf,dict);
		// System.out.println(str);
		return parse(str);
	}

	/**
	 * Returns the number of fields on this object
	 *
	 * @return the number of fields
	 */
	public int length(){
		if(root.child==null){
			return 0;
		}
		if(length>-1){
			return length;
		}
		length=root.getChildCount();
		return length;
	}

	/**
	 * Returns a raw string extracted from the source string that covers the
	 * start and end index of this object.
	 *
	 * @return as string representation of this object as given in the source string
	 */
	public String toString(){
		return new String(cbuf,root.startIndex,root.endIndex-root.startIndex);
	}

	public byte[] toByteArray(){
		int size=root.getBufferSize();
		ByteBuffer buf=ByteBuffer.allocate(size);
		root.writeToBuffer(buf);
		return buf.array();
	}
}