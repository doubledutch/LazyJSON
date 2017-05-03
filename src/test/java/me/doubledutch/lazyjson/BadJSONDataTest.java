
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
    public void testBadComma1() throws LazyException{
        String str="{\"foo\":42,}";
        LazyObject obj=new LazyObject(str);
    }

     @Test(expected=LazyException.class)
    public void testBadComma2() throws LazyException{
        String str="[\"foo\",42,]";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testBadComma3() throws LazyException{
        String str="[],";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testBadComma4() throws LazyException{
        String str="{},";
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
    public void testRawValue() throws LazyException{
        String str="9";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testHalfObject() throws LazyException{
        String str="{";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testHalfObject2() throws LazyException{
        String str="{}}";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testUnexpectedEndObjectChar() throws LazyException{
        String str="[}";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testUnexpectedEndArrayChar() throws LazyException{
        String str="{]";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testUnexpectedEndArrayChar2() throws LazyException{
        String str="{\"foo\"]";
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
    public void testBadFieldValue1() throws LazyException{
        String str="{\"foo\":trye}";
        LazyObject obj=new LazyObject(str);
    }

     @Test(expected=LazyException.class)
    public void testBadFieldValue2() throws LazyException{
        String str="{\"foo\":nil}";
        LazyObject obj=new LazyObject(str);
    }

     @Test(expected=LazyException.class)
    public void testBadFieldValue3() throws LazyException{
        String str="{\"foo\":folse}";
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
    public void testHalfArray2() throws LazyException{
        String str="[]]";
        LazyArray obj=new LazyArray(str);
    }

    @Test(expected=LazyException.class)
    public void testBadArraySeparator() throws LazyException{
        String str="[9:4]";
        LazyArray obj=new LazyArray(str);
    }

    @Test(expected=LazyException.class)
    public void testBadType() throws LazyException{
        String str="[9,4]";
        LazyArray obj=new LazyArray(str);
        obj.getJSONObject(0);
    }

    @Test
    public void testBadOptType() throws LazyException{
        String str="[9,4]";
        LazyArray obj=new LazyArray(str);
        assertNull(obj.optJSONObject(0));
    }



    @Test(expected=LazyException.class)
    public void testBadAType() throws LazyException{
        String str="[9,4]";
        LazyArray obj=new LazyArray(str);
        obj.getJSONArray(0);
    }

    @Test
    public void testBadOptAType() throws LazyException{
        String str="[9,4]";
        LazyArray obj=new LazyArray(str);
        assertNull(obj.optJSONArray(0));
    }



    @Test(expected=LazyException.class)
    public void testBadObjectType() throws LazyException{
        String str="{\"foo\":4}";
        LazyObject obj=new LazyObject(str);
        obj.getJSONObject("foo");
    }

    @Test
    public void testBadOptObjectType() throws LazyException{
        String str="{\"foo\":4}";
        LazyObject obj=new LazyObject(str);
        assertNull(obj.optJSONObject("foo"));
    }

    @Test(expected=LazyException.class)
    public void testBadValueType() throws LazyException{
        String str="{\"foo\":\"bar\"}";
        LazyObject obj=new LazyObject(str);
        obj.getLong("foo");
    }

    @Test
    public void testLazyException(){
        String str="{{\"foo\":4}";
        try{
            LazyObject obj=new LazyObject(str);
            obj.getString("bar");
        }catch(LazyException e){
            e.toString();
        }
    }

    @Test
    public void testLazyException2(){
        String str="{\"foo\":}";
        try{
            LazyObject obj=new LazyObject(str);
            obj.getString("bar");
        }catch(LazyException e){
            e.toString();
        }
    }
}