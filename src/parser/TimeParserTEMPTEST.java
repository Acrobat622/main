package parser;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class TimeParserTEMPTEST {

    @Test
    public void test() {
        ArrayList<String> times1 = new ArrayList<String>();
     
     // test HH:MM o'clock
        times1.clear();
        times1.add("11:00");
        assertEquals(times1,
                TimeParser.extractTime("mds sale start at 11"));
  
    }
        
        
    
}


