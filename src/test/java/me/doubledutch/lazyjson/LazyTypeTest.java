package me.doubledutch.lazyjson;


import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class LazyTypeTest{
	@Test
	public void testObjectValueTypes() throws LazyException{
		LazyObject obj=new LazyObject("{\"foo\":42,\"bar\":3.1415,\"baz\":\"Hello World!\",\"baz2\":\"\\n\",\"bonk\":false,\"test\":null,\"obj\":{},\"arr\":[]}");
		assertEquals(obj.getType("foo"),LazyType.INTEGER);
		assertEquals(obj.getType("bar"),LazyType.FLOAT);
		assertEquals(obj.getType("baz"),LazyType.STRING);
		assertEquals(obj.getType("baz2"),LazyType.STRING);
		assertEquals(obj.getType("bonk"),LazyType.BOOLEAN);
		assertEquals(obj.getType("test"),LazyType.NULL);
		assertEquals(obj.getType("obj"),LazyType.OBJECT);
		assertEquals(obj.getType("arr"),LazyType.ARRAY);
	}

	@Test
	public void testArrayValueTypes() throws LazyException{
		LazyArray arr=new LazyArray("[42,3.1415,\"Hello World!\",false,null,{},[],\"\\n\"]");
		assertEquals(arr.getType(0),LazyType.INTEGER);
		assertEquals(arr.getType(1),LazyType.FLOAT);
		assertEquals(arr.getType(2),LazyType.STRING);
		assertEquals(arr.getType(3),LazyType.BOOLEAN);
		assertEquals(arr.getType(4),LazyType.NULL);
		assertEquals(arr.getType(5),LazyType.OBJECT);
		assertEquals(arr.getType(6),LazyType.ARRAY);
		assertEquals(arr.getType(7),LazyType.STRING);
		
	}

	@Test
	public void testValueNaming() throws LazyException{
		assertEquals(LazyType.valueOf("INTEGER"),LazyType.INTEGER);
	}
}