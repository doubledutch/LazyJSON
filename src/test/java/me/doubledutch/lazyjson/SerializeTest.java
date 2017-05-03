package me.doubledutch.lazyjson;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class SerializeTest{
    @Test
    public void testSerializeObject() throws LazyException{
        // TODO: look into fixing this
        /*
        String str="{\"foo\":42,\"bar\":{\"baz\":\"Hello World!\"}}";
        LazyObject obj=new LazyObject(str);
        byte[] raw=obj.toByteArray();
        assertNotNull(raw);

        // Now try to re-assemble
        LazyNode node=LazyNode.readFromBuffer(raw);
        assertNotNull(node);
        LazyObject obj2=new LazyObject(node);
        // If serialization in and out worked, this should be a working object
        assertEquals(obj.getInt("foo"),obj2.getInt("foo"));
        assertEquals(obj.getJSONObject("bar").getString("baz"),obj2.getJSONObject("bar").getString("baz"));
        // System.out.println(raw.length+" vs "+str.length());
        */
    }
}