package sim.app.sugarscape;

/*
* Copyright 2006 by Anthony Bigbee
* Licensed under the Academic Free License version 3.0
* See the file "LICENSE" for more information
*/

import java.awt.Color;
import java.awt.Paint;

import sim.engine.Steppable;
import sim.engine.SimState;


public class Scape implements Steppable {


      public int[] capacity;

      public int[] regen_rate;
      public int seasonal_rate;
      //ublic float[]current_level;
      public int[]current_level;
      public int[] previous;
      public static final Color LEVEL0 = new Color (0,50,0);
      public int loc_x, loc_y;
      public int[] time_since_last_regen;
      public int hemisphere;
      public float pollution;
      public int diffusion_rate;
      public int time_since_last_diffusion;
      public Scape neighbor1, neighbor2, neighbor3, neighbor4;
      public static final int MAX_VALS = 4; /* maximum number of levels */
      //public int[] resources;
      public Sugarscape model;
      public boolean diag;

      public Scape (int x, int y, int hemis, int diffus_rate,   Sugarscape _model) {
        model = _model;
        regen_rate = new int[model.resources];
        //System.out.println("Rate set to " + growback_rate);
        current_level = new int[model.resources];
        capacity = new int[model.resources];
        previous = new int[model.resources];
        loc_x = x;
        loc_y = y;
        time_since_last_regen = new int[model.resources];
        hemisphere = hemis;
        seasonal_rate = 0;
        pollution = 0;//default
        diffusion_rate = diffus_rate;
        time_since_last_diffusion = 0;
        diag = false;
        //get objects representing Von Neumann neighbors.
        //they never change so we can keep a permanent reference to them.
      }

      public void setResource(int type, int _capacity, int regen_ra,  int lev) {
          time_since_last_regen[type] = 0;
          capacity[type] = _capacity;
          regen_rate[type] = regen_ra;
          current_level[type] = lev;
          previous[type] = lev;
      }

      public Paint resolveColor () {
        return LEVEL0;
      }
      
      public float getSize () {
        int max = 0;
          for (int a = 0; a < model.resources; a++) {
             if (current_level[a] > max) {
                 max = current_level[a];
             }
        }
        return (max/4f);
      }

      public float getPollutionSize () {
        //return (float)Math.log(pollution);
        return (pollution/16.0f);
      }

      public int getLevel(int type) {
          return current_level[type];
      }

      public void setDiag (boolean diag) {
          this.diag = diag;
      }


      //what's the current level of the biggest resource?
      public int getLevel() {
          int max = 0;
          for (int a = 0; a < model.resources; a++) {
             if (current_level[a] > max) {
                 max = current_level[a];
             }
        }
        return max;
      }

      public int getType() {
        int max = 0;
        int index = 0;
          for (int a = 0; a < model.resources; a++) {
             if (current_level[a] > max) {
                 max = current_level[a];
                 index = a;
             }
        }
        return index;
      }
      public int getTotal() {
        int max = 0;
        int index = 0;
          for (int a = 0; a < model.resources; a++) {
                 max = max + current_level[a];
        }
        return max;
      }

      public void setGrowBackRate(int rate, int type) {
          regen_rate[type] = rate;
      }

      public int getSeasonalRate() {
          return seasonal_rate;
      }

      public float getPollution () {
          return pollution;
      }

      public boolean getDiag () {
          return diag;
      }

      public void addPollution(float pollut) {
        pollution = pollution + pollut;
      }


      private void diffusePollution(Sugarscape sugar) {
        if ((time_since_last_diffusion >= diffusion_rate) && (sugar.schedule.time()>99)) {
           //get Von Neumann pollution from other sites
           //System.out.print(pollution + ":");
           float total = 0;
           if (neighbor1!=null) {
               total = total + neighbor1.pollution;
           }
           if (neighbor2!=null) {
               total = total + neighbor2.pollution;
           }
           if (neighbor3!=null) {
               total = total + neighbor3.pollution;
           }
           if (neighbor4!=null) {
               total = total + neighbor4.pollution;
           }

           //now average it
           total = total/4;
           //replace current pollution with average
           pollution = total;
           //System.out.print(pollution+ " ");
        }
        time_since_last_diffusion++;
      }

      public void step (SimState state) {
      }
}

