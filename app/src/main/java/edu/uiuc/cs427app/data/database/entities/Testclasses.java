package edu.uiuc.cs427app.data.database.entities;

import java.util.LinkedList;
import java.util.List;

/**
 * Provides utility classes and methods for testing purposes.
 */
public  class Testclasses {

    /**
     * Interface representing a simple contract with a single method a().
     */
    public interface aa {
        /**
         * Returns an integer value.
         * @return an integer result
         */
        int a();
    }

    /**
     * Implementation of the aa interface that returns 1 from a().
     */
    static public class aa1 implements aa{
        /**
         * Returns the integer 1.
         * @return always returns 1
         */
        public int a(){return 1;}
    }
    /**
     * Simple static inner class with a count field and basic methods.
     */
    static public class V {
        public int count=0;
        public List<City> cities;
        /**
         * Default constructor for V.
         */
        public V(){
            cities = new LinkedList<>();
        }
        /**
         * Returns a string representation of the V object.
         * @return a string describing the count value
         */
        @Override
        public String toString(){
            return "V{count="+count+"}";
        }
    }
    static private V v;
    /**
     * Returns the singleton instance of V, or creates a new one if it does not exist.
     * @return the singleton V instance
     */

    static public V getInstanceV(){
        if(v== null){
            v = new V();
        }
        return v;
    }

}