package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

/*
****************************************************
*See comments for Culture_K.java                   *
****************************************************
*/
public class Culture {

    public boolean[] tagset;
    int tags_length;
    int middle;

    public Culture (boolean[] tags) {
       tagset = tags;
       tags_length = tagset.length;
       middle = tags_length/2;
    }
    
    /*
     * Flip tag at specified position
     */
    public void flipTag(int tag) {
       tagset[tag] = !tagset[tag];
    }

    /*
     * Return fraction of culture bins that are zeros.
     */
    public double fractionZeros() {
       double counter = 0;
       for (int a=0; a < tags_length; a++) {
           if (!tagset[a]) {
               counter++;
           }
       }
       return counter/tags_length;
    }

    /*
     * Used to classify culutural affiliation.
     * Affects some rules/outcomes, but also the color of the agent
     * when displayed.
     */
    public int getAffiliation() {
       int counter = 0;
       for (int a=0; a < tags_length; a++) {
           if (tagset[a]) {
               counter++;
           }
       }
       if (counter > middle) {
           return 1;
       }
       return 0;
    }
}
