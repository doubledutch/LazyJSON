package me.doubledutch.lazyjson.compressor;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

import me.doubledutch.lazyjson.*;

public class TemplateExtractionTest{
	@Test
    public void matchingTemplates(){
    	LazyObject obj1=new LazyObject("{\"foo\":\"bar\"}");
    	LazyObject obj2=new LazyObject("{\"foo\":\"Hello World!\"}");

    	Template t1=obj1.extractTemplate();
    	Template t2=obj2.extractTemplate();
    	assertEquals(t1,t2);
    }

    @Test
    public void differentTemplates(){
    	LazyObject obj1=new LazyObject("{\"foo\":\"bar\"}");
    	LazyObject obj2=new LazyObject("{\"foo\":\"Hello World!\",\"baz\":\"42\"}");

    	Template t1=obj1.extractTemplate();
    	Template t2=obj2.extractTemplate();
    	assertNotEquals(t1,t2);
    }
}