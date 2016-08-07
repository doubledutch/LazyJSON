package me.doubledutch.lazyjson;

import java.util.*;
import java.nio.ByteBuffer;

/**
 * The LazyNode is the primary output of the LazyParser.
 * It should probably be named LazyNode instead of LazyNode, but as the
 * project evolved, the name stuck and I have ironically been too lazy to
 * change it!
 */
public final class LazyNode{
	// Token types used for classification during parsing
	protected static final byte OBJECT=0;
	protected static final byte ARRAY=1;
	protected static final byte FIELD=2;
	// protected static final byte VALUE=3;
	protected static final byte VALUE_TRUE=4;
	protected static final byte VALUE_FALSE=5;
	protected static final byte VALUE_NULL=6;
	protected static final byte VALUE_STRING=7;
	protected static final byte VALUE_NUMBER=8;

	protected static final byte END_MARKER=9;

	protected final byte type;

	// Start and end index into source string for this token.
	// For an object or array, the end index will be the end of the entire
	// object or array.
	protected final int startIndex;
	protected int endIndex=-1;

	// When strings are parsed we make a note of any escaped characters.
	// This lets us do a quick char copy when accessing string values that
	// do not have any escaped characters
	// When numbers are parsed, we use the same field to mark floating point
	// characters.
	protected boolean modified=false;

	// Children are stored as a linked list by maintaining the first and last
	// child of this token, as well as a link to the next sibling
	protected LazyNode child;
	protected LazyNode lastChild;
	protected LazyNode next;

	/**
	 * Construct a new LazyNode with the given type and index into the source string
	 *
	 * @param type the type of this token
	 * @param startIndex the index into the source string where this token was found
	 */
	protected LazyNode(byte type,int startIndex){
		this.startIndex=startIndex;
		this.type=type;
	}

	/**
	 * Add a new child to the current linked list of child tokens
	 *
	 * @param token the child to add
	 */
	protected void addChild(LazyNode token){
		// If no children have been added yet, lastChild will be null
		if(lastChild==null){
			child=token;
			lastChild=token;
			return;
		}
		// Set the next pointer on the current end of the child list and set last child to the given token
		lastChild.next=token;
		lastChild=token;
	}

	/**
	 * Count the children attached to this token. Be aware that this requires actual linked list traversal!
	 *
	 * @return the number of child tokens attached to this token
	 */
	protected int getChildCount(){
		if(child==null){
			return 0;
		}
		int num=0;
		LazyNode token=child;
		while(token!=null){
			num++;
			token=token.next;
		}
		return num;
	}

	/**
	 * Convenience method to create a new token with the type set to array and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyNode cArray(int index){
		return new LazyNode(ARRAY,index);
	}

	/**
	 * Convenience method to create a new token with the type set to object and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyNode cObject(int index){
		return new LazyNode(OBJECT,index);
	}

	/**
	 * Convenience method to create a new token with the type set to field and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyNode cField(int index){
		return new LazyNode(FIELD,index);
	}

	/**
	 * Convenience method to create a new token with the type set to string value and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyNode cStringValue(int index){
		return new LazyNode(VALUE_STRING,index);
	}

	/**
	 * Convenience method to create a new token with the type set to number value and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyNode cNumberValue(int index){
		return new LazyNode(VALUE_NUMBER,index);
	}

	/**
	 * Convenience method to create a new token with the type set to a boolean true value and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyNode cValueTrue(int index){
		return new LazyNode(VALUE_TRUE,index);
	}

	/**
	 * Convenience method to create a new token with the type set to a boolean false value and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyNode cValueFalse(int index){
		return new LazyNode(VALUE_FALSE,index);
	}

	/**
	 * Convenience method to create a new token with the type set to a null value and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyNode cValueNull(int index){
		return new LazyNode(VALUE_NULL,index);
	}

	/**
	 * Parses the characters of this token and attempts to construct an integer
	 * value from them.
	 *
	 * @param source the source character array for this token
	 * @return the integer value if it could be parsed
	 * @throws LazyException if the value could not be parsed
	 */
	protected int getIntValue(char[] source) throws LazyException{
		if(type!=VALUE_NUMBER || modified)throw new LazyException("Not an integer",startIndex);
		int i=startIndex;
		boolean sign=false;
		if(source[i]=='-'){
			sign=true;
			i++;
		}
		int value=0;
		for(;i<endIndex;i++){
			char c=source[i];
			// If we only allow this to be called on integer values, the parsing is pre done!
			// if(c<'0'||c>'9')throw new LazyException("'"+getStringValue(source)+"' is not a valid integer",startIndex);
			value+='0'-c;
			if(i+1<endIndex){
				value*=10;
			}
		}
		return sign?value:-value;
	}

	/**
	 * Parses the characters of this token and attempts to construct a long
	 * value from them.
	 *
	 * @param source the source character array for this token
	 * @return the long value if it could be parsed
	 * @throws LazyException if the value could not be parsed
	 */
	protected long getLongValue(char[] source) throws LazyException{
		if(type!=VALUE_NUMBER || modified)throw new LazyException("Not a long",startIndex);
		int i=startIndex;
		boolean sign=false;
		if(source[i]=='-'){
			sign=true;
			i++;
		}
		long value=0;
		for(;i<endIndex;i++){
			char c=source[i];
			value+='0'-c;
			if(i+1<endIndex){
				value*=10;
			}
		}
		return sign?value:-value;
	}

	/**
	 * Parses the characters of this token and attempts to construct a double
	 * value from them.
	 *
	 * @param source the source character array for this token
	 * @return the double value if it could be parsed
	 * @throws LazyException if the value could not be parsed
	 */
	protected double getDoubleValue(char[] source) throws LazyException{
		String str=getStringValue(source);
		try{
			double d=Double.parseDouble(str);
			return d;
		}catch(NumberFormatException nfe){
			throw new LazyException("'"+str+"' is not a valid double",startIndex);
		}
	}

	/**
	 * Extracts a string containing the characters given by this token. If the
	 * token was marked as having escaped characters, they will be unescaped
	 * before the value is returned.
	 *
	 * @param source the source character array for this token
	 * @return the string value held by this token
	 */
	protected String getStringValue(char[] source){
		if(!modified){
			return new String(source,startIndex,endIndex-startIndex);
		}else{
			StringBuilder buf=new StringBuilder(endIndex-startIndex);
			for(int i=startIndex;i<endIndex;i++){
				char c=source[i];
				if(c=='\\'){
					i++;
					c=source[i];
					if(c=='"' || c=='\\' || c=='/'){
						buf.append(c);
					}else if(c=='b'){
						buf.append('\b');
					}else if(c=='f'){
						buf.append('\f');
					}else if(c=='n'){
						buf.append('\n');
					}else if(c=='r'){
						buf.append('\r');
					}else if(c=='t'){
						buf.append('\t');
					}else if(c=='u'){
						String code=new String(source,i+1,4);
						buf.append((char)Integer.parseInt(code, 16));
						i+=4;
					}
				}else{
					buf.append(c);
				}
			}
			return buf.toString();
		}
	}

	/**
	 * Returns a string iterator for this tokens children.
	 *
	 * @param cbuf the source character array for this token
	 * @return an iterator for the children of this token as strings
	 */
	protected Iterator<String> getStringIterator(char[] cbuf){
		return new StringIterator(this,cbuf);
	}

	/*
	// Debug method used for development purposes only

	protected String toString(int pad){
		String out="";
		for(int i=0;i<pad;i++)out+=" ";
		if(type==OBJECT){
			out+="{";
		}else if(type==ARRAY){
			out+="[";
		}else if(type==FIELD){
			out+="\"";
		}else if(type==VALUE){
			out+="V";
		}
		out+=":["+startIndex+","+endIndex+"]";
		out+="\n";
		if(child!=null){
			LazyNode token=child;
			while(token!=null){
				out+=token.toString(pad+2);
				token=token.next;
			}
		}else{
			if(child!=null){
				out+=child.toString(pad+2);
			}
		}
		return out;
	}*/

	// Internal class used to iterate over children as strings
	private final class StringIterator implements Iterator<String>{
		private LazyNode next;
		private char[] cbuf;

		protected StringIterator(LazyNode token,char[] cbuf){
			next=token.child;
			this.cbuf=cbuf;
		}

		public boolean hasNext(){
			return next!=null;
		}

		public void remove() throws UnsupportedOperationException{
			throw new UnsupportedOperationException("Can't remove from token");
		}

		public String next() throws NoSuchElementException{
			if(hasNext()){
				String value=next.getStringValue(cbuf);
				next=next.next; // If only I could squeeze one more "next" into this statement
				return value;
			}
			throw new NoSuchElementException();
		}
	}

	protected static LazyNode readFromBuffer(byte[] raw){
		ByteBuffer buf=ByteBuffer.wrap(raw);
		return readFromBuffer(buf);
	}

	protected static LazyNode readFromBuffer(ByteBuffer buf){
		byte type=buf.get();
		if(type==END_MARKER)return null;
		int startIndex=buf.getInt();
		int endIndex=buf.getInt();
		byte modified=buf.get();
		// TODO: add constructor for this purpose
		LazyNode node=new LazyNode(type,startIndex);
		node.endIndex=endIndex; 
		node.modified=modified==0;
		if(type==OBJECT || type==ARRAY){
			LazyNode child=readFromBuffer(buf);
			node.child=child;
			node.lastChild=child;
			child=readFromBuffer(buf);
			while(child!=null){
				node.lastChild.next=child;
				node.lastChild=child;
				child=readFromBuffer(buf);
			}
		}else if(type==FIELD){
			LazyNode child=readFromBuffer(buf);
			node.child=child;
			node.lastChild=child;
		}
		return node;
	}

	protected void writeToBuffer(ByteBuffer buf){
		// ByterBuffer must be allocated with enough space before calling
		buf.put(type);
		buf.putInt(startIndex);
		buf.putInt(endIndex);
		if(modified){
			buf.put((byte)0);
		}else{
			buf.put((byte)1);
		}
		if(type==OBJECT || type==ARRAY){
			LazyNode n=child;
			while(n!=null){
				n.writeToBuffer(buf);
				n=n.next;
			}
			buf.put(END_MARKER);
		}else if(type==FIELD){
			child.writeToBuffer(buf);
		}
	}



	protected int getBufferSize(){
		int size=1+4+4+1; // type, start and end index, modifier
		if(type==OBJECT || type==ARRAY){
			LazyNode n=child;
			while(n!=null){
				size+=n.getBufferSize();
				n=n.next;
			}
			size+=1;
		}else if(type==FIELD){
			size+=child.getBufferSize();
		}
		return size;
	}


}