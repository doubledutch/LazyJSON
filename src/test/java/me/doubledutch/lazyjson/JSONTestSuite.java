
package me.doubledutch.lazyjson;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class JSONTestSuite{
    @Test(expected=LazyException.class)
    public void n_array_1_true_without_comma() throws LazyException{
    	LazyArray elm=new LazyArray("[1 true]");
    }

    @Test(expected=LazyException.class)
    public void n_array_comma_and_number() throws LazyException{
    	LazyArray elm=new LazyArray("[,1]");
    }

    @Test(expected=LazyException.class)
    public void n_array_double_comma() throws LazyException{
    	LazyArray elm=new LazyArray("[1,,2]");
    }
    @Test(expected=LazyException.class)
    public void n_array_inner_array_no_comma() throws LazyException{
    	LazyArray elm=new LazyArray("[3[4]]");
    }
    @Test(expected=LazyException.class)
    public void n_array_missing_value() throws LazyException{
    	LazyArray elm=new LazyArray("[ , \"\"]");
    }
    @Test(expected=LazyException.class)
    public void n_number_minus_01() throws LazyException{
    	LazyArray elm=new LazyArray("[-01]");
    }
    @Test(expected=LazyException.class)
    public void n_number_1_000() throws LazyException{
    	LazyArray elm=new LazyArray("[1 000.0]");
    }
    @Test(expected=LazyException.class)
    public void n_number_neg_int_starting_with_zero() throws LazyException{
    	LazyArray elm=new LazyArray("[-012]");
    }
    @Test(expected=LazyException.class)
    public void n_number_with_leading_zero() throws LazyException{
    	LazyArray elm=new LazyArray("[012]");
    }
    @Test(expected=LazyException.class)
    public void n_string_backslash_00() throws LazyException{
    	LazyArray elm=new LazyArray("[\"\\00\"]");
    }
    @Test(expected=LazyException.class)
    public void n_string_invalid_backslash_esc() throws LazyException{
    	LazyArray elm=new LazyArray("[\"\\a\"]");
    }
    @Test(expected=LazyException.class)
    public void n_string_escape_x() throws LazyException{
    	LazyArray elm=new LazyArray("[\"\\x00\"]");
    }
    @Test(expected=LazyException.class)
    public void n_object_garbage_at_end() throws LazyException{
    	LazyObject elm=new LazyObject("{\"a\":\"a\" 123}");
    }
    @Test(expected=LazyException.class)
    public void n_object_two_commas_in_a_row() throws LazyException{
    	LazyObject elm=new LazyObject("{\"a\":\"b\",,\"c\":\"d\"}");
    }

    @Test
    public void y_array_with_leading_space() throws LazyException{
    	LazyArray elm=new LazyArray("[1]");
    }
    @Test
    public void y_structure_whitespace_array() throws LazyException{
    	LazyArray elm=new LazyArray("	[]");
    }
}