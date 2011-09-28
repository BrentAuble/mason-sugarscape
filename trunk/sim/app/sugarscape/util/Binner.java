package sim.app.sugarscape.util;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

/*
 * Create frequencies for values in range of 0-25
 */
public class Binner {

    public Binner (String configfile, String _min, String _max) {
	
    int max = Integer.parseInt(_max);
    int min = Integer.parseInt(_min);
    int total = max-min;
	try {
       int[] bins = new int[total];
       for (int a = 0; a < total; a++) {
           bins[a] = 0;
       }
       File f = new File(configfile);
       FileReader fr = new FileReader(f);
       BufferedReader br = new BufferedReader(fr);

       String line = br.readLine();
       while (line!=null) {
          int bin = Integer.parseInt(line);
          bins[bin-min] = bins[bin-min]+1;
          line = br.readLine();
       }
       br.close();
       fr.close();
       for (int a = 0; a < total; a++) {
          System.out.print( a+min + " ");
       }
       System.out.println();
       for (int a = 0; a < total; a++) {
          System.out.print( bins[a]+ " ");
       }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void main (String[] args) {
        if (args.length!=3) {
            System.out.println("Usage:  java binner filename min max");
            System.exit(1);
        }
        Binner b = new Binner(args[0], args[1], args[2]);
    }
}
