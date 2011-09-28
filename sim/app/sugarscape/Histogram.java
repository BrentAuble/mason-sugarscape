package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.SimState;
import sim.util.Bag;

import java.util.ArrayList;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by IntelliJ IDEA.
 * User: abigbee
 * Date: Mar 13, 2005
 * Time: 3:48:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class Histogram {

    int cols;
    int bin_size;  //i.e. tick marks on the vertical axis
    int[] bins;
    int[] bins_assoc;
    String[][] matrix;
    String xlabel;
    String assoc_label;
    int cols_max;

    public static final int MAX_HEIGHT = 100;
    public static final int MAX_COUNT_CHARS = 10;
    public static final int BIN_WIDTH = 4;
    public static final int BIN_SPACING = 1;
    public NumberFormat dec;

    public Histogram (String xlab, String assoc_lab, int columns, int bin_size) {
        cols = columns;
        xlabel = xlab;
        assoc_label = assoc_lab;
        matrix = new String[(cols)*2][MAX_HEIGHT];
        cols_max = cols+1;
        bins = new int[cols_max]; //0,1 will be unused
        bins_assoc = new int[cols+1];
        this.bin_size = bin_size;
        dec = DecimalFormat.getNumberInstance();
        dec.setMaximumFractionDigits(1);
        dec.setMinimumFractionDigits(1);
    }
    //code from http://discuss.develop.com/archives/wa.exe?A2=ind0108&L=java&T=0&F=&S=&P=2825
    public static double logBaseN(double x, double base) {
        return Math.log(x) / Math.log(base);
    }
    public void zero() {
        for (int a=0; a < cols_max; a++) {
           bins[a] = 0;
           bins_assoc[a] = 0;
        }
    }

    public void render () {
         System.out.println("_______________________________");
         int max = 0;
         int bin_count;
                for (int c = 2; c < cols; c++) {
                 //System.out.print(bins[c]/5.0f+ " ");
                 if ( (bins[c]/bin_size) > (float)max) {
                     max = bins[c]/(int)bin_size;
                 }
                }
                for (int d = 2; d < cols; d++) {
                    for (int xx = max; xx >= 0; xx--) {
                        bin_count = bins[d]/bin_size;
                        if (xx==0) {
                            if (bin_count > 0) {
                              matrix[d][xx] = "*** ";
                            } else {
                                 matrix[d][xx] = "    ";
                            }
                        } else
                        if (bin_count >=xx)  {
                           matrix[d][xx] = "*** ";
                        } else {
                             matrix[d][xx] = "    ";
                        }
                    }
                }
                StringBuffer padding = new StringBuffer(MAX_COUNT_CHARS);
                char[] p = new char[MAX_COUNT_CHARS];
                for (int sp = 0; sp < MAX_COUNT_CHARS; sp++) {
                    p[sp] = ' ';
                }
                padding.append(p,0,MAX_COUNT_CHARS);
                System.out.println(padding+assoc_label);
                System.out.print(padding);
                String val;
                double v;
                for (int h = 2; h < cols; h++) {
                    if (bins[h]!=0) {
                        v = ((double)bins_assoc[h])/bins[h] ;
                        val = dec.format(v);
                    } else {
                        val = "0.0";
                        v = 0;
                    }
                    System.out.print(val+" ");
                }
                int spaces2;
                System.out.println();
                int count;
                for (int e = max; e >= 0; e--) {
                    count = e*(int)bin_size;
                    System.out.print(count);
                      if (count < 10) {
                          spaces2 = 1;
                      } else {
                          spaces2 = (int)Math.floor(logBaseN((double)count, 10)+1.0d);
                      }
                    char[] p2 = new char[MAX_COUNT_CHARS-spaces2]; //9 digits plus a space
                    for (int sp = 0; sp < p2.length; sp++) {
                        p2[sp] = ' ';
                    }
                    padding.delete(0, padding.length());
                    padding.append(p2,0,p2.length);
                    System.out.print(padding);
                    for (int f = 2; f < cols; f++) {
                        System.out.print(matrix[f][e]);
                    }
                    System.out.println("");
                }
                padding.delete(0,padding.length());
                p = new char[MAX_COUNT_CHARS];
                for (int sp = 0; sp < MAX_COUNT_CHARS; sp++) {
                    p[sp] = ' ';
                }
                padding.append(p,0,MAX_COUNT_CHARS);
                System.out.print(padding);
                for (int g = 2; g < cols; g++) {
                    System.out.print(g+"   ");
                }
                System.out.println("");
                System.out.print(padding+ xlabel);
                System.out.println("\n_______________________________");
             }


}
