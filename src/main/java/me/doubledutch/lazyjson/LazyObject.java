package me.doubledutch.lazyjson;

import me.doubledutch.lazyjson.compressor.DictionaryCache;
import me.doubledutch.lazyjson.compressor.Template;
import java.util.Iterator;
import java.nio.ByteBuffer;

/**
 * An object used to parse and inspect JSON data given in the form of a string.
 */
public class LazyObject extends LazyElement{
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
		cbuf=parser.cbuf;
		// source=raw;
	}

	protected LazyObject(LazyNode root,char[] source){
		super(root,source);
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

	/**
	 * Returns the string value stored in this object for the given key.
	 *
	 * @param key the name of the field on this object
	 * @return the requested string value
	 * @throws LazyException if the value for the given key was not a string.
	 */
	public String getString(String key) throws LazyException{
		LazyNode token=getFieldToken(key);
		return token.getStringValue(cbuf);
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
		return token.getStringValue(cbuf);
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
		return token.getStringValue(cbuf);
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
		return token.getIntValue(cbuf);
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
		return token.getIntValue(cbuf);
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
		return token.getIntValue(cbuf);
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
		return token.getLongValue(cbuf);
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
		return token.getLongValue(cbuf);
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
		return token.getLongValue(cbuf);
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
		return token.getDoubleValue(cbuf);
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
		return token.getDoubleValue(cbuf);
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
		return token.getDoubleValue(cbuf);
	}

	/**
	 * Returns true if the value stored in this object for the given key is null.
	 *
	 * @param key the name of the field on this object
	 * @return true if the value is null, false otherwise
	 * @throws LazyException if no value was set for the given key.
	 */
	public boolean isNull(String key){
		LazyNode token=getFieldToken(key);
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
		return new LazyObject(token,cbuf);
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
		if(token.type!=LazyNode.OBJECT)throw new LazyException("Requested value is not an object",token);
		return new LazyObject(token,cbuf);
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
		return new LazyArray(token,cbuf);
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
		if(token.type!=LazyNode.ARRAY)throw new LazyException("Requested value is not an array",token);
		return new LazyArray(token,cbuf);
	}

	/**
	 * Returns a string iterator with the fields of this object as values.
	 *
	 * @return an iterator of object field names
	 */
	public Iterator<String> keys(){
		return root.getStringIterator(cbuf);
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
		// Quickly check the length first
		int length=key.length();
		if(token.endIndex-token.startIndex!=length){
			return false;
		}
		// Now go through the field character for character to compare
		for(int i=0;i<length;i++){
			char c=key.charAt(i);
			if(c!=cbuf[token.startIndex+i]){
				return false;
			}
		}
		return true;
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

	/*
	// For debug purposes only
	public String toString(int pad){
		return root.toString(pad);
	}
	*/
	
}