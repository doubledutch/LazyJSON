package me.doubledutch.lazyjson.compressor;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class DictionaryTest{
	@Test
    public void addValuesImmediately(){
    	Dictionary d=new Dictionary(1000,0);
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
    public void getValues(){
    	Dictionary d=new Dictionary(1000,0);
    	short i1=d.put("foo");
    	short i2=d.put("bar");
    	short i3=d.put("baz");

    	assertNotNull(d.get(i1));
    	assertNotNull(d.get(i2));
    	assertNotNull(d.get(i3));
    }

     @Test
    public void getNonExistingValues(){
    	Dictionary d=new Dictionary(1000,0);
    	short i1=d.put("foo");
    	short i2=d.put("bar");
    	short i3=d.put("baz");

    	assertNull(d.get((short)9283));
    }

    @Test
    public void checkWithoutAdding(){
    	Dictionary d=new Dictionary(1000,0);
    	assertEquals(-1,d.get("foo"));
    }

    @Test
    public void addValues(){
    	Dictionary d=new Dictionary(1000,1);
    	int i1=d.put("foo");
    	assertEquals(i1,-1);
    	i1=d.put("foo");
    	assertNotEquals(i1,-1);
    }

    @Test
    public void onlyAddWithinWindow(){
    	Dictionary d=new Dictionary(3,1);
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
    	Dictionary d=new Dictionary(1000,2);
    	int i1=d.put("foo");
    	assertEquals(i1,-1);
    	i1=d.put("foo");
    	assertEquals(i1,-1);
    	i1=d.put("foo");
    	assertNotEquals(i1,-1);
    }
}