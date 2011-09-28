package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/


import ec.util.QuickSort;
import sim.app.sugarscape.util.DualDoubleArrayFastQSort;

public class QuickSortTester {

    public static final int EC = 0;
    public static final int GOS = 1;

    public QuickSortTester(int method, int size) {
        QuickSort q = new QuickSort();
        DualDoubleArrayFastQSort d = new DualDoubleArrayFastQSort();
        //MersenneTwisterFast m = new MersenneTwisterFast(System.currentTimeMillis());
        double[] test_array = new double[size];
        double[] dummy = new double[size];
        for (int a = 0; a < size; a++) {
           //test_array[a] = m.nextDouble()*1000;
           test_array[a] = a;
           dummy[a] = test_array[a];
           //System.out.print(test_array[a]+ " ");
        }
        test_array[size-1] = 0;
        long start = System.currentTimeMillis();
        if (method==EC) {
            q.qsort(test_array);
            //java.util.Arrays.sort(test_array);
        } else {
            d.sort(test_array, dummy);

        }
        long stop = System.currentTimeMillis();
        System.out.println(stop-start);
        //for (int b = 0; b < size; b++) {
            //System.out.print(test_array[b]+ " ");
        //}
    }




public static void main (String[] args) {
     int size = Integer.parseInt(args[1]);
     if (args[0].compareToIgnoreCase("EC")==0) {
         QuickSortTester q = new QuickSortTester(0,size);
     } else {
         QuickSortTester q = new QuickSortTester(1,size);
     }
}
}
