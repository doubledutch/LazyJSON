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
    }

    @Test
    public void addStringTest() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":42}";
        LazyObject obj=new LazyObject(str);
		obj.put("test","Hello World");
		assertEquals(obj.getString("test"),"Hello World");
		assertEquals(obj.getString("foo"),"bar");
    }
}