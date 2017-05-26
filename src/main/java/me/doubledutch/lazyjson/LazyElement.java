package me.doubledutch.lazyjson;

import me.doubledutch.lazyjson.compressor.*;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;

public abstract class LazyElement{
	protected LazyNode root;
	protected LazyElement parent;

	// Cache value for length
	private int length=-1;

	protected LazyElement(LazyNode root){
		this.root=root;
	}

	protected LazyElement() throws LazyException{

	}

	protected LazyNode appendAndSetDirtyString(byte type,String value) throws LazyException{
		StringBuilder dirtyBuf=root.getDirtyBuf();
		LazyNode child=new LazyNode(type,dirtyBuf.length());
		dirtyBuf.append(value);
		child.endIndex=dirtyBuf.length();
		child.dirty=true;
		child.dirtyBuf=dirtyBuf;
		return child;
	}

	public Template extractTemplate(){
		Template t=new Template();
		root.addSegments(t);
		t.compact();
		return t;
	}

	public abstract LazyType getType();

	public void writeTemplateValues(ByteBuffer buf,DictionaryCache dict) throws BufferOverflowException{
		root.writeSegmentValues(buf,dict);
	}

	/**
	 * Parses a string and returns either a LazyObject or LazyArray
	 *
	 * @param str the source json data
	 * @return either a LazyObject or LazyArray instance
	 * @throws LazyException if the string could not be parsed
	 */
	public static LazyElement parse(String str) throws LazyException{
		int index=0;
		while(index<str.length()){
			char ch=str.charAt(index);
			if(ch=='['){
				return new LazyArray(str);
			}
			if(ch=='{'){
				return new LazyObject(str);
			}
			index++;
		}
		throw new LazyException("The given string is not a JSON object or array");
	}

	protected static boolean shouldQuoteString(String str){
		if(str==null)return false;
		boolean found=false;
		int length=str.length();
		char[] cbuf=new char[length];
		str.getChars(0,length,cbuf,0);
		for(int i=0;i<length;i++){
			char c=cbuf[i];
			if(c=='\\' || c=='"' || c=='\b' || c=='\t' || c=='\n' || c=='\f' || c=='\r'){// || c<' ' || (c>= '\u0080' && c<'\u00a0') || (c>='\u2000' && c<'\u2100')){
				found=true;
			}
		}
		return found;
	}

	protected static String quoteString(String str){
		StringBuffer buf=new StringBuffer();
		int length=str.length();
		char[] cbuf=new char[length];
		str.getChars(0,length,cbuf,0);
		
        for(int i=0; i<length; i++){
        	char c=cbuf[i];
            switch(c){
            	case '\\':
            		buf.append("\\\\");
            		break;
            	case '"':
                	buf.append('\\');
                	buf.append(c);
                	break;
	            case '\b':
	                buf.append("\\b");
	                break;
	            case '\t':
	                buf.append("\\t");
	                break;
	            case '\n':
	                buf.append("\\n");
	                break;
	            case '\f':
	                buf.append("\\f");
	                break;
	            case '\r':
	                buf.append("\\r");
	                break;
	            default:
	                /*
					We shouldn't need to encode special characters other than the above, all others should be handled by utf-8 encoding
	                if(c<' ' || (c>= '\u0080' && c<'\u00a0') || (c>='\u2000' && c<'\u2100')){
	                    String tmp="000"+Integer.toHexString(c);
	                    buf.append("\\u"+tmp.substring(tmp.length()-4));
	                }else{*/
	                   buf.append(c);
	                //}
	        }
	    }
        return buf.toString();
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

	protected abstract String serializeElementToString();

	/**
	 * Returns a raw string extracted from the source string that covers the
	 * start and end index of this element.
	 *
	 * @return as string representation of this object as given in the source string
	 */
	public String toString(){
		if(root.isDirty()){
			return serializeElementToString();
		}else{
			return new String(root.cbuf,root.startIndex,root.endIndex-root.startIndex);
		}
	}

	/**
	 * Returns the character count of the source string.
	 * 
	 * @return the length of the source string for this element
	 */
	public int getSourceLength(){
		return root.endIndex-root.startIndex;
	}

	public byte[] toByteArray(){
		int size=root.getBufferSize();
		ByteBuffer buf=ByteBuffer.allocate(size);
		root.writeToBuffer(buf);
		return buf.array();
	}

	public abstract int hashCode();

	private boolean equalObjects(LazyObject o1,LazyObject o2){
		if(o1.length()!=o2.length())return false;
		for(String key:o1.keySet()){
			if(!o2.has(key))return false;
			LazyType t1=o1.getType(key);
			if(t1!=o2.getType(key))return false;
			switch(t1){
				// NULL, implicitly true
				case STRING:if(!o1.getString(key).equals(o2.getString(key)))return false;
					break;
				case INTEGER:if(o1.getLong(key)!=o2.getLong(key))return false;
					break;
				case FLOAT:if(o1.getDouble(key)!=o2.getDouble(key))return false;
					break;
				case BOOLEAN:if(o1.getBoolean(key)!=o2.getBoolean(key))return false;
					break;
				case OBJECT:if(!o1.getJSONObject(key).equals(o2.getJSONObject(key)))return false;
					break;
				case ARRAY:if(!o1.getJSONArray(key).equals(o2.getJSONArray(key)))return false;
					break;
			}
		}
		return true;
	}

	private boolean equalArrays(LazyArray a1,LazyArray a2){
		if(a1.length()!=a2.length())return false;
		for(int i=0;i<length();i++){
			LazyType t1=a1.getType(i);
			if(t1!=a2.getType(i))return false;
			switch(t1){
				// NULL, implicitly true
				case STRING:if(!a1.getString(i).equals(a2.getString(i)))return false;
					break;
				case INTEGER:if(a1.getLong(i)!=a2.getLong(i))return false;
					break;
				case FLOAT:if(a1.getDouble(i)!=a2.getDouble(i))return false;
					break;
				case BOOLEAN:if(a1.getBoolean(i)!=a2.getBoolean(i))return false;
					break;
				case OBJECT:if(!a1.getJSONObject(i).equals(a2.getJSONObject(i)))return false;
					break;
				case ARRAY:if(!a1.getJSONArray(i).equals(a2.getJSONArray(i)))return false;
					break;
			}
		}
		return true;
	}

	public boolean equals(Object obj){
		// Verify both are LazyElement
		if(!(obj instanceof LazyElement))return false;
		LazyElement el=(LazyElement)obj;
		// Verify both are same type
		if(el.getType()!=getType())return false;
		// Do a deep comparison
		if(getType()==LazyType.OBJECT){
			return equalObjects((LazyObject)this,(LazyObject)el);
		}else{
			return equalArrays((LazyArray)this,(LazyArray)el);
		}
	}
}