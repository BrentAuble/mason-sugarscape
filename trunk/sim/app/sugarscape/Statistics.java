package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.Schedule;
import sim.engine.Steppable;
import sim.engine.SimState;
import sim.util.Bag;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;

/**
 * Date: Mar 11, 2005
 * Time: 10:33:01 PM
 *
 * Generates some specific statistics as scheduled in the model.
 * This really should be generalized like the user selectable statistics mechanism in Ascape
 * It checks to see whether certain rules are active before attempting to calculate statistics associated
 * with those rules, and that should not be hardcoded.
 *
 */
public class Statistics implements Steppable {

	/* how many segments in the Lorenz curve */
	public static int      LORENZ_CURVE_SEGMENTS       = 10;
	public static float    LORENZ_CURVE_FACTOR         = 1f/LORENZ_CURVE_SEGMENTS;
    Sugarscape model;
    StringBuffer output;
    int outmode;
    BufferedWriter bw;
    
    int[] tags;
    public NumberFormat one_digit;
    public NumberFormat two_digit;

    public Statistics (Sugarscape sugar, Schedule schedule, int outmode) {
      one_digit = DecimalFormat.getNumberInstance();
      one_digit.setMaximumFractionDigits(1);
      one_digit.setMinimumFractionDigits(1);
      two_digit = DecimalFormat.getNumberInstance();
      two_digit.setMaximumFractionDigits(2);

      model = sugar;
      output = new StringBuffer(200);
      this.outmode = outmode;
      tags = new int[model.culture_tags];
      output.append("run,time,gini,agents_born,alive_agents,avg_vision,avg_metabolism_sugar,avg_metabolism_spice");
      if (model.print_trades) {
                       output.append(",trades,average_trade_price,total_trades");
      }
      if (model.print_culture) {
          output.append(",blues,tags");
      }
      if (model.pollution_enabled) {
          output.append(",total_pollution,non_zero_sites");
      }
      output.append("\n");
      if (outmode==model.PRINT) {

          doOutput(output);
          return;
      }
      File f = new File("exp/"+System.currentTimeMillis()+".exp");
      try {
          FileWriter fw = new FileWriter(f);
          bw = new BufferedWriter(fw);
      } catch (Exception e) {
          e.printStackTrace();
          this.outmode = model.PRINT;
      }
      output.append("run,time,gini,agents_replaced,alive_agents,vision,metabolism_sugar,metabolism_spice");
      if (model.print_trades) {
                       output.append(",trades,average_trade_price,total_trades,total_potential_trade_partners");
      }
      if (model.print_culture) {
          output.append(",blues,tags");
      }
      if (model.pollution_enabled) {
          output.append(",total_pollution,non_zero_sites");
      }
      output.append("\n");
      doOutput(output);
    }
      public void doOutput(StringBuffer line) {
          if (outmode==model.PRINT) {
              System.out.print(line);
          } else {
             try {
              bw.write(line.toString());
              bw.flush();
             } catch (Exception e) {
                 e.printStackTrace();
             }
          }
      }
      /* compute area as rectangle plus a triangle that is on top.
       * Triangle vertices = ul, ur, (ur_x, ul_y) = triangle
       */
      public double computeAreaPolygon (float x2, float y2, float x3, float y3) {
            double area = 0;
            /* compute rectangle */
            float height = y2;
            float width  = x3-x2;
            area = height*width;
            /* compute total area (rectangle + triangle) */
            area = area + (.5d * (width*(y3-y2)));
            return area;
      }
      //generate core statistics
      public void step(SimState state) {
               output.delete(0, output.length());
               float lower_left_x;
               float lower_left_y;
               float upper_left_x;
               float upper_left_y;
               model.lorenz_curve.clear();
               float vision_total = 0;
               float metab_total_sugar = 0;
               float metab_total_spice = 0;
               float age_total = 0;             
               model.agents_series.add(model.schedule.time()+1, (double)model.active_agents_count);
               float[] agent_wealth_sugar = new float[model.active_agents_count];
               float[] agent_wealth_spice = new float[model.active_agents_count];
               int[] agent_vision = new int[model.active_agents_count];
               float total_sugar = 0;
               float total_spice = 0;
               int counter = 0;
               int blues = 0;
               for (int tt=0; tt < model.culture_tags; tt++) {
                           tags[tt]=0;
               }
               Iterator<Agent> i = model.active_agents.iterator();
               while (i.hasNext()) {
                   Agent a = i.next();
                   if (a.alive) {
                       vision_total = vision_total + a.vision;
                       metab_total_sugar = metab_total_sugar + a.metabolic_rate_sugar;
                       metab_total_spice = metab_total_spice + a.metabolic_rate_spice;
                       age_total = age_total + a.age;
                       total_sugar = total_sugar + a.wealth_sugar;
                       total_spice = total_spice + a.wealth_spice;
                       agent_wealth_sugar[counter] = a.wealth_sugar;
                       agent_wealth_spice[counter] = a.wealth_spice;
                       agent_vision[counter] = a.vision;
                       if (a.culture.getAffiliation()==1) {
                           blues++;
                       }
                       counter++;
                       //calculate the number of each kind of tag
                       if (model.print_culture) {
                           for (int tt=0; tt < model.culture_tags; tt++) {
                               if (a.culture.tagset[tt]) {
                                   tags[tt]++;
                               }
                           }
                       }
                   }
               }
               /* sorting the wealth is the first step in constructing Lorenz
                * curve and calculating Gini coefficient.
                */
               if (model.active_agents_count!=0) {
                   java.util.Arrays.sort(agent_wealth_sugar);
                   java.util.Arrays.sort(agent_wealth_spice);
               }
               int bins_size = counter/LORENZ_CURVE_SEGMENTS;
               int counter2 = 0;
               float cum_total_sugar = 0;
               float cum_total_spice = 0;
               model.lorenz_curve.add(0,0);
               lower_left_x = 0;
               lower_left_y = 0;
               upper_left_x = 0;
               upper_left_y = 0;
               double gini = 0;
               float viz_total = 0;
               //double[] vals = new double[10];
               int num_agents = 0;
               //lorenz_agent_vision.clear();
               int bin = 0;
               /* WARNING there are 3 bugs in the GINI and Lorenz calculations
                * 1.  GINI goes to 1.0 within 100 time steps for fig3-4 and then NaN
                * 2.  The final bin/Lorenz curve segment isn't handled properly
                * 3.  The x boundaries of the Lorenz segments aren't right
                */
               while ( bin < (LORENZ_CURVE_SEGMENTS-1) ) {
                  viz_total = 0;
                  num_agents = 0;
                  /* add up the wealth for the bin per the number of agents in that bin */
                  for (int b = 0; b < bins_size; b++ ) {
                     cum_total_sugar = cum_total_sugar + agent_wealth_sugar[counter2];
                     //System.out.print(agent_wealth_sugar[counter2] + " ");
                     //System.out.println(cum_total_sugar);
                     cum_total_spice = cum_total_spice + agent_wealth_spice[counter2];
                     viz_total = viz_total + agent_vision[counter2];
                     counter2++;
                     num_agents++;
                  }
                  //System.out.println(cum_total_sugar );//+ " " + agent_wealth_sugar[counter2]);
                  float sugar_ratio = cum_total_sugar/total_sugar;
                  gini = gini + computeAreaPolygon(upper_left_x, upper_left_y, (bin+1)*LORENZ_CURVE_FACTOR, sugar_ratio);
                  upper_left_x = (bin+1)*.1f;
                  upper_left_y = cum_total_sugar/total_sugar;
                  model.lorenz_curve.add((bin+1)*LORENZ_CURVE_SEGMENTS, sugar_ratio*100);
                  //vals[a] = viz_total/num_agents;
                  bin++;//dataset.addValue(vision/num_agents, series1, categories[a]);
                   //plot a lorenz point
                  //lorenz_agent_vision.add((a+1)*10, vision);
               }
               /* add the remainder to the last bin */
               while (counter2 < model.active_agents_count) {
            	   cum_total_sugar = cum_total_sugar + agent_wealth_sugar[counter2];
                   //System.out.print(agent_wealth_sugar[counter2] + " ");
                   //System.out.println(cum_total_sugar);
                   cum_total_spice = cum_total_spice + agent_wealth_spice[counter2];
                   viz_total = viz_total + agent_vision[counter2];
                   counter2++;
                   num_agents++;
                   //vals[a] = viz_total/num_agents;
               }
               float sugar_ratio = cum_total_sugar/total_sugar;
               gini = gini + computeAreaPolygon(upper_left_x, upper_left_y, 1.0f, sugar_ratio);
               upper_left_x = (LORENZ_CURVE_SEGMENTS)*.1f;
               upper_left_y = cum_total_sugar/total_sugar;
               model.lorenz_curve.add((bin+1)*LORENZ_CURVE_SEGMENTS, sugar_ratio*100);
               //System.out.println(cum_total_sugar );//+ " " + agent_wealth_sugar[counter2]);           
                   //add data to various charts
                   if (model.evolution_chart_on) {
                       model.vision_series.add((double)(state.schedule.time()+1),vision_total/model.active_agents_count);
                       model.metabolism_series.add((double)(state.schedule.time()+1), metab_total_sugar/model.active_agents_count );
                   }
                   model.age.add((double)(state.schedule.time()), age_total/model.active_agents_count);
                   //model.agents_series.add((double)(state.schedule.time()), counter);

                   //model.dataset.addSeries("Vision_series",vals,10,0,12);
                   //plot a gini coeff point
                   model.gini_coeff.add((double)(state.schedule.time()+1),1.0d-(2.0d*gini));
                   model.lorenz_curve.setDescription(two_digit.format(1.0d-(2.0d*gini)));
                   //run,time,gini,agents_replaced,alive_agents,avg_vision,avg_metabolism_sugar,avg_metabolism_spice
                   output.append(model.run);
                   output.append(",");
                   output.append((double)(state.schedule.time()+1));
                   output.append(",");
                   //System.out.print(model.run+"," + (double)(state.schedule.time()));
                   System.out.println("gini = " + gini);
                   output.append(two_digit.format(1-(2*gini)));
                   output.append(",");
                   output.append(model.historical_agents);
                   output.append(",");
                   output.append(model.active_agents_count);
                   output.append(",");
                   output.append(two_digit.format(vision_total/model.active_agents_count));
                   output.append(",");
                   output.append(two_digit.format(metab_total_sugar/model.active_agents_count));
                   output.append(",");
                   output.append(two_digit.format(metab_total_spice/model.active_agents_count));
                   //output.append(",");

                  if (model.trade_chart_on && (state.schedule.time()>0)) {
                      model.trade_series.add((double)(state.schedule.time()+1),model.trades);
                  }
                  if (model.print_trades && (( (state.schedule.time()+1) % model.trades_freq) ==0)) {

                       output.append(",");
                       double prices_total = 1;
                       output.append(model.trades);
                       Object[] prices = model.trade_prices.objs;
                       for (int a = 0; a < model.trade_prices.size();a++) {
                           prices_total = prices_total * ((Double)prices[a]).doubleValue();
                       }
                       output.append(",");
                       output.append(two_digit.format(Math.pow(prices_total,(1.0d/model.trade_prices.size()))));
                       //output.append(model.trade_prices.size() + " "+ all.size());// + "," + trade_prices.size());
                       output.append(",");
                       output.append(model.total_trades);
                       output.append(",");
                       output.append(model.neighbor_occurrence);
                       model.trade_prices.clear();
                       model.total_trades+=model.trades;
                       model.trades = 0;
                   }
                   float total_pollution = 0;
                   Object[][] all_objs = model.scape_grid.field;
                   int all_objs_xx = model.scape_grid.getWidth();
                   int all_objs_yy = model.scape_grid.getHeight();
                   int non_zero_sites = 0;
                   if (model.print_culture) {
                       output.append(", Blue = " + blues + ",tags=[ ");
                       for (int tt=0; tt < model.culture_tags; tt++) {
                           output.append(tags[tt]+" ");
                       }
                       output.append("]");
                       if (model.culture_tag_chart_on) {
                           model.culture_tag_series.add((double)(state.schedule.time()+1),(double)blues/model.active_agents_count);
                       }
                   }
                   if (model.pollution_enabled) {
                       //for (int zz = 0; zz< scape_cells; zz++) {
                       for (int xx = 0; xx < all_objs_xx; xx++) {
                          for (int yy = 0; yy < all_objs_yy; yy++) {
                           Scape s = (Scape)all_objs[xx][yy];
                           total_pollution = total_pollution + s.pollution;
                               if (s.pollution > 0) {
                                   non_zero_sites++;
                               }
                          }
                       }
                       output.append(",");
                       output.append(total_pollution);
                       output.append(",");
                       output.append(non_zero_sites);
                   }
                   output.append("\n");
                   doOutput(output);
                   //run,time,gini,agents_replaced,alive_agents,avg_vision,avg_metabolism
            }
      }


