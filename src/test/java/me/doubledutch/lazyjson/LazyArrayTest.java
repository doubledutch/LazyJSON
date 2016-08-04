package me.doubledutch.lazyjson;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class LazyArrayTest{
    @Test
    public void testLength() throws LazyException{
        String str="[false,null,true]";
        LazyArray array=new LazyArray(str);
        assertEquals(3,array.length());
    }

    @Test
    public void testNullValues() throws LazyException{
        String str="[false,null,true]";
        LazyArray array=new LazyArray(str);
        assertEquals(false,array.getBoolean(0));
        assertTrue(array.isNull(1));
        assertFalse(array.isNull(2));
        assertEquals(true,array.getBoolean(2));
    }

    @Test
    public void testBooleanValues() throws LazyException{
        String str="[false,42,true]";
        LazyArray array=new LazyArray(str);
        assertEquals(false,array.getBoolean(0));
        assertEquals(42,array.getInt(1));
        assertEquals(true,array.getBoolean(2));
    }

    @Test
    public void testStringValues() throws LazyException{
        String str="[\"a\",\"\\u0061\"]";
        LazyArray array=new LazyArray(str);
        assertEquals("a",array.getString(0));
        assertEquals("a",array.getString(1));
    }

     @Test
    public void testStringEscapeValues() throws LazyException{
        String str="[\"\\t99\",\"foo\\r\\n\"]";
        LazyArray array=new LazyArray(str);
        assertEquals("\t99",array.getString(0));
        assertEquals("foo\r\n",array.getString(1));
    }

    @Test
    public void testDoubleValues() throws LazyException{
        String str="[0.9,3.1415,-3.78,1.2345e+1,1.2345e-1,1.2345E+1,1.2345E-1,2e+1,2e-1,2e1,0.34e-10]";
        LazyArray array=new LazyArray(str);
        assertEquals(0.9,array.getDouble(0),0);
        assertEquals(3.1415,array.getDouble(1),0);
        assertEquals(-3.78,array.getDouble(2),0);
        assertEquals(12.345,array.getDouble(3),0);
        assertEquals(0.12345,array.getDouble(4),0);
        assertEquals(12.345,array.getDouble(5),0);
        assertEquals(0.12345,array.getDouble(6),0);
        assertEquals(20.0,array.getDouble(7),0);
        assertEquals(0.2,array.getDouble(8),0);
        assertEquals(20.0,array.getDouble(9),0);
    }

    @Test
    public void testIntValues() throws LazyException{
        String str="[9,0,-378]";
        LazyArray array=new LazyArray(str);
        assertEquals(9,array.getInt(0));
        assertEquals(0,array.getInt(1));
        assertEquals(-378,array.getInt(2));
    }

    @Test
    public void testLongValues() throws LazyException{
        String str="[9,0,-378]";
        LazyArray array=new LazyArray(str);
        assertEquals(9l,array.getLong(0));
        assertEquals(0l,array.getLong(1));
        assertEquals(-378l,array.getLong(2));
    }

     @Test
    public void testInnerArrayValues() throws LazyException{
        String str="[[9,0],[\"foo\",-378]]";
        LazyArray array=new LazyArray(str);
        LazyArray a1=array.getJSONArray(0);
        assertEquals(9,a1.getInt(0));
        assertEquals(0,a1.getInt(1));
        LazyArray a2=array.getJSONArray(1);
        assertEquals(-378,a2.getInt(1));
    }

    @Test
    public void testObjectValues() throws LazyException{
        String str="[{\"foo\":\"bar\",\"baz\":{\"test\":9}}]";
        LazyArray array=new LazyArray(str);
        LazyObject obj=array.getJSONObject(0);
        obj=obj.getJSONObject("baz");
        assertNotNull(obj);
        assertEquals(9,obj.getInt("test"));

        str="[{\"i\":1024,\"b\":2048,\"p\":\"XXXXXXXXXXXXXXXXXXXX\"},{\"i\":2,\"b\":3,\"p\":\"XXXXXXXXXXXXXXXXXXXX\"},{\"i\":1024,\"b\":2048,\"p\":\"XXXXXXXXXXXXXXXXXXXX\"}]";
        array=new LazyArray(str);
        obj=array.getJSONObject(1);
        assertNotNull(obj);
        assertEquals(3,obj.getInt("b"));
    }

    @Test
    public void testArrayValues() throws LazyException{
        String str="[\"foo\",\"bar\",42]";
        LazyArray array=new LazyArray(str);
        assertEquals("foo",array.getString(0));
        assertEquals(42,array.getInt(2));
    }

    @Test
    public void testNickSample() throws LazyException{
        String str="[{\"foo\":[{}],\"[]\":\"{}\"}]";
        LazyArray input=new LazyArray(str);
        LazyObject obj=input.getJSONObject(0);
        assertNotNull(obj);
        LazyArray arr=obj.getJSONArray("foo");
        assertNotNull(arr);
        LazyObject obj2=arr.getJSONObject(0);
        assertNotNull(obj2);
        assertEquals(obj.getString("[]"),"{}");
    }
}