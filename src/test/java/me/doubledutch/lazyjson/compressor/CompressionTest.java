package me.doubledutch.lazyjson.compressor;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.nio.ByteBuffer;

import me.doubledutch.lazyjson.*;


public class CompressionTest{
	@Test
	public void testInOut() throws Exception{
		String str="{\"foo\":42}";
		Compressor c=new Compressor("./ctest",1000,0);
		byte[] out=c.compress(str);
		// System.out.println(out.length+" vs "+str.length());
		String str2=c.decompress(out);
		assertEquals(str,str2);
	}

	@Test
	public void testSet() throws Exception{
		Compressor c=new Compressor("./ctest",1000,3);
		List<byte[]> list=new ArrayList<byte[]>();
		for(int i=0;i<100;i++){
			String str="{\"foo\":"+i+"}";
			list.add(c.compress(str));
		}
		for(int i=0;i<100;i++){
			String str=c.decompress(list.get(i));
			LazyObject obj=new LazyObject(str);
			assertEquals(i,obj.getInt("foo"));			
		}
	}

	@Test
	public void testBadCompression() throws Exception{
		String str="{\"foo\":[0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9]}";
		Compressor c=new Compressor("./ctest",1000,0);
		byte[] out=c.compress(str);
		// System.out.println(out.length+" vs "+str.length());
		String str2=c.decompress(out);
		assertEquals(str,str2);
	}

	@Test
	public void testCommitAndReload() throws Exception{
		String str="{\"foo\":42,\"bar\":\"Hello World!\"}";
		Compressor c=new Compressor("./ctest",10,0);
		byte[] out=c.compress(str);
		c.commit();
		c=new Compressor("./ctest",10,0);
		// System.out.println(out.length+" vs "+str.length());
		String str2=c.decompress(out);
		assertEquals(str,str2);
		File ftest=new File("./ctest.templates");
		ftest.delete();
		ftest=new File("./ctest.dictionary");
		ftest.delete();
	}
}