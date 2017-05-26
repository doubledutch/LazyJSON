package me.doubledutch.lazyjson;

import me.doubledutch.lazyjson.compressor.DictionaryCache;
import me.doubledutch.lazyjson.compressor.Template;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.nio.ByteBuffer;

/**
 * An object used to parse and inspect JSON data given in the form of a string.
 */
public class LazyObject extends LazyElement{
	public static final Object NULL=new Object();
	/**
	 * Create a new Lazy JSON object based on the JSON representation in the given string.
	 *
	 * @param raw the input string
	 * @throws LazyException if the string could not be parsed as a JSON object
	 */
	public LazyObject(String raw) throws LazyException{
		LazyParser parser=new LazyParser(raw);
		parser.tokenize();	
		if(parser.root.type!=LazyNode.OBJECT){
			throw new LazyException("JSON Object must start with {",0);
		}
		root=parser.root;
		// source=raw;
	}

	public LazyObject() throws LazyException{
		LazyParser parser=new LazyParser("{}");
		parser.tokenize();	
		root=parser.root;
	}

	// protected LazyObject(LazyNode root,char[] source){
	//	super(root,source,null);
	// }

	protected LazyObject(LazyNode root){
		super(root);
	}


	/**
	 * Returns the type of this element.
	 *
	 * @return LazyType.OBJECT
	 */
	public LazyType getType(){
		return LazyType.OBJECT;
	}

	/**
	 * Returns the value type of the given field.
	 *
	 * @param key the requested field
	 * @return the type of the value for the given field
	 * @throws LazyException if the requested field did not exist
	 */
	public LazyType getType(String key) throws LazyException{
		LazyNode token=getFieldToken(key);
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

	public Object opt(String key) throws LazyException{
		LazyNode token=getOptionalFieldToken(key);
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

	public Object get(String key) throws LazyException{
		LazyNode token=getFieldToken(key);
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

	public Object remove(String key) throws LazyException{
		Object obj=opt(key); // TODO: should this be get instead of opt?
		LazyNode token=getOptionalField(key);
		if(token!=null){
			// System.out.println("found the token!");
			LazyNode pointer=this.root.child;
			if(pointer==token){
				root.child=token.next;
				if(root.lastChild==pointer){
					root.lastChild=null;
				}
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
		return obj;
	}

	protected String serializeElementToString(){
		StringBuilder buf=new StringBuilder();
		buf.append("{");
		LazyNode pointer=root.child;
		boolean first=true;
		while(pointer!=null){
			if(first){
				first=false;
			}else{
				buf.append(",");
			}
			buf.append("\"");
			buf.append(pointer.getStringValue());
			buf.append("\":");
			if(pointer.child.type==LazyNode.OBJECT){
				buf.append(new LazyObject(pointer.child).toString());
			}else if(pointer.child.type==LazyNode.ARRAY){
				buf.append(new LazyArray(pointer.child).toString());
			}else if(pointer.child.type==LazyNode.VALUE_STRING || pointer.child.type==LazyNode.VALUE_ESTRING){
				buf.append("\"");
				buf.append(pointer.child.getRawStringValue());
				buf.append("\"");
			}else if(pointer.child.type==LazyNode.VALUE_TRUE){
				buf.append("true");
			}else if(pointer.child.type==LazyNode.VALUE_FALSE){
				buf.append("false");
			}else if(pointer.child.type==LazyNode.VALUE_NULL){
				buf.append("null");
			}else{
				buf.append(pointer.child.getStringValue());
			}
			pointer=pointer.next;
		}
		buf.append("}");
		return buf.toString();
	}

	private void attachField(String key,LazyNode child) throws LazyException{
		// TODO: change to avoid this constant check
		StringBuilder dirtyBuf=root.getDirtyBuf();
		LazyNode token=getOptionalField(key);
		if(token==null){
			// new field
			token=LazyNode.cField(dirtyBuf.length());
			token.dirty=true;
			token.dirtyBuf=dirtyBuf;
			// TODO: we should be encoding the value
			dirtyBuf.append(key);
			token.endIndex=dirtyBuf.length();
			if(root.child==null){
				root.child=token;
				root.lastChild=token;
			}else{
				root.lastChild.next=token;
				root.lastChild=token;
			}
		}
		token.child=child;
		token.lastChild=child;
	}

	public LazyObject put(String key,String value) throws LazyException{
		if(value==null){
			remove(key);
			return this;
		}
		LazyNode child=null;
		if(shouldQuoteString(value)){
			child=appendAndSetDirtyString(LazyNode.VALUE_ESTRING,quoteString(value));
		}else{
			child=appendAndSetDirtyString(LazyNode.VALUE_STRING,value);
		}
		attachField(key,child);
		return this;
	}

	public LazyObject put(String key,int value) throws LazyException{
		LazyNode child=appendAndSetDirtyString(LazyNode.VALUE_INTEGER,Integer.toString(value));
		attachField(key,child);
		return this;
	}

	public LazyObject put(String key,long value) throws LazyException{
		LazyNode child=appendAndSetDirtyString(LazyNode.VALUE_INTEGER,Long.toString(value));
		attachField(key,child);
		return this;
	}

	public LazyObject put(String key,float value) throws LazyException{
		LazyNode child=appendAndSetDirtyString(LazyNode.VALUE_FLOAT,Float.toString(value));
		attachField(key,child);
		return this;
	}

	public LazyObject put(String key,double value) throws LazyException{
		LazyNode child=appendAndSetDirtyString(LazyNode.VALUE_FLOAT,Double.toString(value));
		attachField(key,child);
		return this;
	}

	public LazyObject put(String key,boolean value) throws LazyException{
		LazyNode child=null;
		if(value){
			child=LazyNode.cValueTrue(-1);
		}else{
			child=LazyNode.cValueFalse(-1);
		}
		child.dirty=true;
		attachField(key,child);
		return this;
	}

	public LazyObject put(String key,LazyObject value) throws LazyException{
		attachField(key,value.root);
		/*if(value.cbuf==cbuf && value.dirtyBuf==dirtyBuf){
			value.root.dirty=true;
			attachField(key,value.root);
		}else if(value.cbuf!=cbuf){
			// Differen't sources
			StringBuilder buf=getDirtyBuf();
			value.root.moveInto(buf,value.cbuf,value.dirtyBuf);
			value.root.dirty=true;
			attachField(key,value.root);
			value.dirtyBuf=buf;
		}else throw new LazyException("Unknown data merge condition :-( :-( :-(");*/
		return this;
	}

	public LazyObject put(String key,LazyArray value) throws LazyException{
		attachField(key,value.root);
		/*if(value.cbuf==cbuf && value.dirtyBuf==dirtyBuf){
			value.root.dirty=true;
			attachField(key,value.root);
		}else if(value.cbuf!=cbuf){
			// Differen't sources
			StringBuilder buf=getDirtyBuf();
			value.root.moveInto(buf,value.cbuf,value.dirtyBuf);
			value.root.dirty=true;
			attachField(key,value.root);
			value.dirtyBuf=buf;
			// System.out.println("not matching put conditions");
		}else throw new LazyException("Unknown data merge condition :-( :-( :-(");*/
		return this;
	}

	public LazyObject put(String key,Object value) throws LazyException{
		if(value==NULL){
			LazyNode child=LazyNode.cValueNull(-1);
			child.dirty=true;
			attachField(key,child);
			return this;
		}else if(value==null){
			// TODO: remove key instead
		}
		// TODO: look into faster ways of branching by type
		if(value instanceof java.lang.Integer){
			return put(key,((Integer)value).intValue());
		}
		if(value instanceof java.lang.Long){
			return put(key,((Long)value).longValue());
		}
		if(value instanceof java.lang.Float){
			return put(key,((Float)value).floatValue());
		}
		if(value instanceof java.lang.Double){
			return put(key,((Double)value).doubleValue());
		}
		if(value instanceof java.lang.Boolean){
			return put(key,((Boolean)value).booleanValue());
		}
		if(value instanceof java.lang.String){
			return put(key,(String)value);
		}
		if(value instanceof me.doubledutch.lazyjson.LazyObject){
			return put(key,(LazyObject)value);
		}
		if(value instanceof me.doubledutch.lazyjson.LazyArray){
			return put(key,(LazyArray)value);
		}
		throw new LazyException("Unsupported object type");
	}

	/**
	 * Returns the string value stored in this object for the given key.
	 *
	 * @param key the name of the field on this object
	 * @return the requested string value
	 * @throws LazyException if the value for the given key was not a string.
	 */
	public String getString(String key) throws LazyException{
		LazyNode token=getFieldToken(key);
		return token.getStringValue();
	}

	/**
	 * Returns the string value stored in this object for the given key.
	 * Returns null if there is no such key.
	 *
	 * @param key the name of the field on this object
	 * @return the requested string value or null if there was no such key
	 */
	public String optString(String key){
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return null;
		if(token.type==LazyNode.VALUE_NULL)return null;
		return token.getStringValue();
	}

	/**
	 * Returns the string value stored in this object for the given key.
	 * Returns the default value if there is no such key.
	 *
	 * @param key the name of the field on this object
	 * @param defaultValue the default value to return
	 * @return the requested string value or the default value if there was no such key
	 */
	public String optString(String key,String defaultValue){
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return defaultValue;
		if(token.type==LazyNode.VALUE_NULL)return defaultValue;
		return token.getStringValue();
	}

	/**
	 * Returns the integer value stored in this object for the given key.
	 *
	 * @param key the name of the field on this object
	 * @return an integer value
	 * @throws LazyException if the value for the given key was not an integer.
	 */
	public int getInt(String key) throws LazyException{
		LazyNode token=getFieldToken(key);
		return token.getIntValue();
	}

	/**
	 * Returns the integer value stored in this object for the given key.
	 * Returns 0 if there is no such key.
	 *
	 * @param key the name of the field on this object
	 * @return the requested integer value or 0 if there was no such key
	 */
	public int optInt(String key){
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return 0;
		if(token.type==LazyNode.VALUE_NULL)return 0;
		return token.getIntValue();
	}

	/**
	 * Returns the integer value stored in this object for the given key.
	 * Returns the default value if there is no such key.
	 *
	 * @param key the name of the field on this object
	 * @param defaultValue the default value to return
	 * @return the requested integer value or the default value if there was no such key
	 */
	public int optInt(String key,int defaultValue){
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return defaultValue;
		if(token.type==LazyNode.VALUE_NULL)return defaultValue;
		return token.getIntValue();
	}

	/**
	 * Returns the long value stored in this object for the given key.
	 *
	 * @param key the name of the field on this object
	 * @return a boolean value
	 * @throws LazyException if the value for the given key was not a long.
	 */
	public long getLong(String key) throws LazyException{
		LazyNode token=getFieldToken(key);
		return token.getLongValue();
	}

	/**
	 * Returns the long value stored in this object for the given key.
	 * Returns 0 if there is no such key.
	 *
	 * @param key the name of the field on this object
	 * @return the requested long value or 0 if there was no such key
	 */
	public long optLong(String key){
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return 0l;
		if(token.type==LazyNode.VALUE_NULL)return 0l;
		return token.getLongValue();
	}

	/**
	 * Returns the long value stored in this object for the given key.
	 * Returns the default value if there is no such key.
	 *
	 * @param key the name of the field on this object
	 * @param defaultValue the default value to return
	 * @return the requested long value or the default value if there was no such key
	 */
	public long optLong(String key,long defaultValue){
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return defaultValue;
		if(token.type==LazyNode.VALUE_NULL)return defaultValue;
		return token.getLongValue();
	}

	/**
	 * Returns the double value stored in this object for the given key.
	 *
	 * @param key the name of the field on this object
	 * @return a boolean value
	 * @throws LazyException if the value for the given key was not a double.
	 */
	public double getDouble(String key) throws LazyException{
		LazyNode token=getFieldToken(key);
		return token.getDoubleValue();
	}

	/**
	 * Returns the double value stored in this object for the given key.
	 * Returns 0.0 if there is no such key.
	 *
	 * @param key the name of the field on this object
	 * @return the requested double value or 0.0 if there was no such key
	 */
	public double optDouble(String key){
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return 0.0;
		if(token.type==LazyNode.VALUE_NULL)return 0.0;
		return token.getDoubleValue();
	}

	/**
	 * Returns the double value stored in this object for the given key.
	 * Returns the default value if there is no such key.
	 *
	 * @param key the name of the field on this object
	 * @param defaultValue the default value to return
	 * @return the requested long value or the default value if there was no such key
	 */
	public double optDouble(String key,double defaultValue){
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return defaultValue;
		if(token.type==LazyNode.VALUE_NULL)return defaultValue;
		return token.getDoubleValue();
	}

	/**
	 * Returns true if the value stored in this object for the given key is null.
	 *
	 * @param key the name of the field on this object
	 * @return true if the value is null, false otherwise
	 * @throws LazyException if no value was set for the given key.
	 */
	public boolean isNull(String key){
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return true;
		if(token.type==LazyNode.VALUE_NULL)return true;
		return false;
	}

	/**
	 * Returns the boolean value stored in this object for the given key.
	 *
	 * @param key the name of the field on this object
	 * @return a boolean value
	 * @throws LazyException if the value for the given key was not a boolean.
	 */
	public boolean getBoolean(String key){
		LazyNode token=getFieldToken(key);
		if(token.type==LazyNode.VALUE_STRING || token.type==LazyNode.VALUE_ESTRING){
			String str=token.getStringValue().toLowerCase().trim();
			if(str.equals("true"))return true;
			if(str.equals("false"))return false;
			throw new LazyException("Requested value is not a boolean",token);
		}
		if(token.type==LazyNode.VALUE_TRUE)return true;
		if(token.type==LazyNode.VALUE_FALSE)return false;
		throw new LazyException("Requested value is not a boolean",token);
	}

	/**
	 * Returns the boolean value stored in this object for the given key.
	 * Returns false if there is no such key.
	 *
	 * @param key the name of the field on this object
	 * @return the requested boolean value or false if there was no such key
	 */
	public boolean optBoolean(String key){
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return false;
		if(token.type==LazyNode.VALUE_STRING || token.type==LazyNode.VALUE_ESTRING){
			String str=token.getStringValue().toLowerCase().trim();
			if(str.equals("true"))return true;
			if(str.equals("false"))return false;
			throw new LazyException("Requested value is not a boolean",token);
		}
		// if(token.type==LazyNode.VALUE_NULL)return false;
		if(token.type==LazyNode.VALUE_TRUE)return true;
		// if(token.type==LazyNode.VALUE_FALSE)return false;
		return false;
	}

	/**
	 * Returns the boolean value stored in this object for the given key.
	 * Returns the default value if there is no such key.
	 *
	 * @param key the name of the field on this object
	 * @param defaultValue the default value to return
	 * @return the requested boolean value or the default value if there was no such key
	 */
	public boolean optBoolean(String key,boolean defaultValue){
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return defaultValue;
		if(token.type==LazyNode.VALUE_NULL)return defaultValue;
		if(token.type==LazyNode.VALUE_STRING || token.type==LazyNode.VALUE_ESTRING){
			String str=token.getStringValue().toLowerCase().trim();
			if(str.equals("true"))return true;
			if(str.equals("false"))return false;
			throw new LazyException("Requested value is not a boolean",token);
		}
		if(token.type==LazyNode.VALUE_TRUE)return true;
		return false;
	}

	/**
	 * Returns the JSON object stored in this object for the given key.
	 *
	 * @param key the name of the field on this object
	 * @return an array value
	 * @throws LazyException if the value for the given key was not an object.
	 */
	public LazyObject getJSONObject(String key) throws LazyException{
		LazyNode token=getFieldToken(key);
		if(token.type!=LazyNode.OBJECT)throw new LazyException("Requested value is not an object",token);
		LazyObject obj=new LazyObject(token);
		obj.parent=this;
		return obj;
	}

	/**
	 * Returns the JSON object stored in this object for the given key on null if the key doesn't exist.
	 *
	 * @param key the name of the field on this object
	 * @throws LazyException if the value for the given key was not an object.
	 * @return an object value or null if there was no such key
	 */
	public LazyObject optJSONObject(String key) throws LazyException{
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return null;
		if(token.type==LazyNode.VALUE_NULL)return null;
		if(token.type!=LazyNode.OBJECT)return null;
		LazyObject obj=new LazyObject(token);
		obj.parent=this;
		return obj;
	}

	/**
	 * Returns the JSON array stored in this object for the given key.
	 *
	 * @param key the name of the field on this object
	 * @return an array value
	 * @throws LazyException if the value for the given key was not an array.
	 */
	public LazyArray getJSONArray(String key) throws LazyException{
		LazyNode token=getFieldToken(key);
		if(token.type!=LazyNode.ARRAY)throw new LazyException("Requested value is not an array",token);
		LazyArray arr=new LazyArray(token);
		arr.parent=this;
		return arr;
	}

	/**
	 * Returns the JSON array stored in this object for the given key or null if the key doesn't exist.
	 *
	 * @param key the name of the field on this object
	 * @return an array value or null if the key doesn't exist
	 * @throws LazyException if the value for the given key was not an array.
	 */
	public LazyArray optJSONArray(String key) throws LazyException{
		LazyNode token=getOptionalFieldToken(key);
		if(token==null)return null;
		if(token.type==LazyNode.VALUE_NULL)return null;
		if(token.type!=LazyNode.ARRAY)return null;
		LazyArray arr=new LazyArray(token);
		arr.parent=this;
		return arr;
	}

	/**
	 * Returns a string iterator with the fields of this object as values.
	 *
	 * @return an iterator of object field names
	 */
	public Iterator<String> keys(){
		return root.getStringIterator();
	}

	/**
	 * Returns a set containing all keys on this object. If possible, use the keys iterator instead for improved performance.
	 *
	 * @return a set containing all keys in this object
	 */
	public Set<String> keySet(){
		HashSet<String> set=new HashSet<String>();
		Iterator<String> keys=keys();
		while(keys.hasNext()){
			set.add(keys.next());
		}
		return set;
	}

	public static java.lang.String[] getNames(LazyObject obj){
		Set<String> keys=obj.keySet();
		return keys.toArray(new String[keys.size()]);
	}

	public int hashCode(){
		int code=1;
		for(String key:keySet()){
			LazyType t1=getType(key);
			switch(t1){
				case STRING:code+=getString(key).hashCode();
					break;
				case INTEGER:
						long l=getLong(key);
						code+=(int)(l ^ (l >>> 32));
					break;
				case FLOAT:
						double d=getDouble(key);
						l=Double.doubleToLongBits(d);
						code+=(int)(l ^ (l >>> 32));
					break;
				case BOOLEAN:
						if(getBoolean(key)){
							code+=1;
						}
					break;
				case OBJECT:code+=37*getJSONObject(key).hashCode();
					break;
				case ARRAY:code+=37*getJSONArray(key).hashCode();
					break;
			}
		}
		return code;
	}


	/**
	 * Utility method to evaluate wether a given string matches the value
	 * of a field.
	 *
	 * @param key the key to compare a token to
	 * @param token the field token
	 * @return true if the key matches, false otherwise
	 */
	private boolean keyMatch(String key,LazyNode token){
		if(token.type==LazyNode.EFIELD){
			String field=token.getStringValue();
			return field.equals(key);
		}else{
			// Quickly check the length first
			int length=key.length();
			if(token.endIndex-token.startIndex!=length){
				return false;
			}
			// Now go through the field character for character to compare
			if(token.dirty){
				for(int i=0;i<length;i++){
					char c=key.charAt(i);
					if(c!=token.dirtyBuf.charAt(token.startIndex+i)){
						return false;
					}
				}
			}else{
				for(int i=0;i<length;i++){
					char c=key.charAt(i);
					if(c!=token.cbuf[token.startIndex+i]){
						return false;
					}
				}
			}
			return true;
		}
	}

	/**
	 * Returns true if the given key matches a field on this object.
	 *
	 * @param key the name of the field to look for
	 * @return true if the key exists, false otherwise
	 */
	public boolean has(String key){
		LazyNode child=root.child;
		while(child!=null){
			if(keyMatch(key,child)){
				return true;
			}
			child=child.next;
		}
		return false;
	}

	/**
	 * Fields for an object are attached as children on the token representing
	 * the object itself. This method finds the correct field for a given key
	 * and returns its first child - the child being the value for that field.
	 * This is a utility method used internally to extract field values.
	 *
	 * @param key the name of the desired field
	 * @return the first child of the matching field token if one exists
	 * @throws LazyException if the field does not exist
	 */
	private LazyNode getFieldToken(String key) throws LazyException{
		LazyNode child=root.child;
		while(child!=null){
			if(keyMatch(key,child)){
				return child.child;
			}
			child=child.next;
		}
		throw new LazyException("Unknown field '"+key+"'");
	}

	/**
	 * Fields for an object are attached as children on the token representing
	 * the object itself. This method finds the correct field for a given key
	 * and returns its first child - the child being the value for that field.
	 * This is a utility method used internally to extract field values.
	 *
	 * @param key the name of the desired field
	 * @return the first child of the matching field token if one exists, null otherwise
	 */
	private LazyNode getOptionalFieldToken(String key){
		LazyNode child=root.child;
		while(child!=null){
			if(keyMatch(key,child)){
				return child.child;
			}
			child=child.next;
		}
		return null;
	}

	/**
	 * Fields for an object are attached as children on the token representing
	 * the object itself. This method finds the correct field for a given key.
	 * This is a utility method used internally to extract field values.
	 *
	 * @param key the name of the desired field
	 * @return the first child of the matching field token if one exists, null otherwise
	 */
	private LazyNode getOptionalField(String key){
		LazyNode child=root.child;
		while(child!=null){
			if(keyMatch(key,child)){
				return child;
			}
			child=child.next;
		}
		return null;
	}

	
	// For debug purposes only
	public String toString(int pad){
		return root.toString(pad);
	}
	
	
}