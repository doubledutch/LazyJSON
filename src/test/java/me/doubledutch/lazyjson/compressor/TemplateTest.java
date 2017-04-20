package me.doubledutch.lazyjson.compressor;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.nio.ByteBuffer;

import me.doubledutch.lazyjson.*;

public class TemplateTest{
	@Test
	public void testStringValues() throws Exception{
		String str="{\"foo\":\"Hello World!\"}";
		LazyObject obj1=new LazyObject(str);
		Template t=obj1.extractTemplate();
		ByteBuffer buf=ByteBuffer.allocate(4096);
		buf.mark();
		DictionaryCache dict=new DictionaryCache(100,2);
		obj1.writeTemplateValues(buf,dict);
		// System.out.println(str.length()+" vs "+buf.position());
		buf.reset();
		// System.out.println(t.read(buf,dict));
		LazyObject obj2=(LazyObject)LazyElement.readFromTemplate(t,buf,dict);
		assertEquals(obj1.getString("foo"),obj2.getString("foo"));
	}

	@Test
	public void testArrayValues() throws Exception{
		String str="{\"foo\":[\"Hello World!\",0,300,65000,3.1415,true,false,null,112147483650]}";
		LazyObject obj1=new LazyObject(str);
		Template t=obj1.extractTemplate();
		ByteBuffer buf=ByteBuffer.allocate(4096);
		buf.mark();
		DictionaryCache dict=new DictionaryCache(100,2);
		obj1.writeTemplateValues(buf,dict);
		// System.out.println(str.length()+" vs "+buf.position());
		buf.reset();
		System.out.println(t.read(buf,dict));

		LazyObject obj2=(LazyObject)LazyElement.readFromTemplate(t,buf,dict);
		assertEquals(obj1.getJSONArray("foo").getInt(1),obj2.getJSONArray("foo").getInt(1));
	}

	@Test
	public void testLongStringValues() throws Exception{
		String str="{\"foo\":\"Hello World!12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890\"}";
		LazyObject obj1=new LazyObject(str);
		Template t=obj1.extractTemplate();
		ByteBuffer buf=ByteBuffer.allocate(4096);
		buf.mark();
		DictionaryCache dict=new DictionaryCache(100,2);
		obj1.writeTemplateValues(buf,dict);
		// System.out.println(str.length()+" vs "+buf.position());
		buf.reset();
		// System.out.println(t.read(buf,dict));
		LazyObject obj2=(LazyObject)LazyElement.readFromTemplate(t,buf,dict);
		assertEquals(obj1.getString("foo"),obj2.getString("foo"));
	}

	@Test
	public void testIntValues() throws Exception{
		String str="{\"foo\":42,\"bar\":8192,\"baz\":60000,\"giant\":2147483650,\"giant2\":112147483650}";
		LazyObject obj1=new LazyObject(str);
		Template t=obj1.extractTemplate();
		ByteBuffer buf=ByteBuffer.allocate(4096);
		buf.mark();
		DictionaryCache dict=new DictionaryCache(100,2);
		obj1.writeTemplateValues(buf,dict);
		buf.reset();
		LazyObject obj2=(LazyObject)LazyElement.readFromTemplate(t,buf,dict);
		assertEquals(obj1.getInt("foo"),obj2.getInt("foo"));
		assertEquals(obj1.getInt("bar"),obj2.getInt("bar"));
		assertEquals(obj1.getInt("baz"),obj2.getInt("baz"));
		assertEquals(obj1.getLong("giant"),obj2.getLong("giant"));
		assertEquals(obj1.getLong("giant2"),obj2.getLong("giant2"));
	}

	@Test
	public void testBooleanValues() throws Exception{
		String str="{\"foo\":{\"bar\":false,\"baz\":true}}";
		LazyObject obj1=new LazyObject(str);
		Template t=obj1.extractTemplate();
		ByteBuffer buf=ByteBuffer.allocate(4096);
		buf.mark();
		DictionaryCache dict=new DictionaryCache(100,2);
		obj1.writeTemplateValues(buf,dict);
		// System.out.println(str.length()+" vs "+buf.position());
		buf.reset();
		// System.out.println(t.read(buf,dict));
		LazyObject obj2=(LazyObject)LazyElement.readFromTemplate(t,buf,dict);
		assertEquals(obj1.getJSONObject("foo").getBoolean("bar"),obj2.getJSONObject("foo").getBoolean("bar"));
	}

	@Test
	public void testNullValues() throws Exception{
		String str="{\"foo\":null}";
		LazyObject obj1=new LazyObject(str);
		Template t=obj1.extractTemplate();
		ByteBuffer buf=ByteBuffer.allocate(4096);
		buf.mark();
		DictionaryCache dict=new DictionaryCache(100,2);
		obj1.writeTemplateValues(buf,dict);
		// System.out.println(str.length()+" vs "+buf.position());
		buf.reset();
		// System.out.println(t.read(buf,dict));
		LazyObject obj2=(LazyObject)LazyElement.readFromTemplate(t,buf,dict);
		assertTrue(obj2.isNull("foo"));
	}

	@Test
	public void testDoubleValues() throws Exception{
		String str="{\"foo\":3.1415}";
		LazyObject obj1=new LazyObject(str);
		Template t=obj1.extractTemplate();
		ByteBuffer buf=ByteBuffer.allocate(4096);
		buf.mark();
		DictionaryCache dict=new DictionaryCache(100,2);
		obj1.writeTemplateValues(buf,dict);
		// System.out.println(str.length()+" vs "+buf.position());
		buf.reset();
		// System.out.println(t.read(buf,dict));
		LazyObject obj2=(LazyObject)LazyElement.readFromTemplate(t,buf,dict);
		assertEquals(obj2.getDouble("foo"),3.1415,0);
	}

	@Test
	public void testDictionaryEffect() throws Exception{
		String str="{\"foo\":\"Hello World!\",\"bar\":0}";
		LazyObject obj1=new LazyObject(str);
		Template t=obj1.extractTemplate();
		ByteBuffer buf=ByteBuffer.allocate(1024*4096);
		buf.mark();
		DictionaryCache dict=new DictionaryCache(100,1);
		int fullSize=0;
		for(int i=0;i<100;i++){
			str="{\"foo\":\"Hello World!\",\"bar\":"+i+"}";
			fullSize+=str.length();
			obj1=new LazyObject(str);
			obj1.writeTemplateValues(buf,dict);
		}
		// System.out.println(fullSize+" vs "+buf.position());
		buf.reset();
		// System.out.println(t.read(buf,dict));
		LazyObject obj2=null;
		for(int i=0;i<100;i++){
			// System.out.println(i);
			obj2=(LazyObject)LazyElement.readFromTemplate(t,buf,dict);
		}
		assertEquals(obj1.getString("foo"),obj2.getString("foo"));
		assertEquals(obj2.getInt("bar"),99);
	}
}