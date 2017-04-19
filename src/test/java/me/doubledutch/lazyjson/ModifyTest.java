package me.doubledutch.lazyjson;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import java.net.*;

public class ModifyTest{
    @Test
    public void changeStringTest() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":42}";
        LazyObject obj=new LazyObject(str);
		assertEquals(obj.getString("foo"),"bar");
		obj.put("foo","Hello World");
		assertEquals(obj.getString("foo"),"Hello World");
		assertEquals(obj.toString(),"{\"foo\":\"Hello World\",\"baz\":42}");
    }

    @Test
    public void addStringTest() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":42}";
        LazyObject obj=new LazyObject(str);
		obj.put("test","Hello World");
		assertEquals(obj.getString("test"),"Hello World");
		assertEquals(obj.getString("foo"),"bar");
		assertEquals(obj.toString(),"{\"foo\":\"bar\",\"baz\":42,\"test\":\"Hello World\"}");
    }

    @Test
    public void buildObjectTest() throws LazyException{
        LazyObject obj=new LazyObject();
        obj.put("foo","bar");
        obj.put("baz",42);
        obj.put("dval",3.1415);
        obj.put("fval",false);
        obj.put("tval",true);
		obj.put("test","Hello World");
		obj.put("nval",LazyObject.NULL);
		assertEquals(obj.getString("test"),"Hello World");
		assertEquals(obj.getInt("baz"),42);
		assertEquals(obj.getLong("baz"),42l);
		assertEquals(obj.getDouble("dval"),3.1415,0.0);
		assertEquals(obj.getBoolean("fval"),false);
		assertEquals(obj.getBoolean("tval"),true);
		assertEquals(obj.getString("foo"),"bar");
		assertEquals(obj.toString(),"{\"foo\":\"bar\",\"baz\":42,\"dval\":3.1415,\"fval\":false,\"tval\":true,\"test\":\"Hello World\",\"nval\":null}");
    }

    @Test
    public void cleanObjectToObject() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":{\"foo\":9}}";
        LazyObject obj=new LazyObject(str);
        obj.put("test",obj.getJSONObject("baz"));
        assertEquals(obj.getJSONObject("test").getInt("foo"),9);
    }

    @Test
    public void dirtyObjectToObjectSameBuf() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":{\"foo\":9}}";
        LazyObject obj=new LazyObject(str);
        obj.getJSONObject("baz").put("foo",10);
        System.out.println(obj.toString());
        obj.put("test",obj.getJSONObject("baz"));
        System.out.println(obj.toString());
        assertEquals(obj.getJSONObject("test").getInt("foo"),10);
    }
}