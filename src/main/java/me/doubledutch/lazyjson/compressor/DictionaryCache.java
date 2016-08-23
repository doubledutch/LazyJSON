package me.doubledutch.lazyjson.compressor;

import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class DictionaryCache{
	private final int MAX_SIZE=32767;
	private String[] data=new String[MAX_SIZE];
	private short next=0;
	private Map<String,Short> dataMap=new HashMap<String,Short>();
	private LinkedHashMap<String,Integer> slidingWindow;
	private final int windowSize;
	private int minRepetitions;
	private boolean dirty=false;

	private int dictionaryHit=0;
	private int dictionaryMiss=0;

	/**
	 * Create a new dictionary with the given window size and given repetition
	 * requirement before new values are added to the dictionary.
	 *
	 * @param windowSizeArg the size of the sliding window of values
	 * @param minRepetitions the number of times a value must be seen within the sliding window before its added to the dictionary
	 */
	public DictionaryCache(int windowSizeArg,int minRepetitions){
		this.windowSize=windowSizeArg;
		this.minRepetitions=minRepetitions;
		init();
	}

	/**
	 * Initializes the internal sliding window data structure.
	 */
	private void init(){
		// We are going to use a linked hash map to maintain our sliding window
		slidingWindow=new LinkedHashMap<String,Integer>(windowSize+1, .75F, false){
            protected boolean removeEldestEntry(Map.Entry<String,Integer> eldest){
                return size()>windowSize;                                  
            }
        };
	}

	/**
	 * Returns the number of entries currently commited to this dictionary.
	 *
	 * @return the number of entries in the dictionary
	 */
	public int getSize(){
		return next;
	}

	/**
	 * Returns a flag specifying wether or not the dictionary has been modified.
	 *
	 * @return a boolean specifying wether or not the dictionary has been modified
	 */
	public boolean isDirty(){
		return dirty;
	}

	/**
	 * Clears the flag specifying wether or not the dictionary has been modified.
	 */
	public void clearDirtyFlag(){
		dirty=false;
	}

	protected void fromDataInputStream(DataInputStream din) throws IOException{
		next=(short)din.readInt();
		for(int i=0;i<next;i++){
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
				String str=new String(raw,StandardCharsets.UTF_8);
				data[(short)i]=str;
				dataMap.put(str,(short)i);
			}
		}
	}

	protected void toDataOutputStream(DataOutputStream dout) throws IOException{
		dout.writeInt(next);
		for(int i=0;i<next;i++){
			String raw=data[i];
			byte[] encoded=raw.getBytes(StandardCharsets.UTF_8);
			int length=encoded.length;
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
		dout.flush();
	}

	/**
	 * Returns the value held at a specific location in the dictionary.
	 *
	 * @param index the value to look up
	 * @return the value from the dictionary or null if no such value exists
	 */
	public String get(short index){
		if(index<0)return null;
		if(index>=next)return null;
		return data[index];
	}

	/**
	 * Returns the location of a value in the dictionary if it is present, but
	 * does not increase the sliding window stats or attempt to put the value
	 * in the dictionary.
	 *
	 * @param value the value to lookup
	 * @return the location in the dictionary if the value was present
	 */

	public short get(String value){
		if(dataMap.containsKey(value))return dataMap.get(value);
		return -1;
	}

	/**
	 * Add a new value to the dictionary if we have seen it a certain number
	 * of times within the current sliding window. If not, return -1 and mark
	 * that we saw it. If the value is already in the dictionary, just return
	 * the lookup position.
	 *
	 * @param value the value to add
	 * @return the index of the value in the dictionary of -1 if it wasn't added
	 */
	public short put(String value){
		// Do we already have this value?
		if(dataMap.containsKey(value)){
			dictionaryHit++;
			return dataMap.get(value);
		}
		// Are we filled up?
		if(next==MAX_SIZE){
			dictionaryMiss++;
			return -1;
		}
		// Should we add values without actual repetitions?
		if(minRepetitions==0){
			data[next]=value;
			dataMap.put(value,next);
			dirty=true;
			dictionaryHit++;
			return next++;
		}
		// Have we seen this value before?
		if(slidingWindow.containsKey(value)){
			int count=slidingWindow.get(value)+1;
			// Does it satisfy the minimum repetition count?
			if(count>minRepetitions){
				slidingWindow.remove(value);
				data[next]=value;
				dataMap.put(value,next);
				dirty=true;
				dictionaryHit++;
				return next++;
			}else{
				slidingWindow.put(value,count);
			}
		}else{
			// Add this new value to the map
			slidingWindow.put(value,1);
		}
		dictionaryMiss++;
		return -1;
	}

	public double getDictionaryUtilization(){
		if(dictionaryHit+dictionaryMiss==0)return 0.0;
		return dictionaryHit/((double)dictionaryHit+dictionaryMiss);
	}
}