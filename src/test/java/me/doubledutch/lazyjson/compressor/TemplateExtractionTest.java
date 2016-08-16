package me.doubledutch.lazyjson.compressor;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

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
    public void hashcodeTests(){
        LazyObject obj1=new LazyObject("{\"foo\":\"bar\"}");
        LazyObject obj2=new LazyObject("{\"foo\":\"Hello World!\"}");
        LazyObject obj3=new LazyObject("{\"foobar\":[0,1,\"Hello World!\"]}");

        Template t1=obj1.extractTemplate();
        Template t2=obj2.extractTemplate();
        Template t3=obj3.extractTemplate();
        assertEquals(t1.hashCode(),t2.hashCode());
        assertNotEquals(t1.hashCode(),t3.hashCode());
    }

    @Test
    public void differentTemplates(){
    	LazyObject obj1=new LazyObject("{\"foo\":\"bar\"}");
    	LazyObject obj2=new LazyObject("{\"foo\":\"Hello World!\",\"baz\":\"42\"}");

    	Template t1=obj1.extractTemplate();
    	Template t2=obj2.extractTemplate();
    	assertNotEquals(t1,t2);
    }

    @Test
    public void SerializeTemplate() throws IOException{
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        DataOutputStream dout=new DataOutputStream(out);
        LazyObject obj1=new LazyObject("{\"foo\":\"bar\"}");
        LazyObject obj2=new LazyObject("{\"foo\":\"Hello World!\",\"baz\":\"42\"}");
        LazyObject obj3=new LazyObject("{\"012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\":\"foo\"}");
        LazyObject obj4=new LazyObject("{\"foo\":[0,1,2,3,4,5,6,7,8,9]}");
        Template t1=obj1.extractTemplate();
        Template t2=obj2.extractTemplate();
        Template t3=obj3.extractTemplate();
        Template t4=obj4.extractTemplate();
        t1.toDataOutput(dout);
        t2.toDataOutput(dout);
        t3.toDataOutput(dout);
        t4.toDataOutput(dout);
        dout.flush();
        DataInputStream din=new DataInputStream(new ByteArrayInputStream(out.toByteArray()));
        Template t5=Template.fromDataInput(din);
        Template t6=Template.fromDataInput(din);
        Template t7=Template.fromDataInput(din);
        Template t8=Template.fromDataInput(din);
        assertNotEquals(t5,t6);
        assertEquals(t1,t5);
        assertEquals(t2,t6);
        assertEquals(t3,t7);
        assertEquals(t4,t8);
    }
}