package sim.app.sugarscape.util;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.app.sugarscape.Sugarscape;
import sim.engine.SimState;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.io.*;

import ec.util.ParameterDatabase;

public class ParamSweeper {

     public int num_params, sweep_id;
     public Hashtable params_sweep_hash;
     public int[][] params;
     public String[] params_names;
     public int[] param_steps;
     public int loops, steps;
     public String config;
     public ArrayList sequence;
     public String fixed_param; //used to label lines in xy graphs
     public Hashtable output_params_map;


     public static final int MAX_PARAMS = 500;  //a model with more than 500 parameters would be unbelievable.
     public static final int START = 0;
     public static final int END = 1;
     public static final int STEP = 2;
     public static final int PARAM_PARAM = 3; //start, stop, step
     private ParameterDatabase paramdb;

     /* Generate permutations based on param_sweep file
      * where each line in the file contains:
      * parameter=starting_value,ending_value,step_size
      */
     
     public ParamSweeper (int loops, ParameterDatabase _paramdb, int steps,  String param_sweep) {
       
       this.loops = loops;
       this.steps = steps;
       num_params = 0;
       sequence = new ArrayList(1000);
       //1.  load in the file
       params = new int[MAX_PARAMS][PARAM_PARAM];
       params_names = new String[MAX_PARAMS];
       paramdb = _paramdb;
       fixed_param = null;
       int start, end, step_size;
       try {
               File f = new File(param_sweep);
               FileReader fr = new FileReader(f);
               BufferedReader br = new BufferedReader(fr);
               String line = br.readLine();
               String var = null;
               String val = null;
               int index = 0;
               while (line !=null) {
                   StringTokenizer st = new StringTokenizer(line,"=");

                   if ( (st.hasMoreTokens()) ) {
                     var = st.nextToken();

                     if (st.hasMoreTokens() && (!var.startsWith("#")) ) {
                         val = st.nextToken();
                         StringTokenizer st3 = new StringTokenizer(val,",");
                         start = Integer.parseInt(st3.nextToken());
                         end  = Integer.parseInt(st3.nextToken());
                         step_size = Integer.parseInt(st3.nextToken());
                         if ( (var!=null)  && (!var.startsWith("#"))) {
                            if (var.compareToIgnoreCase("parameter")!=0) {
                                params_names[index] = var.toLowerCase();
                                //System.out.println(params_names[index]);
                                params[index][START] = start;
                                params[index][END] = end;
                                params[index][STEP] = step_size;
                                index++;
                            } else {
                                fixed_param = var;
                            }
                         }
                     }
                   }
                   line = br.readLine();
                   //System.out.println(line);
               }
               num_params = index;
               permute(); //construct the permutations
             } catch (Exception e) {
                 e.printStackTrace();
             }
     }

     public void permute() {
         //int[][] results = new results[]
         param_steps = new int[num_params];
         //int[][] permut = new int[num_params][];
         //int permutations = 1;
         for (int a = 0; a < num_params; a++) {
            param_steps[a] = (params[a][END]-params[a][START])/params[a][STEP] + 1;
            //System.out.println(param_steps[a] + " = param_steps[a]");
            if (param_steps[a]==0) {
                param_steps[a]=1;
            }
         }
         recursivePermute(0, new ArrayList(4));
     }

     public void recursivePermute (int index, ArrayList left) {
        int cur_val = params[index][START];
        for (int a = 0; a < param_steps[index] ; a++) {
        	if (index+1 != num_params) {
                 left.add(new Integer(cur_val));
                 recursivePermute(index+1, left);
                 left.remove(left.size()-1);
             } else { 
                   /*This is where we actually add to the sequence of parameter values that will be executed
                    */         	   
            	   sequence.addAll(left);
                   sequence.add(new Integer(cur_val));
                   //System.out.println(cur_val + " = cur_val");
             }
             cur_val = cur_val + params[index][STEP];
         }
     }

     public void start() {
        try {
            Sugarscape sugar = null;
            writeSweepFile();
            //walk the permutations
            int sequence_size = sequence.size();
            //System.out.println(sequence_size + " = sequence size, " + num_params + " = num_params");
            int permutations = sequence_size/num_params;
            for (int zz = 0; zz < permutations; zz++) {
                for (int a = 0; a < loops; a++) {
                    //System.out.println("loop "+a + " " + sequence.size());
                    if (sugar==null)
                        {
                            sugar = new Sugarscape(paramdb, System.currentTimeMillis(), sweep_id);
                            //update parameters hashtable
                            Hashtable sugar_params = sugar.params_hash;
                            updateParams(sugar_params, zz);
                            sugar.start();
                        }
                    double time;
                    while((time = sugar.schedule.time()) < steps)
                        {
                        //if (time % 100 == 0) System.out.println(time);
                        if (!sugar.schedule.step(sugar)) break;
                        /*if (time%500==0 && time!=0)
                            {
                            String s = "sugar." + time + ".checkpoint";
                            System.out.println("Checkpointing to file: " + s);
                            sugar.writeToCheckpoint(new java.io.File(s));
                            }*/
                        }
                    sugar.stopLogging();
                    sugar.finish();
                    sugar = null;
                }
                sweep_id++; //increment the sweep or parameter-set run id
            }
        } catch (Exception e) {
           e.printStackTrace();
           System.exit(1);
        }
     }
     public void updateParams(Hashtable parameters, int index) {
           for (int b = 0; b < num_params; b++) {
        	     
                 Integer p =  (Integer)sequence.get( (index*num_params)+b);
                 //System.out.println(b +" = param index " + p.toString());
                 parameters.put(params_names[b], p.toString());
                 //System.out.println(params_names[b] + " = " + p.toString());
             }
     }
     public void writeSweepFile() {
         int permutations = sequence.size()/num_params;
         //System.out.println("writing sweep file  " + sequence.size() );
         try {
             File f = new File (System.currentTimeMillis()+".set");
             FileWriter fw = new FileWriter(f);
             BufferedWriter buf = new BufferedWriter(fw);
             buf.write("run,");
             for (int aa = 0; aa < num_params; aa++) {
                 buf.write(params_names[aa]);
                 if (aa!= (num_params-1)) {
                     buf.write(",");
                 }
             }
             buf.write("\n");
             for (int a = 0; a < permutations; a++) {
                 buf.write( (a+1)+",");
                 for (int b = 0; b < num_params; b++) {
                    buf.write( ((Integer)sequence.get((a*num_params)+ b) ).toString() );
                    if (b!= (num_params-1)) {
                        buf.write(",");
                    }
                 }
                 buf.write("\n");
             }
             buf.flush();
             buf.close();
        } catch (Exception e) {
           e.printStackTrace();
       }
     }

     public static void main(String[] args)
            {
            int sw_id = 1;
            Sugarscape sugar = null;
            int loops = 1;
            int steps = 5000;
            String param_sweep = null;
            ParameterDatabase parameters = null;
            try {
                for(int x=0;x<args.length-1;x++)
                if (args[x].equals("-checkpoint"))
                    {
                    SimState state = SimState.readFromCheckpoint(new java.io.File(args[x+1]));
                    if (state == null) System.exit(1);
                    else if (!(state instanceof Sugarscape))
                        {
                        System.out.println("Checkpoint contains some other simulation: " + state);
                        System.exit(1);
                        }
                    else sugar = (Sugarscape)state;
                    }
                else if (args[x].equals("-loops")) {
                    loops = Integer.parseInt(args[x+1]);
                } else if (args[x].equals("-file")) {
                    parameters=new ParameterDatabase(
                                    new File(args[x+1]).getAbsoluteFile());
                    //System.out.println(parameters.size() + "  = paramdb size.");
                } else if (args[x].equals("-steps")) {
                    steps = Integer.parseInt(args[x+1]);
                } else if (args[x].equals("-sweep")) {
                    param_sweep = args[x+1];
                } else if (args[x].equals("-id")) {
                    sw_id = Integer.parseInt(args[x+1]);
                }
        if (param_sweep!=null) {
            ParamSweeper sweep = new ParamSweeper(loops, parameters,steps, param_sweep);
            sweep.sweep_id = sw_id;
            sweep.start();
            System.exit(0);
        } 
        /*
         * The code below is for non parameter sweeping execution the model.
         */
        for (int a = 0; a < loops; a++) {
            System.out.println("loop "+a);
            if (sugar==null)
                {
                sugar = new Sugarscape(parameters, System.currentTimeMillis(), 1);
                sugar.start();
                }
            double time;
            while((time = sugar.schedule.time()) < steps)
                {
                //if (time % 100 == 0) System.out.println(time);
                //Thread.sleep(1000);
                if (!sugar.schedule.step(sugar)) break;

                /*if (time%500==0 && time!=0)
                    {
                    String s = "sugar." + time + ".checkpoint";
                    System.out.println("Checkpointing to file: " + s);
                    sugar.writeToCheckpoint(new java.io.File(s));
                    }*/
                }
            //sugar.genStatistics();
            sugar.stopLogging();
            sugar.finish();
            sugar = null;
            }
        } catch (Exception e) {
           e.printStackTrace();
           System.exit(1);
        }
    }
}
