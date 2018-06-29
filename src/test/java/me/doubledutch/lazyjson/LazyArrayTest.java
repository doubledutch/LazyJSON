package me.doubledutch.lazyjson;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class LazyArrayTest{
    @Test
    public void testVeryDeepNestedArray() throws LazyException{
        String str="[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[9"+
                   "]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]";
        LazyArray array=new LazyArray(str);
        assertEquals(1,array.length());
    }

    @Test
    public void testLength() throws LazyException{
        String str="[false,null,true]";
        LazyArray array=new LazyArray(str);
        assertEquals(3,array.length());
    }

    @Test
    public void testHashCode() throws LazyException{
        String str1="[false,null,true]";
        String str2="[2,[2,2,4],\"foo\"]";
        LazyArray arr1=new LazyArray(str1);
        LazyArray arr2=new LazyArray(str2);
        assertNotEquals(arr1.hashCode(),arr2.hashCode());
    }

    @Test
    public void testRemove() throws LazyException{
        String str="[2,false,null,true,9]";
        LazyArray array=new LazyArray(str);
        array.remove(0);
        assertEquals(4,array.length());
        assertEquals(9,array.getInt(3));
        array.remove(2);
        assertEquals(9,array.getInt(2));
    }

    @Test
    public void testInnerEquals() throws LazyException{
        String str1="[9,false,[3,4,5]]";
        String str2="[9,false,[3,4,5]]";
        String str3="[9,false,[3,4,7]]";
        String str4="[9,false,[3,4]]";
        LazyArray array1=new LazyArray(str1);
        LazyArray array2=new LazyArray(str2);
        LazyArray array3=new LazyArray(str3);
        LazyArray array4=new LazyArray(str4);
        assertTrue(array1.equals(array2));
        assertFalse(array1.equals(array3));
        assertFalse(array1.equals(array4));
    }

     @Test
    public void arrayGet() throws LazyException{
        String str="[\"foo\",9,true,false,3.1415,null,{\"foo\":42},[2,2,2],\"\\n\"]";
        LazyArray arr=new LazyArray(str);
        assertTrue(arr.get(0) instanceof String);
        assertTrue(arr.get(1) instanceof Long);
        assertTrue(arr.get(2) instanceof Boolean);
        assertTrue(arr.get(3) instanceof Boolean);
        assertTrue(arr.get(4) instanceof Double);
        assertEquals(arr.get(5),LazyObject.NULL);
        assertTrue(arr.get(6) instanceof LazyObject);
        assertTrue(arr.get(7) instanceof LazyArray);
        assertTrue(arr.get(8) instanceof String);
    }

     @Test
    public void arrayOpt() throws LazyException{
        String str="[\"foo\",9,true,false,3.1415,null,{\"foo\":42},[2,2,2],\"\\n\"]";
        LazyArray arr=new LazyArray(str);
        assertTrue(arr.opt(0) instanceof String);
        assertTrue(arr.opt(1) instanceof Long);
        assertTrue(arr.opt(2) instanceof Boolean);
        assertTrue(arr.opt(3) instanceof Boolean);
        assertTrue(arr.opt(4) instanceof Double);
        assertEquals(arr.opt(5),LazyObject.NULL);
        assertTrue(arr.opt(6) instanceof LazyObject);
        assertTrue(arr.opt(7) instanceof LazyArray);
        assertTrue(arr.opt(8) instanceof String);
        assertNull(arr.opt(9));
    }

    @Test(expected=LazyException.class)
    public void testOutOfBoundOp() throws LazyException{
        String str="[\"foo\",-1]";
        LazyArray array=new LazyArray(str);
        array.optInt(-1);
    }

    @Test(expected=LazyException.class)
    public void testOutOfBound() throws LazyException{
        String str="[\"foo\",-1]";
        LazyArray array=new LazyArray(str);
        array.getInt(100);
    }

    @Test(expected=LazyException.class)
    public void testOutOfBoundUnder() throws LazyException{
        String str="[\"foo\",-1]";
        LazyArray array=new LazyArray(str);
        array.getInt(-1);
    }

    @Test(expected=LazyException.class)
    public void testBooleanTypeError() throws LazyException{
        String str="[\"foo\",-1]";
        LazyArray array=new LazyArray(str);
        array.getBoolean(0);
    }

    @Test(expected=LazyException.class)
    public void testOptBooleanTypeError() throws LazyException{
        String str="[\"foo\",-1]";
        LazyArray array=new LazyArray(str);
        array.optBoolean(0);
    }

    @Test(expected=LazyException.class)
    public void testOptBooleanTypeError2() throws LazyException{
        String str="[\"foo\",-1]";
        LazyArray array=new LazyArray(str);
        array.optBoolean(0,false);
    }

    @Test(expected=LazyException.class)
    public void notJSONArray() throws LazyException{
        String str="{\"foo\":-1}";
        LazyArray array=new LazyArray(str);
    }

    @Test
    public void testEmptyLength() throws LazyException{
        String str="[]";
        LazyArray array=new LazyArray(str);
        assertEquals(0,array.length());
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
    public void testOptionalValues() throws LazyException{
        String str="[true,9,\"foo\",3.1415,{},[],null,false]";
        LazyArray array=new LazyArray(str);
        assertEquals(true,array.optBoolean(0));
        assertEquals(true,array.optBoolean(0,false));
        assertEquals(false,array.optBoolean(10));
        assertEquals(false,array.optBoolean(6));
        assertEquals(true,array.optBoolean(6,true));
        assertEquals(true,array.optBoolean(10,true));

        assertEquals(false,array.optBoolean(7,true));
        assertEquals(false,array.optBoolean(7));
        assertEquals(true,array.optBoolean(10,true));

        assertEquals(9,array.optInt(1));
        assertEquals(9,array.optInt(1,42));
        assertEquals(0,array.optInt(10));
        assertEquals(0,array.optInt(6));
        assertEquals(42,array.optInt(6,42));
        assertEquals(42,array.optInt(10,42));

        assertEquals(9,array.optLong(1));
        assertEquals(9,array.optLong(1,42));
        assertEquals(0,array.optLong(10));
        assertEquals(0,array.optLong(6));
        assertEquals(42,array.optLong(6,42));
        assertEquals(42,array.optLong(10,42));
        
        assertEquals("foo",array.optString(2));
        assertEquals("foo",array.optString(2,"bar"));
        assertEquals(null,array.optString(20));
        assertEquals("bar",array.optString(20,"bar"));

        assertEquals(3.1415,array.optDouble(3),0);
        assertEquals(3.1415,array.optDouble(3,7.9),0);
        assertEquals(0.0,array.optDouble(20),0);
        assertEquals(0.0,array.optDouble(6),0);
        assertEquals(7.9,array.optDouble(20,7.9),0);
        assertEquals(7.9,array.optDouble(6,7.9),0);

        assertNotNull(array.optJSONArray(5));
        assertNull(array.optJSONArray(50));
        assertNull(array.optJSONArray(6));

        assertNotNull(array.optJSONObject(4));
        assertNull(array.optJSONObject(6));
        assertNull(array.optJSONObject(50));
        assertNull(array.optString(6));
        assertNotNull(array.optString(6,"foo"));


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

    @Test
    public void testSerializeStringWithEscapedQuotes() {
        LazyArray lazyArray = new LazyArray();
        lazyArray.put("\"foo\" bar");
        lazyArray.put("baz");
        assertEquals("[\"\\\"foo\\\" bar\",\"baz\"]", lazyArray.toString());
    }
}