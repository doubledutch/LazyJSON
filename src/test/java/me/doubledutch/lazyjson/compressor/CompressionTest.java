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
		LazyObject obj2=LazyObject.readFromTemplate(t,buf,dict);
		assertEquals(obj1.getString("foo"),obj2.getString("foo"));
	}

	@Test
	public void testIntValues() throws Exception{
		String str="{\"foo\":42,\"bar\":8192,\"baz\":60000,\"giant\":2147483650}";
		LazyObject obj1=new LazyObject(str);
		Template t=obj1.extractTemplate();
		ByteBuffer buf=ByteBuffer.allocate(4096);
		buf.mark();
		DictionaryCache dict=new DictionaryCache(100,2);
		obj1.writeTemplateValues(buf,dict);
		buf.reset();
		LazyObject obj2=LazyObject.readFromTemplate(t,buf,dict);
		assertEquals(obj1.getInt("foo"),obj2.getInt("foo"));
		assertEquals(obj1.getInt("bar"),obj2.getInt("bar"));
		assertEquals(obj1.getInt("baz"),obj2.getInt("baz"));
		assertEquals(obj1.getLong("giant"),obj2.getLong("giant"));
	}
}