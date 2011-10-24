package sim.app.sugarscape.util;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Enumeration;

/* This class extends a hashtable to allow multiple keys that are identical.
 * This concept was taken from:   
 * 
 */
public class DuplicatesHashtable {

    private Hashtable h;
    private int array_list_size;
    private static int DEFAULT_ARRAY_LIST_SIZE = 2;

    //expected_dups is the expected number of duplicate keys
    //this parameter is used to initialize the ArrayList for each key
    public DuplicatesHashtable (int size, int expected_dups) {
       h = new Hashtable(size);
       array_list_size = expected_dups;
    }

    public DuplicatesHashtable () {
       h = new Hashtable();
       array_list_size = DEFAULT_ARRAY_LIST_SIZE;
    }

    public ArrayList get(Object key) {

       return (ArrayList)h.get(key);
    }

    public void put(Object key, Object value) {
        ArrayList a;
        if (h.get(key)!=null) {
           a = (ArrayList)(h.get(key));
        } else {
           a = new ArrayList(array_list_size);
           h.put(key, a);
        }
        a.add(value);

    }

    public Enumeration keys() {
        return h.keys();
    }
}
