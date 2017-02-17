package me.doubledutch.lazyjson;


import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class DeepEqualsTest{
	@Test
	public void testObjectKeyOrderEquality() throws LazyException{
		LazyObject o1=new LazyObject("{\"foo\":13,\"bar\":42}");
		LazyObject o2=new LazyObject("{\"bar\":42,\"foo\":13}");
		LazyObject o3=new LazyObject("{\"bar\":41,\"foo\":13}");
		assertTrue(o1.equals(o2));
		assertFalse(o1.equals(o3));

		assertEquals(o1.hashCode(),o2.hashCode());
		assertNotEquals(o1.hashCode(),o3.hashCode());
	}
	@Test
	public void testObjectValueTypesEquality() throws LazyException{
		LazyObject o1=new LazyObject("{\"int\":13,\"float\":3.1415,\"string\":\"Hello World\",\"bool\":false,\"null\":null}");
		LazyObject o2=new LazyObject("{\"int\":13,\"float\":3.1415,\"string\":\"Hello World\",\"bool\":false,\"null\":null}");
		LazyObject o3=new LazyObject("{\"int\":14,\"float\":3.1415,\"string\":\"Hello World\",\"bool\":false,\"null\":null}");
		LazyObject o4=new LazyObject("{\"int\":13,\"float\":3.1415,\"string\":\"Hello World!\",\"bool\":false,\"null\":null}");
		LazyObject o5=new LazyObject("{\"int\":13,\"float\":3.1415,\"string\":\"Hello World\",\"bool\":true,\"null\":null}");
		LazyObject o6=new LazyObject("{\"int\":13,\"float\":3.1416,\"string\":\"Hello World\",\"bool\":false,\"null\":null}");
		LazyObject o7=new LazyObject("{\"int\":13,\"float\":3.1415,\"string\":\"Hello World\",\"bool\":false,\"null\":false}");

		assertTrue(o1.equals(o2));
		assertFalse(o1.equals(o3));
		assertFalse(o1.equals(o4));
		assertFalse(o1.equals(o5));
		assertFalse(o1.equals(o6));
		assertFalse(o1.equals(o7));

		assertEquals(o1.hashCode(),o2.hashCode());
		assertNotEquals(o1.hashCode(),o3.hashCode());
		assertNotEquals(o1.hashCode(),o4.hashCode());
		assertNotEquals(o1.hashCode(),o5.hashCode());
		assertNotEquals(o1.hashCode(),o6.hashCode());
		// TODO: false and null is the same value in the hashcode - should we change this?
		// assertNotEquals(o1.hashCode(),o7.hashCode());
	}

	@Test
	public void testNestedObjectEquality() throws LazyException{
		LazyObject o1=new LazyObject("{\"foo\":13,\"bar\":{\"baz\":42}}");
		LazyObject o2=new LazyObject("{\"bar\":{\"baz\":42},\"foo\":13}");
		LazyObject o3=new LazyObject("{\"bar\":{\"baz\":99},\"foo\":13}");
		assertTrue(o1.equals(o2));
		assertFalse(o1.equals(o3));

		assertEquals(o1.hashCode(),o2.hashCode());
		assertNotEquals(o1.hashCode(),o3.hashCode());
	}

	@Test
	public void testArrayValueTypesEquality() throws LazyException{
		LazyArray o1=new LazyArray("[13,3.1415,\"Hello World\",false,null]");
		LazyArray o2=new LazyArray("[13,3.1415,\"Hello World\",false,null]");
		LazyArray o3=new LazyArray("[14,3.1415,\"Hello World\",false,null]");
		LazyArray o4=new LazyArray("[13,3.1416,\"Hello World\",false,null]");
		LazyArray o5=new LazyArray("[13,3.1415,\"Hello World!\",false,null]");
		LazyArray o6=new LazyArray("[13,3.1415,\"Hello World\",true,null]");
		LazyArray o7=new LazyArray("[13,3.1415,\"Hello World\",false,false]");

		assertTrue(o1.equals(o2));
		assertFalse(o1.equals(o3));
		assertFalse(o1.equals(o4));
		assertFalse(o1.equals(o5));
		assertFalse(o1.equals(o6));
		assertFalse(o1.equals(o7));

		assertEquals(o1.hashCode(),o2.hashCode());
		assertNotEquals(o1.hashCode(),o3.hashCode());
		assertNotEquals(o1.hashCode(),o4.hashCode());
		assertNotEquals(o1.hashCode(),o5.hashCode());
		assertNotEquals(o1.hashCode(),o6.hashCode());
	}

	@Test
	public void testArrayInObjectEquality() throws LazyException{
		LazyObject o1=new LazyObject("{\"foo\":13,\"bar\":[\"baz\",42]}");
		LazyObject o2=new LazyObject("{\"bar\":[\"baz\",42],\"foo\":13}");
		LazyObject o3=new LazyObject("{\"bar\":[\"baz\",99],\"foo\":13}");
		assertTrue(o1.equals(o2));
		assertFalse(o1.equals(o3));

		assertEquals(o1.hashCode(),o2.hashCode());
		assertNotEquals(o1.hashCode(),o3.hashCode());
	}

	@Test
	public void testObjectInArrayEquality() throws LazyException{
		LazyArray o1=new LazyArray("[{\"foo\":13,\"bar\":[\"baz\",42]},3]");
		LazyArray o2=new LazyArray("[{\"bar\":[\"baz\",42],\"foo\":13},3]");
		LazyArray o3=new LazyArray("[{\"bar\":[\"baz\",99],\"foo\":13},3]");
		assertTrue(o1.equals(o2));
		assertFalse(o1.equals(o3));

		assertEquals(o1.hashCode(),o2.hashCode());
		assertNotEquals(o1.hashCode(),o3.hashCode());
	}
}