package me.doubledutch.lazyjson.compressor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import me.doubledutch.lazyjson.*;

public class Compressor{
	private HashMap<Template,Short> templateSet=new HashMap<Template,Short>();
	private HashMap<Short,Template> templateIdMap=new HashMap<Short,Template>();
	private short nextTemplate=0;
	private LinkedHashMap<Template,Integer> slidingWindow;
	private final int windowSize;
	private int minRepetitions;
	private boolean useDictionary;
	private String prefix;
	private boolean dirtyFlag=false;
	private DictionaryCache dictionary;

	public Compressor(String prefix,int windowSizeArg,int minRepetitions, boolean useDictionary){
		this.windowSize=windowSizeArg;
		this.minRepetitions=minRepetitions;
		this.useDictionary=useDictionary;
		this.prefix=prefix;
		// We are going to use a linked hash map to maintain our sliding window
		slidingWindow=new LinkedHashMap<Template,Integer>(windowSize+1, .75F, false){
            protected boolean removeEldestEntry(Map.Entry<Template,Integer> eldest){
                return size()>windowSize;                                  
            }
        };
        dictionary=new DictionaryCache(windowSize,minRepetitions);
	}

	private boolean shouldCompress(Template t){
		if(templateSet.containsKey(t))return true;
		if(minRepetitions==0){
			templateSet.put(t,nextTemplate);
			templateIdMap.put(nextTemplate,t);
			nextTemplate++;
			dirtyFlag=true;
			return true;
		}
		// TODO: add check for the max number of templates we are interested in
		// Have we seen this template before?
		if(slidingWindow.containsKey(t)){
			int count=slidingWindow.get(t)+1;
			// Does it satisfy the minimum repetition count?
			if(count>minRepetitions){
				slidingWindow.remove(t);
				templateSet.put(t,nextTemplate);
				templateIdMap.put(nextTemplate,t);
				nextTemplate++;
				dirtyFlag=true;
				return true;
			}else{
				slidingWindow.put(t,count);
			}
		}else{
			// Add this new template to the map
			slidingWindow.put(t,1);
		}
		return false;
	}

	public byte[] compress(String str){
		// 1. Parse data
		LazyElement elm=LazyElement.parse(str);
		// 2. Generate template
		Template t=elm.extractTemplate();
		// 3. If template satisfies criterea - compress
		if(shouldCompress(t)){
			try{
				ByteBuffer buf=ByteBuffer.allocate(str.length()-2);
				buf.putShort((short)templateSet.get(t));
				elm.writeTemplateValues(buf,dictionary);
				return buf.array();
			}catch(BufferOverflowException boe){
				// Compressed output larger than raw data
			}
		}
		// 4. return encoded data
		// TODO: this is incredibly inefficient... fix!
		byte[] encoded=str.getBytes(StandardCharsets.UTF_8);
		ByteBuffer buf=ByteBuffer.allocate(2+encoded.length);
		buf.putShort((short)-1);
		buf.put(encoded);
		return buf.array();
	}

	public String decompress(byte[] data){
		ByteBuffer buf=ByteBuffer.wrap(data);
		// 1. If it's a raw value, simply decode
		short templateId=buf.getShort();
		if(templateId==-1){
			return new String(data,2,data.length-2,StandardCharsets.UTF_8);
		}
		// 2. Find correct template
		Template t=templateIdMap.get(templateId);
		// 3. Decode data
		String str=t.read(buf,dictionary);
		// 4. Return decoded string
		return str;
	}

	public void commit() throws IOException{
		// Save state of templates and dictionary if needed
	}
}