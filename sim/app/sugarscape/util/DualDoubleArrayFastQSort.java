package sim.app.sugarscape.util;

/* Copyright James Gosling, Kevin Smith
 * 
 * Portions Copyright 2006 by Anthony Bigbee.
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
*/

import java.util.ArrayList;

/**
 * A quick sort demonstration algorithm
 * SortAlgorithm.java
 *
 * @author James Gosling
 * @author Kevin A. Smith
 * @version 	@(#)QSortAlgorithm.java	1.3, 29 Feb 1996
 * extended with TriMedian and InsertionSort by Denis Ahrens
 * with all the tips from Robert Sedgewick (Algorithms in C++).
 * It uses TriMedian and InsertionSort for lists shorts than 4.
 * <fuhrmann@cs.tu-berlin.de>
 */
public class DualDoubleArrayFastQSort
{
	/** This is a generic version of C.A.R Hoare's Quick Sort
	* algorithm.  This will handle arrays that are already
	* sorted, and arrays with duplicate keys.<BR>
	*
	* If you think of a one dimensional array as going from
	* the lowest index on the left to the highest index on the right
	* then the parameters to this function are lowest index or
	* left and highest index or right.  The first time you call
	* this function it will be with the parameters 0, a.length - 1.
	*
	* //@param a	   an integer array
	* //@param lo0	 left boundary of array partition
	* //@param hi0	 right boundary of array partition
	*/
	private static final int MAX=10;
    private void QuickSort(double a[], int l, int r) //, double[] list)
   {
	int M = 4;
	int i;
	int j;
	double v,T;
    Object o1, o2;

	if ((r-l)>M)
	{
		i = (r+l)/2;
		if (a[l]>a[i]) swap(a,l,i);//, list);
        if (a[l]>a[r]) swap(a,l,r);//, list);    // Tri-Median Methode!
		if (a[i]>a[r]) swap(a,i,r);//, list);

		j = r-1;
		swap(a,i,j);//, list);
		i = l;
		v = a[j];
		for(;;)
		{
			while(a[++i]<v);
			while(a[--j]>v);
			if (j<i) break;
			swap (a,i,j);//, list);
			//pause(i,j);

		}
		swap(a,i,r-1);//, list);
		QuickSort(a,l,j);//, list);
		QuickSort(a,i+1,r);//, list);
	}
}

	private void swap(double a[], int i, int j)//, double[] list)
	{
		//outputState(list);
        double T;
        double o1, o2;
        /*o1 = list[i];// T = a[i]
        o2 = list[j];// o2 = a[j]
        list[i]=o2;
        list[j]=o1;*/
		T = a[i];
		a[i] = a[j];
		a[j] = T;
	}

	private void InsertionSort(double a[], int lo0, int hi0)//, double[] list)
	{
		int i;
		int j;
		double v;
        double o1;
        double o2;

		for (i=lo0+1;i<=hi0;i++)
		{
			v = a[i];
            //o1 = list[i];
			j=i;
			while ((j>lo0) && (a[j-1]>v))
			{
				a[j] = a[j-1];
                //o2 = list[j-1];
                //outputState(list);
                //list[j] = o2;
                //outputState(list);
                //do the same thing for list

				//pause(i,j);
				j--;
			}
			a[j] = v;
            //list[j] =o1;
	 	}
	}



    public void sort(double a[], double[] list)
	{
		QuickSort(a, 0, a.length - 1);//), list);
		InsertionSort(a,0,a.length-1);//), list);
		//pause(-1,-1);
	}

    private void outputState (ArrayList a) {
        int size = a.size();
        //System.out.println("size:  "+size);
        for (int c = 0;c<size;c++) {
            //System.out.print((String)a.get(c)+" ");
        }
        System.out.println();
    }
   /* public static void main (String[] args) {
        ArrayList list1 = new ArrayList(MAX);
        FastQSortAlgorithm fastq = new FastQSortAlgorithm();
        double[] sortme = new double[MAX];
        double b;
        for (int a = 0;a<MAX;a++) {
            b = (double)Math.round(Math.random()*10);
            //b = System.currentTimeMillis();

            //System.out.print(b+" ");
            sortme[a] = b;
            //list1.add("");
            list1.add((double.toString(b)));
            System.out.print(b+" ");
            //make b a double!!!
        }

        System.out.println();
        //System.out.println("sorting...size of list = "+list1.size());
        fastq.sort(sortme, list1);
        for (int c = 0;c<MAX;c++) {
            System.out.print(sortme[c]+" ");
        }
        System.out.println();
        for (int c = 0;c<MAX;c++) {
            System.out.print((String)list1.get(c)+" ");
        }

    } */
}

