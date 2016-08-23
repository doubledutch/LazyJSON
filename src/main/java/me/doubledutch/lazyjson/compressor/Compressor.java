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
	private String prefix;
	private boolean dirtyFlag=false;
	private DictionaryCache dictionary;

	private int templateHit=0;
	private int templateMiss=0;

	public Compressor(String prefix,int windowSizeArg,int minRepetitions) throws IOException{
		this.windowSize=windowSizeArg;
		this.minRepetitions=minRepetitions;
		this.prefix=prefix;
		// We are going to use a linked hash map to maintain our sliding window
		slidingWindow=new LinkedHashMap<Template,Integer>(windowSize+1, .75F, false){
            protected boolean removeEldestEntry(Map.Entry<Template,Integer> eldest){
                return size()>windowSize;                                  
            }
        };
        dictionary=new DictionaryCache(windowSize,minRepetitions);
        reloadState();
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
		return compress(LazyElement.parse(str));
	}


	public byte[] compress(LazyElement elm){
		// First, extract the template
		Template t=elm.extractTemplate();
		// If the template satisfies our compression criterea - compress
		if(shouldCompress(t)){
			try{
				ByteBuffer buf=ByteBuffer.allocate(elm.getSourceLength()-2);
				buf.putShort((short)templateSet.get(t));
				elm.writeTemplateValues(buf,dictionary);
				int pos=buf.position();
				buf.rewind();
				byte[] result=new byte[pos];
				buf.get(result);
				templateHit++;
				return result;
			}catch(BufferOverflowException boe){
				// Compressed output equal to or larger than raw data
			}
		}
		// Return raw encoded data
		// TODO: this is incredibly inefficient... fix!
		byte[] encoded=elm.toString().getBytes(StandardCharsets.UTF_8);
		ByteBuffer buf=ByteBuffer.allocate(2+encoded.length);
		buf.putShort((short)-1);
		buf.put(encoded);
		templateMiss++;
		return buf.array();
	}

	public LazyElement decompressElement(byte[] data){
		return LazyElement.parse(decompress(data));
	}

	public LazyObject decompressObject(byte[] data){
		return new LazyObject(decompress(data));
	}

	public LazyArray decompressArray(byte[] data){
		return new LazyArray(decompress(data));
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

	private void reloadState() throws IOException{
		File ftest=new File(prefix+".templates");
		if(ftest.exists()){
			DataInputStream in=new DataInputStream(new FileInputStream(prefix+".templates"));
			nextTemplate=(short)in.readInt();
			for(int i=0;i<nextTemplate;i++){
				Template t=Template.fromDataInput(in);
				templateSet.put(t,(short)i);
				templateIdMap.put((short)i,t);
			}
			in.close();
		}
		ftest=new File(prefix+".dictionary");
		if(ftest.exists()){
			DataInputStream in=new DataInputStream(new FileInputStream(prefix+".dictionary"));
			dictionary.fromDataInputStream(in);
			in.close();
		}
	}

	public void commit() throws IOException{
		// Save state of templates and dictionary if needed
		if(dirtyFlag){
			DataOutputStream out=new DataOutputStream(new FileOutputStream(prefix+".templates-tmp"));
			out.writeInt(nextTemplate);
			for(Template t:templateSet.keySet()){
				t.toDataOutput(out);
			}
			out.flush();
			out.close();
			File ftest=new File(prefix+".templates-tmp");
			ftest.renameTo(new File(prefix+".templates"));
			dirtyFlag=false;
		}
		// Save dictionary
		if(dictionary.isDirty()){
			DataOutputStream out=new DataOutputStream(new FileOutputStream(prefix+".dictionary-tmp"));
			dictionary.toDataOutputStream(out);
			out.flush();
			out.close();
			File ftest=new File(prefix+".dictionary-tmp");
			ftest.renameTo(new File(prefix+".dictionary"));
		}
	}

	public int getTemplateCount(){
		return templateSet.size();
	}

	public int getDictionaryCount(){
		return dictionary.getSize();
	}

	public double getTemplateUtilization(){
		if(templateHit+templateMiss==0)return 0.0;
		return templateHit/((double)templateHit+templateMiss);
	}

	public double getDictionaryUtilization(){
		return dictionary.getDictionaryUtilization();
	}
}