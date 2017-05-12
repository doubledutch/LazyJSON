package me.doubledutch.lazyjson;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import java.net.*;

public class ModifyTest{
    @Test
    public void changeStringTest() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":42,\"aval\":[2,2,4],\"oval\":{\"foo\":9}}";
        LazyObject obj=new LazyObject(str);
		assertEquals(obj.getString("foo"),"bar");
		obj.put("foo","Hello World");
		assertEquals(obj.getString("foo"),"Hello World");
		assertEquals(obj.toString(),"{\"foo\":\"Hello World\",\"baz\":42,\"aval\":[2,2,4],\"oval\":{\"foo\":9}}");
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
    public void addObjectTest() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":42}";
        LazyObject root=new LazyObject();
        LazyObject obj=new LazyObject(str);
        root.put("test",obj);
        assertEquals(root.toString(),"{\"test\":{\"foo\":\"bar\",\"baz\":42}}");
        assertNotNull(obj.keys());
    }

    @Test
    public void addObjectByKeysTest() throws LazyException{
        String str="{\"foo\":{\"bar\":9}}";
        LazyObject root=new LazyObject();
        LazyObject obj=new LazyObject(str);
        root.put("test",obj.get("foo"));
        assertNotNull(obj.toString());
    }

    @Test
    public void addObjectArray() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":42}";
        LazyArray root=new LazyArray();
        LazyObject obj=new LazyObject(str);
        root.put(obj);
        assertEquals(root.toString(),"[{\"foo\":\"bar\",\"baz\":42}]");
    }

    @Test
    public void addComplexStringTest() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":42}";
        LazyObject obj=new LazyObject(str);
		obj.put("test","Hello \n\t\r\b\"\\\f World");
		assertEquals(obj.getString("test"),"Hello \n\t\r\b\"\\\f World");
		assertEquals(obj.getString("foo"),"bar");
		assertEquals(obj.toString(),"{\"foo\":\"bar\",\"baz\":42,\"test\":\"Hello \\n\\t\\r\\b\\\"\\\\\\f World\"}");
    }

    @Test
    public void testModifyKeys() throws LazyException{
        String str="{}";
        LazyObject obj=new LazyObject(str);
        obj.put("foo",9);
        obj.put("bar",10);
       // obj.put("baz",11);
        Iterator<String> it=obj.keys();
        assertTrue(it.hasNext());
        assertEquals("foo",it.next());
        assertTrue(it.hasNext());
        assertEquals("bar",it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testRemoveKeys() throws LazyException{
        String str="{\"test\":42}";
        LazyObject obj=new LazyObject(str);
        obj.put("foo",9);
        obj.put("bar",10);
        obj.remove("test");
       // obj.put("baz",11);
        Iterator<String> it=obj.keys();
        assertTrue(it.hasNext());
        assertEquals("foo",it.next());
        assertTrue(it.hasNext());
        assertEquals("bar",it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void buildObjectTest() throws LazyException{
        LazyObject obj=new LazyObject();
        obj.put("foo","bar");
        obj.put("baz",-42);
        obj.put("lval",99l);
        obj.put("dval",3.1415);
        obj.put("floatval",3.1415f);
        obj.put("fval",false);
        obj.put("tval",true);
		obj.put("test","Hello World");
		obj.put("nval",LazyObject.NULL);
		assertEquals(obj.getString("test"),"Hello World");
		assertEquals(obj.getInt("baz"),-42);
		assertEquals(obj.getLong("baz"),-42l);
		assertEquals(obj.getDouble("dval"),3.1415,0.0);
		assertEquals(obj.getBoolean("fval"),false);
		assertEquals(obj.getBoolean("tval"),true);
		assertEquals(obj.getString("foo"),"bar");
		assertEquals(obj.toString(),"{\"foo\":\"bar\",\"baz\":-42,\"lval\":99,\"dval\":3.1415,\"floatval\":3.1415,\"fval\":false,\"tval\":true,\"test\":\"Hello World\",\"nval\":null}");
    }

     @Test
    public void buildObjectFromObjectsTest() throws LazyException{
        LazyObject obj=new LazyObject();
        obj.put("foo",(Object)"bar");
        obj.put("baz",(Integer)42);
        obj.put("lval",(Long)99l);
        obj.put("dval",(Double)3.1415);
        obj.put("floatval",(Float)3.1415f);
        obj.put("fval",(Boolean)false);
        obj.put("tval",(Boolean)true);
		obj.put("test","Hello World");
		obj.put("nval",LazyObject.NULL);
		assertEquals(obj.getString("test"),"Hello World");
		assertEquals(obj.getInt("baz"),42);
		assertEquals(obj.getLong("baz"),42l);
		assertEquals(obj.getDouble("dval"),3.1415,0.0);
		assertEquals(obj.getBoolean("fval"),false);
		assertEquals(obj.getBoolean("tval"),true);
		assertEquals(obj.getString("foo"),"bar");
		assertEquals(obj.toString(),"{\"foo\":\"bar\",\"baz\":42,\"lval\":99,\"dval\":3.1415,\"floatval\":3.1415,\"fval\":false,\"tval\":true,\"test\":\"Hello World\",\"nval\":null}");
    }

    @Test
    public void buildArrayTest() throws LazyException{
    	LazyArray arr=new LazyArray();
    	arr.put("foo");
    	arr.put(42);
    	arr.put(99l);
    	arr.put(3.1415f);
    	arr.put(2.9);
    	arr.put(true);
    	arr.put(false);
    	arr.put(new LazyArray("[2,2,4]"));
    	arr.put(new LazyObject("{\"foo\":42}"));
    	arr.put(LazyObject.NULL);
    	assertEquals(arr.getString(0),"foo");
    	assertEquals(arr.getInt(1),42);
    	assertEquals(arr.getLong(2),99l);
    	assertEquals(arr.getDouble(3),3.1415f,0.0001);
    	assertEquals(arr.getDouble(4),2.9,0.0001);
    	assertEquals(arr.getBoolean(5),true);
    	assertEquals(arr.getBoolean(6),false);
    	assertEquals(arr.toString(),"[\"foo\",42,99,3.1415,2.9,true,false,[2,2,4],{\"foo\":42},null]");
    }

    @Test
    public void serializeArrayTest() throws LazyException{
    	LazyArray arr=new LazyArray("[null,{\"foo\":3},[2,2,4]]");
    	arr.put(42);
    	assertEquals(arr.toString(),"[null,{\"foo\":3},[2,2,4],42]");
    }

     @Test
    public void insertArrayTest() throws LazyException{
    	LazyArray arr=new LazyArray("[0,1,2,3,4,5,6,7,8,9,10]");
    	arr.put(0,"foo");
    	arr.put(1,42);
    	arr.put(2,99l);
    	arr.put(3,3.1415f);
    	arr.put(4,2.9);
    	arr.put(5,true);
    	arr.put(6,false);
    	arr.put(7,new LazyArray("[2,2,4]"));
    	arr.put(8,new LazyObject("{\"foo\":42}"));
    	// arr.put(LazyObject.NULL);
    	assertEquals(arr.getString(0),"foo");
    	assertEquals(arr.getInt(1),42);
    	assertEquals(arr.getLong(2),99l);
    	assertEquals(arr.getDouble(3),3.1415f,0.0001);
    	assertEquals(arr.getDouble(4),2.9,0.0001);
    	assertEquals(arr.getBoolean(5),true);
    	assertEquals(arr.getBoolean(6),false);
    }

    @Test(expected=LazyException.class)
    public void badInsertArrayTestA() throws LazyException{
    	LazyArray arr=new LazyArray("[]");
    	arr.put(1,"foo");
    }

    @Test(expected=LazyException.class)
    public void badInsertArrayTestB() throws LazyException{
    	LazyArray arr=new LazyArray("[2,4]");
    	arr.put(19,"foo");
    }

    @Test(expected=LazyException.class)
    public void badObjectInsert() throws LazyException{
    	LazyObject obj=new LazyObject("{}");
    	obj.put("foo",new File("./"));
    }


    @Test
    public void removeKeyTest() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":{\"foo\":9}}";
        LazyObject obj=new LazyObject(str);
        obj.remove("baz");
        assertFalse(obj.has("baz"));
        assertEquals(obj.getString("foo"),"bar");
    }

    @Test
    public void removeFirstKeyTest() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":{\"foo\":9}}";
        LazyObject obj=new LazyObject(str);
        obj.remove("foo");
        assertFalse(obj.has("foo"));
        assertTrue(obj.has("baz"));
    }

    @Test
    public void cleanArrayToObject() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":[9,2,4]}";
        LazyObject obj=new LazyObject(str);
        obj.put("test",obj.getJSONArray("baz"));
        assertEquals(obj.getJSONArray("test").getInt(0),9);
    }

    @Test
    public void cleanArrayToArray() throws LazyException{
        String str="[\"bar\",[9,2,4]]";
        LazyArray arr=new LazyArray(str);
        arr.put(arr.getJSONArray(1));
        assertEquals(arr.getJSONArray(2).getInt(0),9);
    }

    @Test
    public void cleanObjectToArray() throws LazyException{
        String str="[\"bar\",[9,2,4],{\"foo\":42}]";
        LazyArray arr=new LazyArray(str);
        arr.put(arr.getJSONObject(2));
        assertEquals(arr.getJSONObject(3).getInt("foo"),42);
    }

    @Test
    public void cleanArrayToArrayIndex() throws LazyException{
        String str="[\"bar\",[9,2,4]]";
        LazyArray arr=new LazyArray(str);
        arr.put(0,arr.getJSONArray(1));
        assertEquals(arr.getJSONArray(0).getInt(0),9);
    }

    @Test
    public void cleanObjectToArrayIndex() throws LazyException{
        String str="[\"bar\",[9,2,4],{\"foo\":42}]";
        LazyArray arr=new LazyArray(str);
        arr.put(0,arr.getJSONObject(2));
        assertEquals(arr.getJSONObject(0).getInt("foo"),42);
    }

    @Test
    public void dirtyArrayToObject() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":[9,2,4]}";
        LazyObject obj=new LazyObject(str);
        obj.getJSONArray("baz").put(42);
        obj.put("test",obj.getJSONArray("baz"));
        assertEquals(obj.getJSONArray("test").getInt(3),42);
    }

    @Test
    public void differentBufArrayToObject() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":[9,2,4]}";
        LazyObject obj=new LazyObject(str);
        String str2="[3,3,6,42]";
        obj.put("test",new LazyArray(str2));
        assertEquals(obj.getJSONArray("test").getInt(3),42);
    }

    @Test
    public void differentBufDirtyArrayToObject() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":[9,2,4]}";
        LazyObject obj=new LazyObject(str);
        String str2="[3,3,6,42]";
      	LazyArray arr=new LazyArray(str2);
      	arr.put(0);
        obj.put("test",arr);
        assertEquals(obj.getJSONArray("test").getInt(3),42);
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
        obj.put("test",obj.getJSONObject("baz"));
        assertEquals(obj.getJSONObject("test").getInt("foo"),10);
    }

    @Test
    public void separateBufObjectToObject() throws LazyException{
        String str1="{\"foo\":\"bar\"}";
        String str2="{\"baz\":{\"foo\":9}}";
        LazyObject obj1=new LazyObject(str1);
        LazyObject obj2=new LazyObject(str2);

        obj1.put("test",obj2.getJSONObject("baz"));
        assertEquals(obj1.getJSONObject("test").getInt("foo"),9);
    }
}