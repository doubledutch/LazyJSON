package me.doubledutch.lazyjson;

/**
 * An array used to parse and inspect JSON data given in the form of a string.
 */
public class LazyArray extends LazyElement{
	// Stored traversal location for fast in order traversals
	private LazyNode selectToken=null;
	private int selectInt=-1;

	/**
	 * Create a new Lazy JSON array based on the JSON representation in the given string.
	 *
	 * @param raw the input string
	 * @throws LazyException if the string could not be parsed as a JSON array
	 */
	public LazyArray(String raw) throws LazyException{
		LazyParser parser=new LazyParser(raw);
		parser.tokenize();	
		if(parser.root.type!=LazyNode.ARRAY){
			throw new LazyException("JSON Array must start with [",0);
		}
		root=parser.root;
	}

	public LazyArray() throws LazyException{
		LazyParser parser=new LazyParser("[]");
		parser.tokenize();	
		root=parser.root;
	}

	protected LazyArray(LazyNode root){
		super(root);
	}
	/*
	protected LazyArray(LazyNode root,char[] source){
		super(root,source,null);
	}*/

	protected String serializeElementToString(){
		StringBuilder buf=new StringBuilder();
		buf.append("[");
		LazyNode pointer=root.child;
		boolean first=true;
		while(pointer!=null){
			if(first){
				first=false;
			}else{
				buf.append(",");
			}
			if(pointer.type==LazyNode.OBJECT){
				buf.append(new LazyObject(pointer).toString());
			}else if(pointer.type==LazyNode.ARRAY){
				buf.append(new LazyArray(pointer).toString());
			}else if(pointer.type==LazyNode.VALUE_STRING){
				buf.append("\"");
				buf.append(pointer.getStringValue());
				buf.append("\"");
			}else if (pointer.type==LazyNode.VALUE_ESTRING){
				buf.append("\"");
				buf.append(pointer.getRawStringValue());
				buf.append("\"");
			}else if(pointer.type==LazyNode.VALUE_TRUE){
				buf.append("true");
			}else if(pointer.type==LazyNode.VALUE_FALSE){
				buf.append("false");
			}else if(pointer.type==LazyNode.VALUE_NULL){
				buf.append("null");
			}else{
				buf.append(pointer.getStringValue());
			}
			pointer=pointer.next;
		}
		buf.append("]");
		return buf.toString();
	}

	/**
	 * Returns the type of this element.
	 *
	 * @return LazyType.ARRAY
	 */
	public LazyType getType(){
		return LazyType.ARRAY;
	}

	public int hashCode(){
		int code=2;
		for(int i=0;i<length();i++){
			LazyType t1=getType(i);
			switch(t1){
				case STRING:code+=getString(i).hashCode();
					break;
				case INTEGER:
						long l=getLong(i);
						code+=(int)(l ^ (l >>> 32));
					break;
				case FLOAT:
						double d=getDouble(i);
						l=Double.doubleToLongBits(d);
						code+=(int)(l ^ (l >>> 32));
					break;
				case BOOLEAN:
						if(getBoolean(i)){
							code+=1;
						}
					break;
				case OBJECT:code+=37*getJSONObject(i).hashCode();
					break;
				case ARRAY:code+=37*getJSONArray(i).hashCode();
					break;
			}
		}
		return code;
	}

	/**
	 * Returns the value type of the given field.
	 *
	 * @param index the requested field
	 * @return the type of the value for the given index
	 * @throws LazyException if the requested index did not exist
	 */
	public LazyType getType(int index) throws LazyException{
		LazyNode token=getValueToken(index);
		switch(token.type){
			case LazyNode.OBJECT: return LazyType.OBJECT;
			case LazyNode.ARRAY: return LazyType.ARRAY;
			case LazyNode.VALUE_TRUE: return LazyType.BOOLEAN;
			case LazyNode.VALUE_FALSE: return LazyType.BOOLEAN;
			case LazyNode.VALUE_NULL: return LazyType.NULL;
			case LazyNode.VALUE_STRING: return LazyType.STRING;
			case LazyNode.VALUE_ESTRING: return LazyType.STRING;
			case LazyNode.VALUE_INTEGER: return LazyType.INTEGER;
			case LazyNode.VALUE_FLOAT: return LazyType.FLOAT;
		}
		return null;
	}

	public Object get(int index) throws LazyException{
		LazyNode token=getValueToken(index);
		if(token!=null){
			switch(token.type){
				case LazyNode.OBJECT: LazyObject obj=new LazyObject(token);
									  obj.parent=this;
									  return obj;
				case LazyNode.ARRAY: LazyArray arr= new LazyArray(token);
									 arr.parent=this;
									 return arr;
				case LazyNode.VALUE_TRUE: return (Boolean)true;
				case LazyNode.VALUE_FALSE: return (Boolean)false;
				case LazyNode.VALUE_NULL: return LazyObject.NULL;
				case LazyNode.VALUE_STRING: return token.getStringValue();
				case LazyNode.VALUE_ESTRING: return token.getStringValue();
				case LazyNode.VALUE_INTEGER: return (Long)token.getLongValue();
				case LazyNode.VALUE_FLOAT: return (Double)token.getDoubleValue();
			}
		}
		// Should never happen
		return null;
	}

	public Object opt(int index) throws LazyException{
		LazyNode token=getOptionalValueToken(index);
		if(token!=null){
			switch(token.type){
				case LazyNode.OBJECT: LazyObject obj=new LazyObject(token);
									  obj.parent=this;
									  return obj;
				case LazyNode.ARRAY: LazyArray arr= new LazyArray(token);
									 arr.parent=this;
									 return arr;
				case LazyNode.VALUE_TRUE: return (Boolean)true;
				case LazyNode.VALUE_FALSE: return (Boolean)false;
				case LazyNode.VALUE_NULL: return LazyObject.NULL;
				case LazyNode.VALUE_STRING: return token.getStringValue();
				case LazyNode.VALUE_ESTRING: return token.getStringValue();
				case LazyNode.VALUE_INTEGER: return (Long)token.getLongValue();
				case LazyNode.VALUE_FLOAT: return (Double)token.getDoubleValue();
			}
		}
		return null;
	}

	private void appendChild(LazyNode token) throws LazyException{
		if(root.child==null){
			root.child=token;
			root.lastChild=token;
		}else{
			root.lastChild.next=token;
			root.lastChild=token;
		}
		root.dirty=true;
		selectToken=null;
		selectInt=-1;
	}

	private void insertChild(int index,LazyNode token) throws LazyException{
		root.dirty=true;
		if(index==0){
			token.next=root.child;
			root.child=token;
			return;
		}
		int current=1;
		LazyNode pointer=root.child;
		if(pointer==null)throw new LazyException("Trying to put at index "+index+" on an empty LazyArray");
		while(current<index){
			current++;
			pointer=pointer.next;
			if(pointer==null)throw new LazyException("Index out of bounds "+index);
		}
		token.next=pointer.next;
		pointer.next=token;
		selectToken=null;
		selectInt=-1;
	}

	public LazyArray put(String value) throws LazyException{
		LazyNode child=null;
		if(shouldQuoteString(value)){
			child=appendAndSetDirtyString(LazyNode.VALUE_ESTRING,quoteString(value));
		}else{
			child=appendAndSetDirtyString(LazyNode.VALUE_STRING,value);
		}
		appendChild(child);
		return this;
	}

	public LazyArray put(int value) throws LazyException{
		LazyNode child=appendAndSetDirtyString(LazyNode.VALUE_INTEGER,Integer.toString(value));
		appendChild(child);
		return this;
	}

	public LazyArray put(long value) throws LazyException{
		LazyNode child=appendAndSetDirtyString(LazyNode.VALUE_INTEGER,Long.toString(value));
		appendChild(child);
		return this;
	}

	public LazyArray put(float value) throws LazyException{
		LazyNode child=appendAndSetDirtyString(LazyNode.VALUE_FLOAT,Float.toString(value));
		appendChild(child);
		return this;
	}

	public LazyArray put(double value) throws LazyException{
		LazyNode child=appendAndSetDirtyString(LazyNode.VALUE_FLOAT,Double.toString(value));
		appendChild(child);
		return this;
	}

	public LazyArray put(boolean value) throws LazyException{
		LazyNode child=null;
		if(value){
			child=LazyNode.cValueTrue(-1);
		}else{
			child=LazyNode.cValueFalse(-1);
		}
		child.dirty=true;
		appendChild(child);
		return this;
	}

	public LazyArray put(LazyArray value) throws LazyException{
		appendChild(value.root);
		return this;
	}

	public LazyArray put(LazyObject value) throws LazyException{
		appendChild(value.root);
		return this;
	}

	public LazyArray put(Object value) throws LazyException{
		if(value==LazyObject.NULL){
			LazyNode child=LazyNode.cValueNull(-1);
			child.dirty=true;
			appendChild(child);
			return this;
		}else if(value==null){
			// TODO: hmmm... maybe throw exception? or also put LazyObject.NULL?
		}
		if(value instanceof java.lang.Integer){
			return put(((Integer)value).intValue());
		}
		if(value instanceof java.lang.Long){
			return put(((Long)value).longValue());
		}
		if(value instanceof java.lang.Float){
			return put(((Float)value).floatValue());
		}
		if(value instanceof java.lang.Double){
			return put(((Double)value).doubleValue());
		}
		if(value instanceof java.lang.Boolean){
			return put(((Boolean)value).booleanValue());
		}
		if(value instanceof java.lang.String){
			return put((String)value);
		}
		if(value instanceof me.doubledutch.lazyjson.LazyObject){
			return put((LazyObject)value);
		}
		if(value instanceof me.doubledutch.lazyjson.LazyArray){
			return put((LazyArray)value);
		}
		throw new LazyException("Unsupported object type");
	}

	public LazyArray put(int index,Object value) throws LazyException{
		if(value==LazyObject.NULL){
			LazyNode child=LazyNode.cValueNull(-1);
			child.dirty=true;
			insertChild(index,child);
			return this;
		}else if(value==null){
			// TODO: hmmm... maybe throw exception? or also put LazyObject.NULL?
		}
		if(value instanceof java.lang.Integer){
			return put(index,((Integer)value).intValue());
		}
		if(value instanceof java.lang.Long){
			return put(index,((Long)value).longValue());
		}
		if(value instanceof java.lang.Float){
			return put(index,((Float)value).floatValue());
		}
		if(value instanceof java.lang.Double){
			return put(index,((Double)value).doubleValue());
		}
		if(value instanceof java.lang.Boolean){
			return put(index,((Boolean)value).booleanValue());
		}
		if(value instanceof java.lang.String){
			return put(index,(String)value);
		}
		if(value instanceof me.doubledutch.lazyjson.LazyObject){
			return put(index,(LazyObject)value);
		}
		if(value instanceof me.doubledutch.lazyjson.LazyArray){
			return put(index,(LazyArray)value);
		}
		throw new LazyException("Unsupported object type");
	}

	public LazyArray put(int index,String value) throws LazyException{
		LazyNode child=null;
		if(shouldQuoteString(value)){
			child=appendAndSetDirtyString(LazyNode.VALUE_ESTRING,quoteString(value));
		}else{
			child=appendAndSetDirtyString(LazyNode.VALUE_STRING,value);
		}
		insertChild(index,child);
		return this;
	}

	public LazyArray put(int index,int value) throws LazyException{
		LazyNode child=appendAndSetDirtyString(LazyNode.VALUE_INTEGER,Integer.toString(value));
		insertChild(index,child);
		return this;
	}

	public LazyArray put(int index,long value) throws LazyException{
		LazyNode child=appendAndSetDirtyString(LazyNode.VALUE_INTEGER,Long.toString(value));
		insertChild(index,child);
		return this;
	}

	public LazyArray put(int index,float value) throws LazyException{
		LazyNode child=appendAndSetDirtyString(LazyNode.VALUE_FLOAT,Float.toString(value));
		insertChild(index,child);
		return this;
	}

	public LazyArray put(int index,double value) throws LazyException{
		LazyNode child=appendAndSetDirtyString(LazyNode.VALUE_FLOAT,Double.toString(value));
		insertChild(index,child);
		return this;
	}

	public LazyArray put(int index,boolean value) throws LazyException{
		LazyNode child=null;
		if(value){
			child=LazyNode.cValueTrue(-1);
		}else{
			child=LazyNode.cValueFalse(-1);
		}
		child.dirty=true;
		insertChild(index,child);
		return this;
	}

	public LazyArray put(int index,LazyArray value) throws LazyException{
		insertChild(index,value.root);
		return this;
	}

	public LazyArray put(int index,LazyObject value) throws LazyException{
		insertChild(index,value.root);
		return this;
	}

	public Object remove(int index) throws LazyException{
		Object obj=opt(index); // TODO: should this be get instead of opt?
		LazyNode token=getOptionalValueToken(index);
		if(token!=null){
			// System.out.println("found the token!");
			LazyNode pointer=this.root.child;
			if(pointer==token){
				// System.out.println("yes, it was the first");
				root.child=token.next;
			}else{
				while(pointer!=null){
					if(pointer.next==token){
						pointer.next=token.next;
					}
					pointer=pointer.next;
				}
			}
			root.dirty=true;
		}
		selectToken=null;
		selectInt=-1;
		return obj;
	}

	/**
	 * Returns the JSON array stored at the given index.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a JSON array
	 * @throws LazyException if the index is out of bounds
	 */
	public LazyArray getJSONArray(int index) throws LazyException{
		LazyNode token=getValueToken(index);
		if(token.type!=LazyNode.ARRAY)throw new LazyException("Requested value is not an array",token);
		LazyArray arr= new LazyArray(token);
		arr.parent=this;
		return arr;
	}

	/**
	 * Returns the JSON array stored at the given index.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a JSON array or null if there was no such value
	 * @throws LazyException if the index is out of bounds
	 */
	public LazyArray optJSONArray(int index) throws LazyException{
		LazyNode token=getOptionalValueToken(index);
		if(token==null)return null;
		if(token.type==LazyNode.VALUE_NULL)return null;
		if(token.type!=LazyNode.ARRAY)return null;
		LazyArray arr= new LazyArray(token);
		arr.parent=this;
		return arr;
	}

	/**
	 * Returns the JSON object stored at the given index.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a JSON object
	 * @throws LazyException if the index is out of bounds
	 */
	public LazyObject getJSONObject(int index) throws LazyException{
		LazyNode token=getValueToken(index);
		if(token.type!=LazyNode.OBJECT)throw new LazyException("Requested value is not an object",token);
		LazyObject obj= new LazyObject(token);
		obj.parent=this;
		return obj;
	}

	/**
	 * Returns the JSON object stored at the given index.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a JSON object or null if there was no such value
	 * @throws LazyException if the index is out of bounds
	 */
	public LazyObject optJSONObject(int index) throws LazyException{
		LazyNode token=getOptionalValueToken(index);
		if(token==null)return null;
		if(token.type==LazyNode.VALUE_NULL)return null;
		if(token.type!=LazyNode.OBJECT)return null;
		LazyObject obj= new LazyObject(token);
		obj.parent=this;
		return obj;
	}

	/**
	 * Returns the boolean value stored at the given index.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a boolean
	 * @throws LazyException if the index is out of bounds
	 */
	public boolean getBoolean(int index){
		LazyNode token=getValueToken(index);
		if(token.type==LazyNode.VALUE_TRUE)return true;
		if(token.type==LazyNode.VALUE_FALSE)return false;
		throw new LazyException("Requested value is not a boolean",token);
	}

	/**
	 * Returns the boolean value stored at the given index or null if there was no such value.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a boolean or null if there was no such value
	 * @throws LazyException if the index is out of bounds
	 */
	public boolean optBoolean(int index){
		LazyNode token=getOptionalValueToken(index);
		if(token==null)return false;
		if(token.type==LazyNode.VALUE_NULL)return false;
		if(token.type==LazyNode.VALUE_TRUE)return true;
		if(token.type==LazyNode.VALUE_FALSE)return false;
		throw new LazyException("Requested value is not a boolean",token);
	}

	/**
	 * Returns the boolean value stored at the given index or the default value if there was no such value.
	 *
	 * @param index the location of the value in this array
	 * @param defaultValue the default value
	 * @return the value if it could be parsed as a boolean or the default value if there was no such value
	 * @throws LazyException if the index is out of bounds
	 */
	public boolean optBoolean(int index,boolean defaultValue){
		LazyNode token=getOptionalValueToken(index);
		if(token==null)return defaultValue;
		if(token.type==LazyNode.VALUE_NULL)return defaultValue;
		if(token.type==LazyNode.VALUE_TRUE)return true;
		if(token.type==LazyNode.VALUE_FALSE)return false;
		throw new LazyException("Requested value is not a boolean",token);
	}

	/**
	 * Returns the string value stored at the given index.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a string
	 * @throws LazyException if the index is out of bounds
	 */
	public String getString(int index) throws LazyException{
		LazyNode token=getValueToken(index);
		return token.getStringValue();
	}

	/**
	 * Returns the string value stored at the given index or null if there was no such value.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a boolean or null if there was no such value
	 * @throws LazyException if the index is out of bounds
	 */
	public String optString(int index){
		LazyNode token=getOptionalValueToken(index);
		if(token==null)return null;
		if(token.type==LazyNode.VALUE_NULL)return null;
		return token.getStringValue();
	}

	/**
	 * Returns the string value stored at the given index or the default value if there was no such value.
	 *
	 * @param index the location of the value in this array
	 * @param defaultValue the default value
	 * @return the value if it could be parsed as a string or the default value if there was no such value
	 * @throws LazyException if the index is out of bounds
	 */
	public String optString(int index,String defaultValue){
		LazyNode token=getOptionalValueToken(index);
		if(token==null)return defaultValue;
		if(token.type==LazyNode.VALUE_NULL)return defaultValue;
		return token.getStringValue();
	}

	/**
	 * Returns the int value stored at the given index.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as an int
	 * @throws LazyException if the index is out of bounds
	 */
	public int getInt(int index) throws LazyException{
		LazyNode token=getValueToken(index);
		return token.getIntValue();
	}

	/**
	 * Returns the int value stored at the given index or 0 if there was no such value.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a boolean or null if there was no such value
	 * @throws LazyException if the index is out of bounds
	 */
	public int optInt(int index){
		LazyNode token=getOptionalValueToken(index);
		if(token==null)return 0;
		if(token.type==LazyNode.VALUE_NULL)return 0;
		return token.getIntValue();
	}

	/**
	 * Returns the int value stored at the given index or the default value if there was no such value.
	 *
	 * @param index the location of the value in this array
	 * @param defaultValue the default value
	 * @return the value if it could be parsed as a string or the default value if there was no such value
	 * @throws LazyException if the index is out of bounds
	 */
	public int optInt(int index,int defaultValue){
		LazyNode token=getOptionalValueToken(index);
		if(token==null)return defaultValue;
		if(token.type==LazyNode.VALUE_NULL)return defaultValue;
		return token.getIntValue();
	}

	/**
	 * Returns the long value stored at the given index.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a long
	 * @throws LazyException if the index is out of bounds
	 */
	public long getLong(int index) throws LazyException{
		LazyNode token=getValueToken(index);
		return token.getLongValue();
	}

	/**
	 * Returns the long value stored at the given index or 0 if there was no such value.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a boolean or null if there was no such value
	 * @throws LazyException if the index is out of bounds
	 */
	public long optLong(int index){
		LazyNode token=getOptionalValueToken(index);
		if(token==null)return 0l;
		if(token.type==LazyNode.VALUE_NULL)return 0l;
		return token.getLongValue();
	}

	/**
	 * Returns the long value stored at the given index or the default value if there was no such value.
	 *
	 * @param index the location of the value in this array
	 * @param defaultValue the default value
	 * @return the value if it could be parsed as a string or the default value if there was no such value
	 * @throws LazyException if the index is out of bounds
	 */
	public long optLong(int index,long defaultValue){
		LazyNode token=getOptionalValueToken(index);
		if(token==null)return defaultValue;
		if(token.type==LazyNode.VALUE_NULL)return defaultValue;
		return token.getLongValue();
	}

	/**
	 * Returns the double value stored at the given index.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a double
	 * @throws LazyException if the index is out of bounds
	 */
	public double getDouble(int index) throws LazyException{
		LazyNode token=getValueToken(index);
		return token.getDoubleValue();
	}

	/**
	 * Returns the double value stored at the given index or 0.0 if there was no such value.
	 *
	 * @param index the location of the value in this array
	 * @return the value if it could be parsed as a boolean or null if there was no such value
	 * @throws LazyException if the index is out of bounds
	 */
	public double optDouble(int index){
		LazyNode token=getOptionalValueToken(index);
		if(token==null)return 0.0;
		if(token.type==LazyNode.VALUE_NULL)return 0.0;
		return token.getDoubleValue();
	}

	/**
	 * Returns the double value stored at the given index or the default value if there was no such value.
	 *
	 * @param index the location of the value in this array
	 * @param defaultValue the default value
	 * @return the value if it could be parsed as a string or the default value if there was no such value
	 * @throws LazyException if the index is out of bounds
	 */
	public double optDouble(int index,double defaultValue){
		LazyNode token=getOptionalValueToken(index);
		if(token==null)return defaultValue;
		if(token.type==LazyNode.VALUE_NULL)return defaultValue;
		return token.getDoubleValue();
	}

	/**
	 * Returns true if the value stored at the given index is null.
	 *
	 * @param index the location of the value in this array
	 * @return true if the value is null, false otherwise
	 * @throws LazyException if the index is out of bounds
	 */
	public boolean isNull(int index) throws LazyException{
		LazyNode token=getValueToken(index);
		if(token.type==LazyNode.VALUE_NULL)return true;
		return false;
	}

	/**
	 * Values for an array are attached as children on the token representing
	 * the array itself. This method finds the correct child for a given index
	 * and returns it.
	 *
	 * Since children are stored as a linked list, this method is likely to be
	 * a serious O(n) performance bottleneck for array access. To improve this
	 * for the most common case, we maintain a traversal index and pointer into
	 * the children - meaning that if you traverse the array from the beginning
	 * the complexity will be O(1) for each access request instead.
	 *
	 * @param index the location of the desired value
	 * @return the child for the given index
	 * @throws LazyException if the index is out of bounds
	 */
	private LazyNode getValueToken(int index) throws LazyException{
		if(index<0)throw new LazyException("Array undex can not be negative");
		int num=0;
		LazyNode child=root.child;
		// If the value we are looking for is past our previous traversal point
		// continue at the previous point
		if(selectInt>-1 && index>=selectInt){
			num=selectInt;
			child=selectToken;
		}
		while(child!=null){
			if(num==index){
				// Store the traversal point and return the current token
				selectInt=index;
				selectToken=child;
				return child;
			}
			num++;
			child=child.next;
		}
		throw new LazyException("Array index out of bounds "+index);
	}

	/**
	 * Values for an array are attached as children on the token representing
	 * the array itself. This method finds the correct child for a given index
	 * and returns it.
	 *
	 * Since children are stored as a linked list, this method is likely to be
	 * a serious O(n) performance bottleneck for array access. To improve this
	 * for the most common case, we maintain a traversal index and pointer into
	 * the children - meaning that if you traverse the array from the beginning
	 * the complexity will be O(1) for each access request instead.
	 *
	 * @param index the location of the desired value
	 * @return the child for the given index or null if the index does not exist
	 * @throws LazyException if the index is out of bounds
	 */
	private LazyNode getOptionalValueToken(int index) throws LazyException{
		if(index<0)throw new LazyException("Array undex can not be negative");
		int num=0;
		LazyNode child=root.child;
		// If the value we are looking for is past our previous traversal point
		// continue at the previous point
		if(selectInt>-1 && index>=selectInt){
			num=selectInt;
			child=selectToken;
		}
		while(child!=null){
			if(num==index){
				// Store the traversal point and return the current token
				selectInt=index;
				selectToken=child;
				return child;
			}
			num++;
			child=child.next;
		}
		return null;
	}

	/**
	 * Utility method to get the string value of a specific token
	 *
	 * @param token the token for which to extract a string
	 * @return the string value of the given token
	 */
	// private String getString(LazyNode token){
	//	return token.getStringValue(cbuf);
	// }

	/*
	// For debug purposes only
	public String toString(int pad){
		return root.toString(pad);
	}
	*/
}