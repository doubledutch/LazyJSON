package me.doubledutch.lazyjson;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import java.net.*;

public class LazyObjectTest{
    @Test
    public void lazyElementTest() throws LazyException{
        String str="  [9]";
        LazyElement e=LazyElement.parse(str);
        assertNotNull(e);
    }

    @Test(expected=LazyException.class)
    public void badLazyElementTest() throws LazyException{
        String str="  9";
        LazyElement e=LazyElement.parse(str);
    }

    @Test
    public void keysetTest() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":42}";
        LazyObject obj=new LazyObject(str);
        Set<String> keys=obj.keySet();
        assertTrue(keys.contains("foo"));
        assertTrue(keys.contains("baz"));
        assertFalse(keys.contains("bar"));
    }

     @Test
    public void keysetWithNullTest() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":42,\"test\":null}";
        LazyObject obj=new LazyObject(str);
        Set<String> keys=obj.keySet();
        assertTrue(keys.contains("foo"));
        assertTrue(keys.contains("baz"));
        assertFalse(keys.contains("bar"));
    }

    @Test
    public void innerEqualsTest() throws LazyException{
        String str1="{\"foo\":\"bar\",\"baz\":42}";
        String str2="{\"foo\":\"bar\",\"baz\":42}";
        String str3="{\"foo\":9,\"baz\":42}";
        String str4="{\"baz\":42}";
        String str5="{\"foo2\":\"bar\",\"baz\":42}";
        String str6="{\"foo\":null,\"baz\":42}";
        LazyObject obj1=new LazyObject(str1);
        LazyObject obj2=new LazyObject(str2);
        LazyObject obj3=new LazyObject(str3);
        LazyObject obj4=new LazyObject(str4);
        LazyObject obj5=new LazyObject(str5);
        LazyObject obj6=new LazyObject(str6);
        assertTrue(obj1.equals(obj2));
        assertFalse(obj1.equals(obj3));
        assertFalse(obj1.equals(obj4));
        assertFalse(obj1.equals(obj5));
        assertFalse(obj1.equals(obj6));
        assertFalse(obj1.equals("foo"));
        assertFalse(obj1.equals(new LazyArray()));
    }

    @Test
    public void ryanTestSample2() throws LazyException{
        String str="{\"bundle_id\": null, \"application_id\": \"foo\"}";
        LazyObject obj=new LazyObject(str);
        assertNull(obj.getString("bundle_id"));
    }

    @Test
    public void escapeFieldChars() throws LazyException{
        String str="{\"foo\\n\":\"bar\",\"baz\":42}";
        LazyObject obj=new LazyObject(str);
        assertNotNull(obj.getString("foo\n"));
    }

    @Test(expected=LazyException.class)
    public void testNonObject() throws LazyException{
        String str="[{\"foo\":42}]";
        LazyObject obj=new LazyObject(str);
    }

    @Test(expected=LazyException.class)
    public void testMissingFields() throws LazyException{
        String str="{\"foo\":42}";
        LazyObject obj=new LazyObject(str);
        obj.getString("bar");
    }

    @Test
    public void objectGet() throws LazyException{
        String str="{\"foo\":9,\"bar\":true,\"baz\":3.1415,\"sval\":\"hello world\",\"fval\":false,\"nval\":null,\"aval\":[2,2,4],\"oval\":{\"foo\":42},\"eval\":\"\\n\"}";
        LazyObject obj=new LazyObject(str);
        assertTrue(obj.get("foo") instanceof Long);
        assertTrue(obj.get("bar") instanceof Boolean);
        assertTrue(obj.get("baz") instanceof Double);
        assertTrue(obj.get("sval") instanceof String);
        assertTrue(obj.get("fval") instanceof Boolean);
        assertEquals(obj.get("nval"),LazyObject.NULL);
        assertTrue(obj.get("aval") instanceof LazyArray);
        assertTrue(obj.get("oval") instanceof LazyObject);
        assertTrue(obj.get("eval") instanceof String);
    }

     @Test
    public void objectOpt() throws LazyException{
        String str="{\"foo\":9,\"bar\":true,\"baz\":3.1415,\"sval\":\"hello world\",\"fval\":false,\"nval\":null,\"aval\":[2,2,4],\"oval\":{\"foo\":42},\"eval\":\"\\n\"}";
        LazyObject obj=new LazyObject(str);
        assertTrue(obj.opt("foo") instanceof Long);
        assertTrue(obj.opt("bar") instanceof Boolean);
        assertTrue(obj.opt("baz") instanceof Double);
        assertTrue(obj.opt("sval") instanceof String);
        assertTrue(obj.opt("fval") instanceof Boolean);
        assertEquals(obj.opt("nval"),LazyObject.NULL);
        assertTrue(obj.opt("aval") instanceof LazyArray);
        assertTrue(obj.opt("oval") instanceof LazyObject);
        assertTrue(obj.opt("eval") instanceof String);
        assertNull(obj.opt("does-not-exist"));
    }


   @Test
    public void optionalArrayTest() throws LazyException{
        String str="{\"foo\":[],\"bar\":null}";
        LazyObject obj=new LazyObject(str);
        assertNull(obj.optJSONArray("bar"));
        assertNull(obj.optJSONArray("baz"));
        assertNotNull(obj.optJSONArray("foo"));
    }

    @Test
    public void optionalStringTest() throws LazyException{
        String str="{\"foo\":\"43\",\"bar\":null}";
        LazyObject obj=new LazyObject(str);
        assertNull(obj.optString("bar"));
        assertEquals(obj.optString("foo"),"43");
        assertEquals(obj.optString("foo","44"),"43");
        assertEquals(obj.optString("bar","44"),"44");
        assertEquals(obj.optString("baz","44"),"44");
        assertNull(obj.optString("baz"));
    }

    @Test
    public void optionalBooleanTest() throws LazyException{
        String str="{\"foo\":true,\"bar\":null,\"off\":false}";
        LazyObject obj=new LazyObject(str);
        assertEquals(obj.optBoolean("badonk"),false);
        assertEquals(obj.optBoolean("bar"),false);
        assertEquals(obj.optBoolean("foo"),true);
        assertEquals(obj.optBoolean("foo",false),true);
        assertEquals(obj.optBoolean("bar",true),true);
        assertEquals(obj.optBoolean("baz",true),true);
        assertEquals(obj.optBoolean("off",true),false);
    }

    @Test
    public void optionalIntTest() throws LazyException{
        String str="{\"foo\":43,\"bar\":null}";
        LazyObject obj=new LazyObject(str);
        assertEquals(obj.optInt("bar"),0);
        assertEquals(obj.optInt("foo"),43);
        assertEquals(obj.optInt("foo",44),43);
        assertEquals(obj.optInt("bar",44),44);
        assertEquals(obj.optInt("baz",44),44);
        assertEquals(obj.optInt("baz"),0);
    }

    @Test
    public void optionalLongTest() throws LazyException{
        String str="{\"foo\":43,\"bar\":null}";
        LazyObject obj=new LazyObject(str);
        assertEquals(obj.optLong("bar"),0l);
        assertEquals(obj.optLong("foo"),43l);
        assertEquals(obj.optLong("baz"),0l);
        assertEquals(obj.optLong("foo",44l),43l);
        assertEquals(obj.optLong("bar",44l),44l);
        assertEquals(obj.optLong("baz",44l),44l);
    }

    @Test
    public void optionalDoubleTest() throws LazyException{
        String str="{\"foo\":43.0,\"bar\":null}";
        LazyObject obj=new LazyObject(str);
        assertEquals(obj.optDouble("bar"),0.0,0);
        assertEquals(obj.optDouble("foo"),43.0,0);
        assertEquals(obj.optDouble("foo",44.0),43.0,0);
        assertEquals(obj.optDouble("bar",44.0),44.0,0);
        assertEquals(obj.optDouble("baz",44.0),44.0,0);
        assertEquals(obj.optDouble("baz"),0.0,0);
    }

    @Test
    public void optionalObjectTest() throws LazyException{
        String str="{\"foo\":{},\"bar\":null}";
        LazyObject obj=new LazyObject(str);
        assertNull(obj.optJSONObject("bar"));
        assertNull(obj.optJSONObject("baz"));
        assertNotNull(obj.optJSONObject("foo"));
    }

     @Test
    public void optionalNonObjectTest() throws LazyException{
        String str="{\"foo\":22,\"bar\":null}";
        LazyObject obj=new LazyObject(str);
        assertNull(obj.optJSONObject("foo"));
    }

    @Test(expected=LazyException.class)
    public void getNonObjectTest() throws LazyException{
        String str="{\"foo\":22,\"bar\":null}";
        LazyObject obj=new LazyObject(str);
        obj.getJSONObject("foo");
    }

     @Test
    public void optionalNonArrayTest() throws LazyException{
        String str="{\"foo\":22,\"bar\":null}";
        LazyObject obj=new LazyObject(str);
        assertNull(obj.optJSONArray("foo"));
    }

    @Test(expected=LazyException.class)
    public void getNonArrayTest() throws LazyException{
        String str="{\"foo\":22,\"bar\":null}";
        LazyObject obj=new LazyObject(str);
        obj.getJSONArray("foo");
    }

    @Test
    public void testRyansSample() throws LazyException{
        String str="{\"data\":{\"blah\":9},\"header\":{}}";
        LazyObject obj=new LazyObject(str);
        assertTrue(obj.has("header"));
        assertTrue(obj.has("data"));
        assertEquals(obj.toString(),str);
    }

    @Test
    public void testDeepNesting() throws LazyException{
        String str="{\"foo\":{\"foo\":{\"foo\":{\"foo\":{\"foo\":{\"foo\":{\"foo\":{\"foo\":{\"foo\":{\"foo\":{\"foo\":{\"foo\":{\"foo\":{\"foo\":{\"foo\":{\"foo\":42}}}}}}}}}}}}}}}}";
        LazyObject obj=new LazyObject(str);
        for(int i=0;i<15;i++){
            obj=obj.getJSONObject("foo");
            assertNotNull(obj);
        }
        assertEquals(42,obj.getInt("foo"));
    }

    @Test
    public void testHas() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":{\"key\":42}}";
        LazyObject obj=new LazyObject(str);
        assertTrue(obj.has("foo"));
        assertFalse(obj.has("bar"));
        assertTrue(obj.has("baz"));
        assertFalse(obj.has("key"));
        assertFalse(obj.has("random"));
    }

     @Test
    public void testKeys() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":{\"key\":42}}";
        LazyObject obj=new LazyObject(str);
        Iterator<String> it=obj.keys();
        assertTrue(it.hasNext());
        assertEquals("foo",it.next());
        assertTrue(it.hasNext());
        assertEquals("baz",it.next());
        assertFalse(it.hasNext());
    }

     @Test
    public void testEmptyKeys() throws LazyException{
        String str="{}";
        LazyObject obj=new LazyObject(str);
        Iterator<String> it=obj.keys();
        assertFalse(it.hasNext());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testKeysRemove() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":{\"key\":42}}";
        LazyObject obj=new LazyObject(str);
        Iterator<String> it=obj.keys();
        assertTrue(it.hasNext());
        it.remove();
    }

     @Test(expected=NoSuchElementException.class)
    public void testNoMoreKeys() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":{\"key\":42}}";
        LazyObject obj=new LazyObject(str);
        Iterator<String> it=obj.keys();
        assertTrue(it.hasNext());
        it.next();
        it.next();
        it.next();
        it.next();
    }

     @Test
    public void testLength() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":{\"key\":42}}";
        LazyObject obj=new LazyObject(str);
        assertEquals(2,obj.length());
        assertEquals(2,obj.length());
        str="{}";
        obj=new LazyObject(str);
        assertEquals(0,obj.length());
    }

    @Test
    public void testStringFields() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":\"\",\"bonk\":\"\\t\\b\\r\\n\\\\\\\"\\f\"}";
        LazyObject obj=new LazyObject(str);
        String value=obj.getString("foo");
        assertNotNull(value);
        assertEquals(value,"bar");
        value=obj.getString("baz");
        assertNotNull(value);
        assertEquals(value,"");
        assertEquals(obj.getString("bonk"),"\t\b\r\n\\\"\f");
    }

    @Test
    public void testIntegerFields() throws LazyException{
        String str="{\"foo\":999,\"bar\":0,\"baz\":42,\"bonk\":-378}";
        LazyObject obj=new LazyObject(str);
        assertEquals(999,obj.getInt("foo"));
        assertEquals(0,obj.getInt("bar"));
        assertEquals(42,obj.getInt("baz"));
        assertEquals(-378,obj.getInt("bonk"));
    }

    @Test
    public void testLongFields() throws LazyException{
        String str="{\"foo\":999,\"bar\":0,\"baz\":42,\"bonk\":-378,\"crazy\":12147483647}";
        LazyObject obj=new LazyObject(str);
        assertEquals(999,obj.getLong("foo"));
        assertEquals(0,obj.getLong("bar"));
        assertEquals(42,obj.getLong("baz"));
        assertEquals(-378,obj.getLong("bonk"));
        assertEquals(12147483647l,obj.getLong("crazy"));

    }

    @Test
    public void testDoubleFields() throws LazyException{
        String str="{\"foo\":3.1415,\"bar\":0.0,\"baz\":1.2345e+1,\"bonk\":-3.78}";
        LazyObject obj=new LazyObject(str);
        assertEquals(3.1415d,obj.getDouble("foo"),0);
        assertEquals(0.0,obj.getDouble("bar"),0);
        assertEquals(12.345,obj.getDouble("baz"),0);
        assertEquals(-3.78,obj.getDouble("bonk"),0);
    }

    @Test
    public void testBooleanFields() throws LazyException{
        String str="{\"foo\":false,\"bar\":true}";
        LazyObject obj=new LazyObject(str);
        assertEquals(false,obj.getBoolean("foo"));
        assertEquals(true,obj.getBoolean("bar"));
    }

    @Test(expected=LazyException.class)
    public void testNonBooleanFields() throws LazyException{
        String str="{\"foo\":false,\"bar\":42}";
        LazyObject obj=new LazyObject(str);
        obj.getBoolean("bar");
    }

    @Test
    public void testNullFields() throws LazyException{
        String str="{\"foo\":null,\"bar\":42}";
        LazyObject obj=new LazyObject(str);
        assertEquals(true,obj.isNull("foo"));
        assertEquals(false,obj.isNull("bar"));
    }

     @Test
    public void testObjectSpaces() throws LazyException{
        String str=" {    \"foo\" :\"bar\" ,   \"baz\":  42}   ";
        LazyObject obj=new LazyObject(str);
        assertEquals("bar",obj.getString("foo"));
        assertEquals(42,obj.getInt("baz"));
    }

    @Test
    public void testObjectTabs() throws LazyException{
        String str="\t{\t\"foo\"\t:\"bar\"\t,\t\t\"baz\":\t42\t}\t";
        LazyObject obj=new LazyObject(str);
        assertEquals("bar",obj.getString("foo"));
        assertEquals(42,obj.getInt("baz"));
    }

    @Test
    public void testNestedObjects() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":{\"test\":9}}";
        LazyObject obj=new LazyObject(str);
        obj=obj.getJSONObject("baz");
        assertNotNull(obj);
        assertEquals(9,obj.getInt("test"));
    }

    @Test
    public void testDeepNestedObjects() throws LazyException{
        String str="{\"foo\":\"bar\",\"baz\":{\"test\":9,\"test2\":{\"id\":100},\"second\":33}}";
        LazyObject obj=new LazyObject(str);
        obj=obj.getJSONObject("baz");
        assertNotNull(obj);
        assertEquals(9,obj.getInt("test"));
        obj=obj.getJSONObject("test2");
        assertNotNull(obj);
        assertEquals(100,obj.getInt("id"));
    }

    @Test
    public void testJSONOrgSample1() throws LazyException{
        String str="{\n    \"glossary\": {\n        \"title\": \"example glossary\",\n        \"GlossDiv\": {\n            \"title\": \"S\",\n            \"GlossList\": {\n                \"GlossEntry\": {\n                    \"ID\": \"SGML\",\n                    \"SortAs\": \"SGML\",\n                    \"GlossTerm\": \"Standard Generalized Markup Language\",\n                    \"Acronym\": \"SGML\",\n                    \"Abbrev\": \"ISO 8879:1986\",\n                    \"GlossDef\": {\n                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n                        \"GlossSeeAlso\": [\"GML\", \"XML\"]\n                    },\n                    \"GlossSee\": \"markup\"\n                }\n            }\n        }\n    }}";
        LazyObject obj=new LazyObject(str);
        LazyObject glo=obj.getJSONObject("glossary");
        assertNotNull(glo);
        assertEquals("example glossary",glo.getString("title"));
    }

    @Test
    public void testJSONOrgSample2() throws LazyException{
        String str="{\"menu\": {\n  \"id\": \"file\",\n  \"value\": \"File\",\n  \"popup\": {\n    \"menuitem\": [\n      {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"},      {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"},\n      {\"value\": \"Close\", \"onclick\": \"CloseDoc()\"}\n    ]\n  }\n}}";
        LazyObject obj=new LazyObject(str);
        LazyObject m=obj.getJSONObject("menu");
        assertNotNull(m);
        assertEquals("file",m.getString("id"));
        m=m.getJSONObject("popup");
        assertNotNull(m);
        LazyArray a=m.getJSONArray("menuitem");
        assertNotNull(a);
        LazyObject o=a.getJSONObject(1);
        assertNotNull(o);
        assertEquals("Open",o.getString("value"));
    }

    @Test
    public void testComplexObject() throws Exception{
        String str="{"+
            "\"Type\":\"rating\","+
            "\"IsDisabled\":false,"+
            "\"EventID\":\"deadbeef-dead-beef-dead-beef00000001\","+
            "\"Record\":{"+
                "\"Item\":{"+
                    "\"ID\":2983980,"+
                    "\"Rating\":5,"+
                    "\"Type\":null"+
                "},"+
                "\"User\":{"+
                    "\"ID\":478830012,"+
                    "\"First\":\"Ben\","+
                    "\"Last\":\"Boolean\","+
                    "\"Email\":\"foo@test.local\","+
                    "\"Title\":\"Chief Blame Officer\","+
                    "\"Company\":\"DoubleDutch\","+
                    "\"Department\":null"+
                "}"+
            "}"+
        "}";

        LazyObject obj=new LazyObject(str);
        LazyObject record=obj.getJSONObject("Record");
        assertNotNull(record);
        LazyObject item=record.getJSONObject("Item");
        assertEquals(item.getInt("ID"),2983980);
        assertEquals("rating",obj.getString("Type"));
        LazyObject user=record.getJSONObject("User");
        assertNotNull(user);
        assertEquals("Ben",user.getString("First"));
        assertEquals("DoubleDutch",user.getString("Company"));
        assertTrue(user.isNull("Department"));
    }
}