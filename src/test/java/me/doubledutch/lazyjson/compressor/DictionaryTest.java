package me.doubledutch.lazyjson.compressor;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

public class DictionaryTest{
	@Test
    public void addValuesImmediately(){
    	DictionaryCache d=new DictionaryCache(1000,0);
    	int i1=d.put("foo");
    	int i2=d.put("bar");
    	int i3=d.put("baz");

    	assertNotEquals(-1,d.get("foo"));
    	assertNotEquals(-1,d.get("bar"));
    	assertNotEquals(-1,d.get("baz"));

    	assertEquals(i1,d.get("foo"));
    	assertEquals(i2,d.get("bar"));
    	assertEquals(i3,d.get("baz"));
    }
    
    @Test
    public void serialize() throws IOException{
        DictionaryCache d=new DictionaryCache(1000,0);
        int i1=d.put("foo");
        int i2=d.put("bar");
        int i3=d.put("baz");
        int i4=d.put("0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        DataOutputStream dout=new DataOutputStream(out);
        d.toDataOutputStream(dout);
        byte[] bytes=out.toByteArray();
        DictionaryCache d2=new DictionaryCache(1000,0);
        DataInputStream din=new DataInputStream(new ByteArrayInputStream(bytes));
        d2.fromDataInputStream(din);
        assertEquals(i1,d2.get("foo"));
        assertEquals(i2,d2.get("bar"));
        assertEquals(i3,d2.get("baz"));
        assertEquals(i4,d2.get("0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"));
        int i5=d2.put("bonk");
        assertEquals(i5,d2.get("bonk"));
    }

     @Test
    public void verifyDirtyFlag(){
        DictionaryCache d=new DictionaryCache(1000,0);
        assertFalse(d.isDirty());
        int i1=d.put("foo");
        assertTrue(d.isDirty());
        d.clearDirtyFlag();
        assertFalse(d.isDirty());
    }

    @Test
    public void repeatedAdd(){
        DictionaryCache d=new DictionaryCache(1000,0);
        int i1=d.put("foo");
        int i2=d.put("foo");
        int i3=d.put("foo");
        assertEquals(i1,i2);
        assertEquals(i1,i3);
    }

    @Test
    public void outOfBounds(){
        DictionaryCache d=new DictionaryCache(1000,0);
        assertNull(d.get((short)-1));
    }

    @Test
    public void getValues(){
    	DictionaryCache d=new DictionaryCache(1000,0);
    	short i1=d.put("foo");
    	short i2=d.put("bar");
    	short i3=d.put("baz");

    	assertNotNull(d.get(i1));
    	assertNotNull(d.get(i2));
    	assertNotNull(d.get(i3));
    }

     @Test
    public void getNonExistingValues(){
    	DictionaryCache d=new DictionaryCache(1000,0);
    	short i1=d.put("foo");
    	short i2=d.put("bar");
    	short i3=d.put("baz");

    	assertNull(d.get((short)9283));
    }

    @Test
    public void checkWithoutAdding(){
    	DictionaryCache d=new DictionaryCache(1000,0);
    	assertEquals(-1,d.get("foo"));
    }

    @Test
    public void addValues(){
    	DictionaryCache d=new DictionaryCache(1000,1);
    	int i1=d.put("foo");
    	assertEquals(i1,-1);
    	i1=d.put("foo");
    	assertNotEquals(i1,-1);
    }

    @Test
    public void onlyAddWithinWindow(){
    	DictionaryCache d=new DictionaryCache(3,1);
    	int i1=d.put("foo");
    	assertEquals(i1,-1);
    	d.put("bar");
    	d.put("baz");
    	d.put("bump");
    	i1=d.put("foo");
    	assertEquals(i1,-1);
    }

    @Test
    public void addRepeatedValues(){
    	DictionaryCache d=new DictionaryCache(1000,2);
    	int i1=d.put("foo");
    	assertEquals(i1,-1);
    	i1=d.put("foo");
    	assertEquals(i1,-1);
    	i1=d.put("foo");
    	assertNotEquals(i1,-1);
    }
}