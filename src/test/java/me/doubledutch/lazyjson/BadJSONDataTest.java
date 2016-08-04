
package me.doubledutch.lazyjson;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class BadJSONDataTest{
    @Test(expected=LazyException.class)
    public void testBadNumber1() throws LazyException{
        String str="{\"foo\":-f}";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testBadNumber2() throws LazyException{
        String str="{\"foo\":-9.f}";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testBadNumber3() throws LazyException{
        String str="{\"foo\":-9.1ef}";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testBadNumber4() throws LazyException{
        String str="{\"foo\":-9.1e-f}";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testHalfObject() throws LazyException{
        String str="{";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testUnexpectedEndObject() throws LazyException{
        String str="{}}";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testMissingFieldValue() throws LazyException{
        String str="{\"foo\":}";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testMissingField() throws LazyException{
        String str="{\"foo\"}";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testBadField() throws LazyException{
        String str="{42:\"foo\"}";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testMissingFieldForArray() throws LazyException{
        String str="{[\"foo\"]}";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testHalfArray() throws LazyException{
        String str="[";
        LazyArray obj=new LazyArray(str);
    }

    @Test(expected=LazyException.class)
    public void testBadArraySeparator() throws LazyException{
        String str="[9:4]";
        LazyArray obj=new LazyArray(str);
    }
}