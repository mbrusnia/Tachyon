package org.fhcrc.optides.Utils;

import java.util.ArrayList;
import java.util.Collections;

/*
 * An ArrayList that sorts its elements
 */
public class SortedArrayList<T> extends ArrayList<T> {
	public void insertSorted(T value, String order) {
        add(value);
        Comparable<T> cmp = (Comparable<T>) value;
        if(order == "desc")
	        for (int i = size()-1; i > 0 && cmp.compareTo(get(i-1)) < 0; i--)
	            Collections.swap(this, i, i-1);
        else if(order == "asc")
	        for (int i = size()-1; i > 0 && cmp.compareTo(get(i-1)) > 0; i--)
	            Collections.swap(this, i, i-1);
        
    }
}
