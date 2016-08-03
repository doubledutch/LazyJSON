package me.doubledutch.lazyjson;

import java.util.*;

/**
 * The LazyToken is the primary output of the LazyParser.
 * It should probably be named LazyNode instead of LazyToken, but as the
 * project evolved, the name stuck and I have ironically been too lazy to
 * change it!
 */
public final class LazyToken{
	// Token types used for classification during parsing
	protected static final byte OBJECT=0;
	protected static final byte ARRAY=1;
	protected static final byte FIELD=2;
	protected static final byte VALUE=3;
	protected static final byte VALUE_TRUE=4;
	protected static final byte VALUE_FALSE=5;
	protected static final byte VALUE_NULL=6;
	protected static final byte VALUE_STRING=7;
	protected static final byte VALUE_INTEGER=8;
	protected static final byte VALUE_FLOAT=9;
	protected final byte type;

	// Start and end index into source string for this token.
	// For an object or array, the end index will be the end of the entire
	// object or array.
	protected final int startIndex;
	protected int endIndex=-1;

	// When strings are parsed we make a note of any escaped characters.
	// This lets us do a quick char copy when accessing string values that
	// do not have any escaped characters
	protected boolean escaped=false;

	// Children are stored as a linked list by maintaining the first and last
	// child of this token, as well as a link to the next sibling
	protected LazyToken child;
	protected LazyToken lastChild;
	protected LazyToken next;

	/**
	 * Construct a new LazyToken with the given type and index into the source string
	 *
	 * @param type the type of this token
	 * @param startIndex the index into the source string where this token was found
	 */
	protected LazyToken(byte type,int startIndex){
		this.startIndex=startIndex;
		this.type=type;
	}

	/**
	 * Add a new child to the current linked list of child tokens
	 *
	 * @param token the child to add
	 */
	protected void addChild(LazyToken token){
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
		LazyToken token=child;
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
	protected static LazyToken cArray(int index){
		return new LazyToken(ARRAY,index);
	}

	/**
	 * Convenience method to create a new token with the type set to object and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyToken cObject(int index){
		return new LazyToken(OBJECT,index);
	}

	/**
	 * Convenience method to create a new token with the type set to field and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyToken cField(int index){
		return new LazyToken(FIELD,index);
	}

	/**
	 * Convenience method to create a new token with the type set to value and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyToken cValue(int index){
		return new LazyToken(VALUE,index);
	}

	/**
	 * Convenience method to create a new token with the type set to string value and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyToken cStringValue(int index){
		return new LazyToken(VALUE_STRING,index);
	}

	/**
	 * Convenience method to create a new token with the type set to integer value and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyToken cIntValue(int index){
		return new LazyToken(VALUE_INTEGER,index);
	}

	/**
	 * Convenience method to create a new token with the type set to float value and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyToken cFloatValue(int index){
		return new LazyToken(VALUE_FLOAT,index);
	}

	/**
	 * Convenience method to create a new token with the type set to a boolean true value and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyToken cValueTrue(int index){
		return new LazyToken(VALUE_TRUE,index);
	}

	/**
	 * Convenience method to create a new token with the type set to a boolean false value and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyToken cValueFalse(int index){
		return new LazyToken(VALUE_FALSE,index);
	}

	/**
	 * Convenience method to create a new token with the type set to a null value and
	 * with the starting index set to the given index.
	 *
	 * @param index the starting index for this token
	 * @return a new token
	 */
	protected static LazyToken cValueNull(int index){
		return new LazyToken(VALUE_NULL,index);
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
		int i=startIndex;
		boolean sign=false;
		if(source[i]=='-'){
			sign=true;
			i++;
		}
		int value=0;
		for(;i<endIndex;i++){
			char c=source[i];
			if(c<'0'||c>'9')throw new LazyException("'"+getStringValue(source)+"' is not a valid integer",startIndex);
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
		int i=startIndex;
		boolean sign=false;
		if(source[i]=='-'){
			sign=true;
			i++;
		}
		long value=0;
		for(;i<endIndex;i++){
			char c=source[i];
			if(c<'0'||c>'9')throw new LazyException("'"+getStringValue(source)+"' is not a valid integer",startIndex);
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
		if(!escaped){
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
			LazyToken token=child;
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
		private LazyToken next;
		private char[] cbuf;

		protected StringIterator(LazyToken token,char[] cbuf){
			next=token.child;
			this.cbuf=cbuf;
		}

		public boolean hasNext(){
			return next!=null;
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
}