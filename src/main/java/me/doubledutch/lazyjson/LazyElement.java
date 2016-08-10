package me.doubledutch.lazyjson;

import java.nio.ByteBuffer;

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