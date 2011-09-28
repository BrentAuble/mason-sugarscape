package sim.app.sugarscape.util;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import javax.swing.JFrame;

public class ResultsGrapher {

   public XYSeries results = new XYSeries("Simulation Results");
   public float[][] data;
   public static final int XAXIS = 0;
   public static final int YAXIS = 1;
   public static final int XPARAM = 2;
   public static final String mapping = "map";
   //indeces in results file for x and y axes
   //each x_axis variable will be drawn as its own line
   public int[] x_axis;
   public int y_axis;
   public String[] fields;
   public int x_axis_index = -1;
   public String x_axis_fieldname;
   public int y_axis_index = -1;
   public String y_axis_fieldname;
   public int x_param_index = -1;
   public String x_param_fieldname;
   public Hashtable params;
   public int run_index, time_index;
   public ArrayList raw,sweep_vars, sweep_vals;
   public DuplicatesHashtable timezero, nonzero;
   public String sweep_file, resultsfile;
   public Hashtable output_params_map;

    //if this is set to true, use the serieis index as the parameterized value in
    //the legend instead of the actual value found in the sweep file
    //this is because a distribution is created during sweeping and not an exact value
   public boolean PARAMETERIZED_AVERAGES = true;
    //key is the run number, value is the parameter value

   /*
   x_axis=name
   y_axis=name    (only one of these per file)
   x_param=name
   */

   public ResultsGrapher (String resultsfile, String config, String sweep) {
            //System.out.println("starting...");
            output_params_map = new Hashtable(10);
            loadConfigFile(config);
            this.resultsfile = resultsfile;
            raw = new ArrayList(200);
            ArrayList xp = new ArrayList(10); //number of lines/series
            try {
               File f = new File(resultsfile);
               FileReader fr = new FileReader(f);
               BufferedReader br = new BufferedReader(fr);
               String line = br.readLine();
               String var = null;

               //get the field names
               ArrayList a = new ArrayList(10);
               StringTokenizer st0 = new StringTokenizer(line,",");
               while (st0.hasMoreTokens()) {
                   a.add(st0.nextToken());
               }
               int size = a.size();
               fields = new String[size];

               for (int b = 0;b < size; b++) {
                   fields[b] = (String)a.get(b);
                   if (fields[b].compareToIgnoreCase(x_axis_fieldname)==0) {
                       x_axis_index = b;
                   } else if (fields[b].compareToIgnoreCase(y_axis_fieldname)==0) {
                       y_axis_index = b;
                   } else if (fields[b].compareToIgnoreCase(x_param_fieldname)==0) {
                       x_param_index = b;
                   } else if (fields[b].compareToIgnoreCase("time")==0) {
                       time_index = b;
                   } else if (fields[b].compareToIgnoreCase("run")==0) {
                       run_index = b;
                       //System.out.println(run_index + " = run_index");
                   } //else if (fields[b].)
               }
               if ((x_axis_index==-1) || (y_axis_index == -1)) {
                  System.err.println("Missing an axis variable as specified in "+ config);
                  System.exit(1);
               }

               int counter = 0;
               float val;
               long time_val;
               int run_val = 0;
               int line_number = -1;
               Double x_axis_val = null;
               Double y_axis_val = null;

               Hashtable h = new Hashtable(100);
               ArrayList params = new ArrayList(100);
               timezero = new DuplicatesHashtable(100,100);
               nonzero = new DuplicatesHashtable(100,100);
               while ( (line=br.readLine()) !=null) {
                   line_number++;
                   if (line.startsWith("run")) {
                       continue;
                   }
                   //raw.add(line);
                   counter = 0;
                   time_val = -1;
                   run_val = -1;
                   StringTokenizer st = new StringTokenizer(line,",");
                   while (st.hasMoreTokens())  {
                       var = st.nextToken();
                       val = Float.parseFloat(var);
                       if (counter==time_index) {
                         time_val = Math.round(Double.parseDouble(var));
                       } else if (counter==run_index) {
                         run_val = Math.round(Float.parseFloat(var));
                       } else if (counter==y_axis_index) {
                         y_axis_val = Double.valueOf(var);
                       } else if (counter==x_axis_index) {
                         x_axis_val = Double.valueOf(var);
                       }
                       counter++;

                   }
                   if (time_val!=0) {
                     nonzero.put(new Integer(run_val),y_axis_val);
                   } else {
                     timezero.put(new Integer(run_val),x_axis_val);
                   }
                   //System.out.println(line);
               }

               System.out.println("Finished loading results file.");
               //System.out.println("x_param_fieldname = " + x_param_fieldname);
               //System.out.println("x_axis_fieldname = " + x_axis_fieldname);
               //System.out.println("y_axis_fieldname = " + y_axis_fieldname);
               loadSweepFile(sweep);
               int sweep_map = -1;
               int sweep_map_xaxis = -1;
               int sweep_map_yaxis = -1;

               for (int d = 0; d< sweep_vars.size(); d++) {
                   //System.out.print( ((String)sweep_vars.get(d))+ " ");
                   if ( ((String)sweep_vars.get(d)).compareToIgnoreCase(x_param_fieldname)==0) {
                       sweep_map = d;
                   } else if ( ((String)sweep_vars.get(d)).compareToIgnoreCase(x_axis_fieldname)==0) {
                       sweep_map_xaxis = d;
                   } else if ( ((String)sweep_vars.get(d)).compareToIgnoreCase(y_axis_fieldname)==0) {
                       sweep_map_yaxis = d;
                   }
               }

               //Identify the runs for each level of sweep_var
               int[] sweeps = buildSeries(sweep_map);
               int[] uniq_vals = getUniqueIntVals(sweeps);
               int[][] series = new int[uniq_vals.length][sweeps.length/uniq_vals.length];
               //System.out.println("+++"+uniq_vals.length);
               int counter2 = 0;
               for (int d=0; d < series.length; d++ ) {
                  for (int e = 0; e < sweeps.length/uniq_vals.length; e++) {
                      //series[d][e] = ((int[])sweep_vals.get(counter2))[sweep_map_xaxis];
                         //System.out.print(series[d][e] + " ");
                      //counter2++;
                  }
                  //System.out.println();
               }
               counter2= 0;
               ArrayList[] sweep_xprecise = new ArrayList[uniq_vals.length];
               //new ArrayList(sweep_vals.size());
               for (int k = 0; k < series.length; k++) {
                   sweep_xprecise[k] = new ArrayList(10);
                   for (int g = 0; g < sweeps.length/uniq_vals.length; g++) {
                       sweep_xprecise[k].add(sweep_vals.get(counter2));
                       counter2++;
                   }
               }
               FastQSortAlgorithm fast = new FastQSortAlgorithm();
               DualDoubleArrayFastQSort dual = new DualDoubleArrayFastQSort();

               double[][] sweep_x = new double[series.length][sweeps.length/uniq_vals.length];
               double[][] sweep_y = new double[series.length][sweeps.length/uniq_vals.length];
               for (int j=0; j < series.length; j++) {
                   fast.sort(series[j],sweep_xprecise[j]);

                   for (int i = 0; i < sweep_xprecise[j].size(); i++) {

                       Integer run = new Integer(((int[])sweep_xprecise[j].get(i))[run_index]);

                       ArrayList run_y = nonzero.get(run);
                       ArrayList run_x = timezero.get(run);
                       //System.out.print(run.intValue()+ " ");
                       int sz = run_y.size();
                       double total_y = 0d;
                       double total_x = 0d;
                       for (int m = 0; m < sz; m++) {
                           total_y = total_y + ((Double)run_y.get(m)).doubleValue();
                           total_x = total_x + ((Double)run_x.get(m)).doubleValue();
                       }
                       double y_avg = total_y/sz;
                       double x_avg = total_x/sz;

                       sweep_y[j][i] = y_avg;
                       sweep_x[j][i] = x_avg;
                       //System.out.println(x_avg+" " +y_avg+" ");

                       //System.out.println(run.intValue()+" "+ x_avg + " "+y_avg );
                       //System.out.println (((int[])sweep_x[j].get(i))[sweep_map_xaxis]);

                   }
                   dual.sort(sweep_x[j], sweep_y[j]) ;
                   //System.out.println();
               }
               //now build the graphical series
               String label = x_param_fieldname+"=";
               XYSeries[] all_xyseries = new XYSeries[series.length];

               for (int n = 0; n < series.length; n++) {
                   XYSeries xys = new XYSeries("");
                   if (PARAMETERIZED_AVERAGES) {
                       xys.setDescription(label+(n+1));
                       //xys.setName(label+(n+1));
                   } else {
                       xys.setDescription(label+uniq_vals[n]);
                       //xys.setName(label+uniq_vals[n]);
                   }

                   for (int o = 0; o<sweep_x[n].length; o++) {
                       xys.add(sweep_x[n][o],sweep_y[n][o]);
                       //System.out.println(first_letter+uniq_vals[n] + " " + sweep_x[n][o] + " " + sweep_y[n][o]);
                   }
                   all_xyseries[n] = xys;
                   //System.out.println(xys.getName());
               }
               buildGraphics(all_xyseries);
             } catch (Exception e) {
                 e.printStackTrace();
                 System.exit(1);
             }
   }
   public void buildGraphics(XYSeries[] series) {
       JFrame j = new JFrame("Results for " + resultsfile );
       j.setSize(800,1000);
       Container cp = j.getContentPane();
       ChartPanel c1 = new ChartPanel(this.createChart1(series));
       JFreeChart c = c1.getChart();
       
       //3 lines below for version .92.  Not sure how to do in 1.0.1 yet.
       //StandardLegend legend = (StandardLegend)c.getLegend();
       //legend.setDisplaySeriesLines(true);
       //legend.setDisplaySeriesShapes(true);
       cp.add(c1);
       cp.setVisible(true);
       j.pack();
       j.setVisible(true);
       //j.getContentPane().

   }

   public void buildSeriesStructures () {

   }

   public int[] getUniqueIntVals(int[] vals) {
       int count = 0;
       int size = vals.length;
       Hashtable h = new Hashtable (size);
       for (int a = 0; a < size; a++) {
           h.put(new Integer(vals[a]), "");
       }
       int[] unique = new int[h.size()];
       ArrayList dummy = new ArrayList(h.size());
       java.util.Enumeration e = h.keys();
       int counter = 0;
       while (e.hasMoreElements()) {
          unique[counter] = ((Integer)e.nextElement()).intValue();
          System.out.print(unique[counter]+" ");
          dummy.add(".");
          counter++;
       }
       FastQSortAlgorithm f = new FastQSortAlgorithm();
       f.sort(unique,dummy);
       return unique;
   }

   /*public int getUniqueYValsCount(int map) {
       int dv = 0;
       int[] temp = new double[sweep_vals.size()];
       for (int a =0; a < sweep_vals.size(); a++) {
          temp[a] = ((double[])sweep_vals.get(a))[map];
       }


       return dv;
   }*/

   public int[] buildSeries (int map) {
       int[] sweeps = new int[sweep_vals.size()];
       for (int a = 0; a<sweep_vals.size(); a++) {
           sweeps[a] = ((int[])sweep_vals.get(a))[map];
       }
       FastQSortAlgorithm fast = new FastQSortAlgorithm();
       fast.sort(sweeps,sweep_vals);
       for (int b = 0; b<sweep_vals.size(); b++) {
           int[] v = (int[])sweep_vals.get(b);
           //System.out.println(v[0]+ " " + v[1] + " "+ v[2]);
       }
       return sweeps;
   }


   public void loadConfigFile (String config) {
       try {
               File f = new File(config);
               FileReader fr = new FileReader(f);
               BufferedReader br = new BufferedReader(fr);
               String line;// = br.readLine();
               String var = null;
               while ( (line=br.readLine()) !=null) {
                   if ( (line.startsWith("#")) || (line.startsWith("//"))) {
                     continue;
                   }//ignore comment lines
                       StringTokenizer st0 = new StringTokenizer(line,"=");
                       String axis = st0.nextToken();
                       String field_name = st0.nextToken();
                       if (axis.compareToIgnoreCase("x_axis")==0) {
                           x_axis_fieldname = field_name;
                       } else if (axis.compareToIgnoreCase("y_axis")==0) {
                           y_axis_fieldname = field_name;
                       } else if (axis.compareToIgnoreCase("x_param")==0) {
                           x_param_fieldname = field_name;
                       }
                       //StringTokenizer st = new StringTokenizer(line,",");
                   }

                   //System.out.println(line);

               if ((x_axis_fieldname==null) || (y_axis_fieldname==null) ||
                   (x_param_fieldname == null)) {
                   System.out.println("Missing field in configuration file!");
               }

             } catch (Exception e) {
                 e.printStackTrace();
                 System.exit(1);
             }
        System.out.println("Finished loading config file.");
   }

   public void loadSweepFile (String sweep) {
       //get the parameter names
       //load the values
       sweep_vars = new ArrayList(10);
       sweep_vals = new ArrayList(100);
       try {
               File f = new File(sweep);
               FileReader fr = new FileReader(f);
               BufferedReader br = new BufferedReader(fr);
               String line;// = br.readLine();
               String var = null;

               line = br.readLine();
               if (line==null) {
                   throw new IOException("No data in sweep file");
               }
               StringTokenizer st1 = new StringTokenizer(line,",");
               while (st1.hasMoreTokens()) {
                   sweep_vars.add(st1.nextToken());
               }
               int size= sweep_vars.size();
               //System.out.println("sweep_vars size = " + size);
               while ( (line=br.readLine()) !=null) {
                   if ( (line.startsWith("#")) || (line.startsWith("//"))) {
                     continue;
                   }//ignore comment lines
                       StringTokenizer st0 = new StringTokenizer(line,",");
                       int [] swp = new int[size];
                       int index = 0;
                       while (st0.hasMoreTokens()) {
                          swp[index] = Integer.parseInt(st0.nextToken());
                          index++;
                       }
                       sweep_vals.add(swp);
                       //StringTokenizer st = new StringTokenizer(line,",");
                   }

                   //System.out.println(line);



             } catch (Exception e) {
                 e.printStackTrace();
                 System.exit(1);
             }
        System.out.println("Finished loading sweep file.");
   }

   JFreeChart createChart1(XYSeries[] series) {
                 JFreeChart chart3 = ChartFactory.createXYLineChart(
                 "Results",
                  x_axis_fieldname,
                  y_axis_fieldname,
                  null, //new XYSeriesCollection(series[2]),
                  PlotOrientation.VERTICAL,
                  true,
                  true,
                  false);
                  //System.out.println("Series count = " +series[0].getItemCount());
                  XYPlot plot = chart3.getXYPlot();

                  ValueAxis yAxis = plot.getRangeAxis();
                    //xAxis.setFixedDimension(100);
                  //yAxis.setFixedDimension(1.0);
                  //yAxis.setRange(0,1);
                  ValueAxis xAxis = plot.getDomainAxis();
                  //xAxis.setFixedDimension(50);

                  StandardXYItemRenderer renderer = (StandardXYItemRenderer) plot.getRenderer();
                  renderer.setSeriesPaint(0, Color.black);
                  renderer.setStroke( new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL) );

                  renderer.setItemLabelFont(new Font("Serif", Font.PLAIN, 20));
                  renderer.setItemLabelsVisible(true);

                  renderer.setSeriesItemLabelsVisible(1,true);
                  renderer.setBaseShapesVisible(true);
                  //XYLabelGenerator generator = new StandardXYLabelGenerator();

                  //"{2}", new DecimalFormat("0.00") );
                  //renderer.setLabelGenerator(generator);
                  //NumberAxis axis2 = new NumberAxis("Average Agent Vision");
                  //renderer.setItemLabelsVisible(true);
                  //axis2.setAutoRangeIncludesZero(false);
                  //axis2.setRange(0,12);
                  //plot.setRangeAxis(1, axis2);
                  plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
                    //XYSeriesCollection vision = new XYSeriesCollection(lorenz_agent_vision);
                    //plot.setDataset(1, vision);
                  //String first_letter = x_param_fieldname.substring(0,1)+"=";
                  XYSeriesCollection xys = new XYSeriesCollection();
                  for (int a = 0; a < series.length; a++) {
                     xys.addSeries(series[a]);
                     //xys.
                     //xys.getSeriesName(4);
                     System.out.println(xys.getSeries(a).getDescription());
                  }
                  plot.setDataset(0,xys);
                  return chart3;

    }

    public static void main (String args[]) {
        if (args.length!=3) {
            System.err.println("Usage:  java ResultsGrapher <results_file> <configuration_file> <sweep_file>");
            System.exit(1);
        }
        ResultsGrapher r = new ResultsGrapher(args[0], args[1], args[2]);
    }
}
