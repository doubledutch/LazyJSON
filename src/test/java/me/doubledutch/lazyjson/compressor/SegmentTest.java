package me.doubledutch.lazyjson.compressor;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.nio.ByteBuffer;

import me.doubledutch.lazyjson.*;

public class SegmentTest{
	@Test
	public void testComparison() throws Exception{
		Segment s1=new Segment("foo");
		Segment s2=new Segment("foo");
		assertTrue(s1.equals(s2));
		Segment s3=new Segment("bar");
		assertFalse(s1.equals(s3));
		Segment s4=new Segment("foo",Segment.BYTE);
		Segment s5=new Segment("foo",Segment.LONG);
		assertFalse(s4.equals(s5));
		Segment s6=new Segment(null,Segment.BYTE);
		Segment s7=new Segment(null,Segment.BYTE);
		assertTrue(s6.equals(s7));
		assertFalse(s6.equals("foo"));
		assertFalse(s6.equals(s4));
		assertFalse(s4.equals(s6));
	}
}