package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.*;
import sim.field.grid.ObjectGrid2D;

import sim.util.Int2D;
import sim.util.Bag;
import sim.app.sugarscape.util.Logger;
import sim.util.MutableInt2D;
import sim.display.Display2D;

import sim.display.Console;
import ec.util.*;

import javax.swing.JFrame;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYItemLabelGenerator; 
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.plot.XYPlot;

public class Sugarscape extends SimState {

   public Display2D display;
   public JFrame displayFrame;
   public Console console;

   public ObjectGrid2D agents_grid;
   public ObjectGrid2D scape_grid;

   /* parameters */
   public int gridWidth;
   public int gridHeight;
   public int numAgents;
   public int vision_min;
   public int vision_max;
   public int repl_min;
   public int repl_max;
   public int metabolic_sugar;
   public int metabolic_spice;
   public boolean agent_replacement;
   public int north_season;
   public int south_season;
   public static int SUMMER = 0;
   public int summer_grow;
   public static int WINTER = 1;
   public int winter_grow;
   public int season_rate;
   public static int NORTH = 0;
   public static int SOUTH = 1;
   public int pollution_harvest;
   public int pollution_metabolize;
   public int pollution_diffusion;
   public boolean pollution_enabled, print_culture;
   public int pollution_start, pollution_diffuse_start;
   public int culture_tags = 11;
   public int chart_rate;
   public int chart_start;
   public int initial_endowment_min_sugar;
   public int initial_endowment_min_spice;
   public int initial_endowment_max_sugar;
   public int initial_endowment_max_spice;
   public int female_fertility_start_min;
   public int female_fertility_start_max;
   public int male_fertility_start_min;
   public int male_fertility_start_max;
   public int female_fertility_end_min;
   public int female_fertility_end_max;
   public int male_fertility_end_min;
   public int male_fertility_end_max;
   public boolean reproduction;
   public boolean toroidal;
   public boolean trade;
   public boolean chart_display;
   public boolean print_average_price;
   public boolean print_trades;
   public int resources;
   public boolean fast_draw;
   public boolean print_metabolism_bins;
   public int metabolism_bins_freq;
   public boolean culture_trade;
   
   /* states */
   public long trades;
   public int trades_freq;
   public int average_price_freq;
   public long total_trades;
   public Bag trade_prices;
   
   /* Track active agents, primarily as a convenience for Statistics class */
   public HashSet<Agent>   active_agents;
   public int              active_agents_count;
   
   public ArrayList agent_rules, environment_rules;
   public String rules_seq_agent,rules_seq_environment;
   public Hashtable agent_rules_mapping;
   public Hashtable environment_rules_mapping;
   public MutableInt2D cand;
   public int stats_out;
   public int stats_rate;
   public int stats_start;
   public String sugar_terrain;
   public String spice_terrain;
   public int neighbor_occurrence;
   public int reproduction_min;
   
   /***************************************************************************
   * CONSTANTS
   ***************************************************************************/ 
   public static final int SCHEDULE_MAX_ORDERS = 7;
   
   /* virtual infinity for things like schedule time step
    * don't use this for domains where really big numbers are involved.
    * Integer.MAX_VALUE ~= 2 billion
    * Use a Long.MAX_VALUE (9.2E18)  Double.MAX_VALUE (1.8E308)
    */
   public static final int VIRT_INFINITY = Integer.MAX_VALUE;

   public static final int PRINT = 0;
   public static final int FILE = 1;
   
   public static final int MALE = 1;
   public static final int FEMALE = 2;

   public static final int SUGAR = 0;
   public static final int SPICE = 1;

   public static final int AGENT_RULES_ORDER = 0;
   public static final int ENVIRONMENT_RULES_ORDER = 1;
   
   /* arbitrary, but used to size the active agents array */
   public static final int MAX_AGENTS = 32768;
   
   public ArrayList log_objects;
   public Logger logger;
   public int log_frequency;
   public int run;
    
   ParameterDatabase paramDB;
   public boolean debug;

   public String[] parameters;
   public long historical_agents;

   public String config_file;
   public String terrain;
   public String agents_config;
   public Hashtable params_hash;
   public String agents_file;
   String[] terrain_files;

   /***************************************************************************
    * CHARTING (JFREECHART) objects  
    **************************************************************************/ 
   /*
    * For efficiency reasons, all the JFreeChart instances should only be created if graphical charting is on,
    * so the code block should be moved to a method and/or out of this class
    */  
   public XYSeries avg_agent_vision = new XYSeries( "Vision" );
   public XYSeries lorenz_curve = new XYSeries("Lorenz Curve");

   public XYSeries gini_coeff = new XYSeries("Gini Coefficient");
   public XYSeries metabolism = new XYSeries("Metabolism");
   public XYSeries age = new XYSeries("Age");
   public XYSeries agents_series = new XYSeries("Agents");
   public XYSeriesCollection agents_series_coll = new XYSeriesCollection(agents_series);
   public XYSeries blue_agents = new XYSeries("Blue Agents");
   public XYSeries red_agents = new XYSeries("Red Agents");
   public XYSeries trade_series = new XYSeries("Trade");
   public HistogramDataset dataset = new HistogramDataset();
   public HistogramDataset age_hist_dataset = new HistogramDataset();
   public XYSeries vision_series = new XYSeries("Vision");
   public XYSeries metabolism_series = new XYSeries("Metabolism");
   public XYSeries culture_tag_series = new XYSeries("Culture Tags");

   public XYSeriesCollection trade_coll = new XYSeriesCollection(trade_series);
   public XYSeriesCollection evolution_vision_coll = new XYSeriesCollection(vision_series);
   public XYSeriesCollection evolution_metabolism_coll = new XYSeriesCollection(metabolism_series);
   public XYSeriesCollection culture_tag_coll = new XYSeriesCollection(culture_tag_series);
   public JFreeChart chart4, gini_chart, age_histo_chart, evolution_chart,
    culture_tag_chart, trade_chart;
   public boolean gini_chart_on, wealth_chart_on, population_chart_on,
    age_chart_on, evolution_chart_on, culture_tag_chart_on, trade_chart_on;

   public Sugarscape(ParameterDatabase _paramdb, long seed, int run) {
   /***************************************************************************
      SCHEDULE ORDERS
       
       0 environment rules
       1 agent rules
       2 unused 
       3 season rule
       4 statistics and logging
       5 unused
       6 text histogram
   ***************************************************************************/
      super(new MersenneTwisterFast(seed), new Schedule());
      this.run = run;
      paramDB = _paramdb;
      initialize();
   }

   /***************************************************************************
   * Inspector methods
   ***************************************************************************/
   public ArrayList getAgentRules () {
      return agent_rules;
   }

   public ArrayList getEnvironmentRules () {
      return environment_rules;
   }

   public int getCulture_Tags () {
      return culture_tags;
   }
   public int getMetabolic_Sugar () {
      return metabolic_sugar;
   }

   public int getMetabolic_Spice () {
      return metabolic_spice;
   }

   /***************************************************************************
   * Set the model parameters per whatever is supplied in a .conf file.
   * the last value in each statement is the default value if no parameter is 
   * identified.
   * 
   * Although this is a nice way to do it, one still has to 1) specify the
   * primitive type and 2) default value for *each* parameter.  An alternative
   * is to use java.util.Properties and the approach in MASON web tutorial #2:
   * http://cs.gmu.edu/~eclab/projects/mason/extensions/webtutorial2/index.html
   * 
   * I decided not to use because it would involve a creating a bunch of get
   * and set methods, and that would result in approximately the same amount of
   * code as far as I can tell. Your mileage may vary.
   ***************************************************************************/
   public void loadParameters() {
       if (paramDB==null)
    	   return;      
       /***********************************************************************
       * Environment parameters
       ***********************************************************************/   
       season_rate = paramDB.getIntWithDefault(new Parameter("season_rate"), null, VIRT_INFINITY);
       //System.out.println(season_rate + " =season_rate");
       summer_grow = paramDB.getIntWithDefault(new Parameter("summer_rate"), null, 1);
       winter_grow = paramDB.getIntWithDefault(new Parameter("winter_rate"), null, 1);

       pollution_harvest = paramDB.getIntWithDefault(new Parameter("pollution_harvest"), null,0);
       pollution_start = paramDB.getIntWithDefault(new Parameter("pollution_start"), null, 0);
       pollution_diffuse_start = paramDB.getIntWithDefault(new Parameter("pollution_diffuse_start"), null, 0);
       pollution_enabled = paramDB.getBoolean(new Parameter("pollution_enabled"), null, false);
       pollution_harvest = paramDB.getIntWithDefault(new Parameter("pollution_harvest"), null, 0);

       rules_seq_environment =  paramDB.getStringWithDefault(new Parameter("rules_sequence_environment"),null, null);

       String terrain_type = paramDB.getStringWithDefault(new Parameter("terrain_type"), null,"toroidal");
       if (terrain_type.compareToIgnoreCase("toroid")==0) {
          toroidal = true;
       }

       terrain_files[0] = paramDB.getStringWithDefault(new Parameter("sugar_terrain"), null, "conf/sugar.txt");
       terrain_files[1] = paramDB.getStringWithDefault(new Parameter("spice_terrain"), null, "conf/spice.txt");

       gridWidth = paramDB.getIntWithDefault(new Parameter("grid_width"), null, 50);
       gridHeight = paramDB.getIntWithDefault(new Parameter("grid_height"), null, 50);

       resources =  paramDB.getIntWithDefault(new Parameter("resources"), null, 1);
       
       /********************************************************************************************
       * Agent parameters
       *********************************************************************************************/
       numAgents = paramDB.getIntWithDefault(new Parameter("num_agents"), null, 50);
       agent_replacement = paramDB.getBoolean(new Parameter("replacement"), null, false);
       agents_file = paramDB.getStringWithDefault(new Parameter("agents_file"), null,null);

       vision_min = paramDB.getIntWithDefault(new Parameter("vision_min"), null, 2);
       vision_max = paramDB.getIntWithDefault(new Parameter("vision_max"), null, 1);

       repl_min = paramDB.getIntWithDefault(new Parameter("min_age"), null, 60);
       repl_max = paramDB.getIntWithDefault(new Parameter("max_age"), null, 100);

       metabolic_sugar = paramDB.getIntWithDefault(new Parameter("metabolism_sugar"), null, 1);
       metabolic_spice = paramDB.getIntWithDefault(new Parameter("metabolism_spice"), null, 1);

       reproduction = paramDB.getBoolean(new Parameter("reproduction"), null, false);
       reproduction_min = paramDB.getIntWithDefault(new Parameter("reproduction_min"),null, -1);
       female_fertility_start_min = paramDB.getIntWithDefault(new Parameter("female_fertility_start_min"), null, 12);
       female_fertility_start_max = paramDB.getIntWithDefault(new Parameter("female_fertility_start_max"), null, 15);
       female_fertility_end_min = paramDB.getIntWithDefault(new Parameter("female_fertility_end_min"), null, 40);
       female_fertility_end_max = paramDB.getIntWithDefault(new Parameter("female_fertility_end_max"), null, 50);

       male_fertility_start_min = paramDB.getIntWithDefault(new Parameter("male_fertility_start_min"), null, 12);
       male_fertility_start_max = paramDB.getIntWithDefault(new Parameter("male_fertility_start_max"), null, 15);
       male_fertility_end_min = paramDB.getIntWithDefault(new Parameter("male_fertility_end_min"), null, 50);
       male_fertility_end_max = paramDB.getIntWithDefault(new Parameter("male_fertility_end_max"), null, 60);
       
       culture_tags = paramDB.getIntWithDefault(new Parameter("culture_tags"), null, 3);

       initial_endowment_min_sugar = paramDB.getIntWithDefault(new Parameter("initial_endowment_min_sugar"), null, 50);
       initial_endowment_min_spice = paramDB.getIntWithDefault(new Parameter("initial_endowment_min_spice"), null, 50);
       initial_endowment_max_sugar = paramDB.getIntWithDefault(new Parameter("initial_endowment_max_sugar"), null, 100);
       initial_endowment_max_spice = paramDB.getIntWithDefault(new Parameter("initial_endowment_max_spice"), null, 100);

       rules_seq_agent = paramDB.getStringWithDefault(new Parameter("rules_sequence_agents"), null,null);

       /********************************************************************************************
       * Statistics, graphing, and logging params, and other housekeeping
       *********************************************************************************************/
       debug = paramDB.getBoolean(new Parameter("debug"), null, false);
       stats_rate = paramDB.getIntWithDefault(new Parameter("stats_rate"), null, 1);
       stats_start = paramDB.getIntWithDefault(new Parameter("stats_start"), null, 1);
       String statistics_output = paramDB.getStringWithDefault(new Parameter("stats_start"), null,"print");
       if (statistics_output.compareToIgnoreCase("print")==0) {
           stats_out = PRINT;
       } else if (statistics_output.compareToIgnoreCase("file")==0) {
           stats_out = FILE;
       } else {
           stats_out = PRINT;
       }

       chart_start = paramDB.getIntWithDefault(new Parameter("chart_start"), null, 1);
       chart_display = paramDB.getBoolean(new Parameter("chart_display"), null, false);
       chart_rate = paramDB.getIntWithDefault(new Parameter("chart_rate"), null, 1);

       population_chart_on = paramDB.getBoolean(new Parameter("population_chart"), null, false);
       gini_chart_on = paramDB.getBoolean(new Parameter("gini_chart"),null,false);
       wealth_chart_on = paramDB.getBoolean(new Parameter("wealth_chart"),null,false);
       age_chart_on = paramDB.getBoolean(new Parameter("age_chart"),null,false);
       evolution_chart_on = paramDB.getBoolean(new Parameter("evolution_chart"),null,false);
       culture_tag_chart_on = paramDB.getBoolean(new Parameter("culture_tag_chart"),null,false);
       trade_chart_on = paramDB.getBoolean(new Parameter("trade_chart"),null,false);
       print_trades = paramDB.getBoolean(new Parameter("print_trades"), null, false);
       trades_freq = paramDB.getIntWithDefault(new Parameter("print_trades_freq"),null,500);
       log_frequency = paramDB.getIntWithDefault(new Parameter("log_frequency"),null, 1);
       
       print_metabolism_bins = paramDB.getBoolean(new Parameter("print_metabolism_bins"),null,false);
       print_culture = paramDB.getBoolean(new Parameter("print_culture"),null, false);
       metabolism_bins_freq = VIRT_INFINITY;
       
       /* Uncomment the line below to see the actual parameter values. */
       Enumeration e = paramDB.keys();
       while (e.hasMoreElements()) {
          String s = (String)e.nextElement();
          //System.out.println(paramDB.getProperty(s) + "\t" + s);
       }
   }

   public void initialize() {
      params_hash = new Hashtable(100);
      agent_rules = new ArrayList(RulesSequence.MAX_RULES);
      environment_rules = new ArrayList(RulesSequence.MAX_RULES);

      agent_rules_mapping = new Hashtable(RulesSequence.MAX_RULES);
      environment_rules_mapping = new Hashtable(RulesSequence.MAX_RULES);

      log_objects = new ArrayList(10);
      trades = 0;
      total_trades = 0;
      trade_prices = new Bag(1000);
      historical_agents = 0;
      active_agents = new HashSet<Agent>(); //[MAX_AGENTS];
      active_agents_count = 0;
      log_frequency = VIRT_INFINITY;

      //reusable location for placing new agents. see createAgent()
      cand = new MutableInt2D(0,0);
      terrain_files = new String[2];
      fast_draw = false;
      //load in parameters from configuration file (if any) / set default parameters
      loadParameters();
      agents_grid = new ObjectGrid2D(gridWidth, gridHeight);
      scape_grid = new ObjectGrid2D(gridWidth, gridHeight);

      /*sim.field.SparseField (superclass for agents_grid which is a SparseGrid2D)
       javadoc narrative says these two parameters trade memory for speed
      you can set both to false to optimize for speed since the number of agents is usually small 
      agents_grid.removeEmptyBags = true;
      agents_grid.replaceLargeBags = true;
       
       */

      //which classes do we want to monitor and automatically log their states/members?
      setupLogging();
      //initialize the rules for both environment and rules
      setUpEnvironmentRules();
      setUpAgentRules();
   }

   /*look for any config file parameter that starts with "log_class"
   * and use java's reflection to capture the class members.
   * Finally, add the data structures (such as agent space) for which we want
   *  to log to the logger class.  This does the actual inspection and logging.
   */
   public void setupLogging () {

      Set e = paramDB.keySet();
      Iterator i = e.iterator();

      //Enumeration e = params_hash.keys();
      //System.out.println(paramDB.toString());
      while (i.hasNext()) {
          String key = (String)i.next();
          //System.out.println(key);
          if (key.startsWith("log_class")) {
             String log_p = (String)params_hash.get(key);
             //System.out.println(key + "="+ log_p);
             try {
                 Field f = this.getClass().getDeclaredField(log_p);
                 log_objects.add(f);
             } catch (Exception e1) {
                 System.err.println("Error -- no such field as " + log_p);
             }
          } else if (key.startsWith("agent_rule")) {
              String val = (String)paramDB.get(key);
              StringTokenizer st = new StringTokenizer(val,",");
              String class_name = st.nextToken();
              String short_name = st.nextToken();
              //System.out.println(class_name + " , " + short_name+ " ***");
              agent_rules_mapping.put(short_name,"sim.app.sugarscape."+class_name);
          } else if (key.startsWith("environment_rule")) {

              String val = (String)paramDB.get(key);
              //System.out.println(val + " = val");
              StringTokenizer st = new StringTokenizer(val,",");
              String class_name = st.nextToken();
              String short_name = st.nextToken();
              //System.out.println(class_name + " , " + short_name+ " ***");
              environment_rules_mapping.put(short_name,"sim.app.sugarscape."+class_name);
          }
      }

      if (log_objects.size()>0) {
          logger = new Logger();
          for (int l=0; l < log_objects.size(); l++) {
                  logger.addObject((Field)log_objects.get(l));
          }
      }

   }

   /* primary method for creating new agents.  Invoked by various methods within Sugarscape depending
    * on whether its asexual or sexual reproduction.  Takes care of the statistical bookkeeping as well.
    */
   public Agent newAgent() {
      double met_sug = this.random.nextInt(metabolic_sugar)+1;
      double met_spi = this.random.nextInt(metabolic_spice)+1;
      int sz = agent_rules.size();
      Rule r = null;
      RulesSequence rs = new RulesSequence(sz);
      for (int a = 0; a < sz; a++) {
          try {
            Class c = Class.forName((String)agent_rules.get(a));
            r = (Rule)c.newInstance();
          } catch (Exception e) {
              System.err.println((String)agent_rules.get(a) + " " + a + " Rule not implemented");
              e.printStackTrace(System.err);
              System.exit(1);
          }
          //don't forget to tell the rule the agent to which it applies
          rs.addRule(r);
      }
      rs.setup();
      sim.app.sugarscape.Agent agent = new sim.app.sugarscape.Agent(agent_replacement, repl_min, repl_max,
              this.random.nextInt(vision_max-vision_min+1)+vision_min, (int)met_sug, (int)met_spi, culture_tags,
              (this.random.nextInt(initial_endowment_max_sugar-initial_endowment_min_sugar+1)+initial_endowment_min_sugar),
              (this.random.nextInt(initial_endowment_max_spice-initial_endowment_min_spice+1)+initial_endowment_min_spice),
              this, rs);
      for (int b = 0; b < sz; b++) {
          ((Rule)rs.rules.get(b)).setAgent(agent);
      }
      //set up agent as a stoppable so that agent can die and be removed from schedule
      Stoppable stop = schedule.scheduleRepeating(schedule.time()+1, AGENT_RULES_ORDER, agent, 1);
      agent.my_stoppable = stop;
      active_agents.add(agent);
      active_agents_count++;
      return agent;
   }

   public int randomSexGen () {
       return this.random.nextInt(2)+1;
   }


   public Agent reproduceNewAgent (Agent parent1, Agent parent2, Int2D site) {
       Agent child = null;
       //cross the parents genetic characteristics
       //child gets 1/2 of each parent's wealth
       if (debug) {
           System.out.println(parent1.age + " = age, " + parent2.age + " = age");
       }
       int parent1_contrib_sugar = Math.round(parent1.initial_endowment_sugar*.5f);
       int parent2_contrib_sugar = Math.round(parent2.initial_endowment_sugar*.5f);
       parent1.wealth_sugar = parent1.wealth_sugar - parent1_contrib_sugar;
       parent2.wealth_sugar = parent2.wealth_sugar - parent2_contrib_sugar;
       int child_endowment_sugar =  parent1_contrib_sugar + parent2_contrib_sugar;
       int parent1_contrib_spice;
       int parent2_contrib_spice;
       if (resources == 2) {
           parent1_contrib_spice = Math.round(parent1.initial_endowment_spice*.5f);
           parent2_contrib_spice = Math.round(parent2.initial_endowment_spice*.5f);
           parent1.wealth_spice = parent1.wealth_spice - parent1_contrib_spice;
           parent2.wealth_spice = parent2.wealth_spice - parent2_contrib_spice;
       } else { //don't subtract spice if only 1 resource, this would effect reproduction!!
           parent1_contrib_spice = Math.round(parent1.initial_endowment_spice*.5f);
           parent2_contrib_spice = Math.round(parent2.initial_endowment_spice*.5f);
       }
       int child_endowment_spice =  parent1_contrib_spice + parent2_contrib_spice;
       //System.out.println(child_endowment);
       //now cross the parents' vision, culture, metabolism, and fertility characteristics
       int child_vision = randomParent(parent1, parent2).vision;
       int child_metabolism_sugar = randomParent(parent1, parent2).metabolic_rate_sugar;
       int child_metabolism_spice = randomParent(parent1, parent2).metabolic_rate_spice;
       //int child_fertility_start = randomParent(parent1, parent2).fertility_start;
       //int child_fertility_end = randomParent(parent1, parent2).fertility_end;

       //sex is determined randomly in agent constructor
       //age of death is determined randomly between min_age and max_age in agent constructor
       int sz = agent_rules.size();
       Rule r = null;
       RulesSequence rs = new RulesSequence(sz);
       for (int a = 0; a < sz; a++) {
          try {
            Class c = Class.forName((String)agent_rules.get(a));
            r = (Rule)c.newInstance();

          } catch (Exception e) {
              System.err.println((String)agent_rules.get(a) + " " + a + " Rule not implemented");
              e.printStackTrace(System.err);
              System.exit(1);
          }
            //don't forget to tell the rule the agent to which it applies
          rs.addRule(r);
       }
       rs.setup();
       child = new Agent(agent_replacement, repl_min, repl_max, child_vision, child_metabolism_sugar, child_metabolism_spice,
                          culture_tags, child_endowment_sugar, child_endowment_spice, this,rs);
       for (int b = 0; b < sz; b++) {
          ((Rule)rs.rules.get(b)).setAgent(child);
       }
       boolean[] cult1 = parent1.culture.tagset;
       //boolean[] cult2 = parent2.culture.tagset;
       int size = cult1.length;
       boolean[] child_cult = child.culture.tagset;
       for (int a=0;a<size;a++) {
          child_cult[a] = randomParent(parent1,parent2).culture.tagset[a];
       }
       //System.out.println(child.wealth_sugar +" " + child.wealth_spice);
       //System.out.println("A child is born.");
       agents_grid.field[site.x][site.y] = child; // agents_grid.setObjectLocation(child, site);
       child.my_loc.x = site.x;
       child.my_loc.y = site.y;
       Stoppable stop = this.schedule.scheduleRepeating(schedule.time()+1, AGENT_RULES_ORDER, child, 1);
       child.my_stoppable = stop;
       active_agents_count++;
       historical_agents++;
       return child;
   }
   private Agent randomParent (Agent parent1, Agent parent2) {
       if (this.random.nextInt(2)==1) {
           return parent1;
       }
       return parent2;
   }

   //no longer used
   public void setDefaultParameters() {

   }

   public void setUpEnvironmentRules () {
      StringTokenizer st = new StringTokenizer(rules_seq_environment,",");
      while (st.hasMoreTokens()) {
          String rule = st.nextToken();
          String class_name = (String)environment_rules_mapping.get(rule);
          environment_rules.add(class_name);
      }
   }

   public void setUpAgentRules () {
      StringTokenizer st = new StringTokenizer(rules_seq_agent,",");
      while (st.hasMoreTokens()) {
          String rule = st.nextToken();
          String class_name = (String)agent_rules_mapping.get(rule);
          agent_rules.add(class_name);
          //System.out.println(rule + " added to agent_rules");
      }
   }

    public void dynamicNewAgent() {
       Agent agent = this.newAgent();
       //schedule.scheduleRepeating(schedule.time()+1, AGENT_RULES_ORDER, agent, 1);
       boolean occupied = true;
       while (occupied) {
          cand.setTo(random.nextInt(gridWidth),random.nextInt(gridHeight));
          if (agents_grid.field[cand.x][cand.y]==null) {
              occupied = false;
          }
       }
       agents_grid.field[cand.x][cand.y]=agent; //setObjectLocation(agent,cand.x, cand.y);
       agent.my_loc.setTo(cand.x,cand.y);
       incAgentCount();
   }

   public void createNewAgent() {
       Agent agent = this.newAgent();
       boolean occupied = true;
       while (occupied) {
          cand.setTo(random.nextInt(gridWidth),random.nextInt(gridHeight));
          if (agents_grid.field[cand.x][cand.y]==null) {
              occupied = false;
          }
       }
       agents_grid.field[cand.x][cand.y] = agent; //setObjectLocation(agent, cand.x, cand.y);
       agent.my_loc.setTo(cand.x,cand.y);
   }

   public void start() {
      super.start();
      agents_grid.clear();
      scape_grid.clear();
      //scape_grid = new ObjectGrid2D(gridWidth, gridHeight);
      sim.app.sugarscape.Agent agent;
      /* If the season rate is very large, it will be summer for all foreseable simulation runs in the north and winter
      in the south. Growback rates for each are set as parameters and normally default to 1 */
      north_season = SUMMER;
      south_season = WINTER;
      Steppable season_changer = new Steppable () {
          public void step(SimState state) {
              Sugarscape sugar = (Sugarscape)state;
              if ( sugar.north_season == SUMMER ) {
                  sugar.north_season = WINTER;
                  sugar.south_season = SUMMER;
              } else {
                  sugar.north_season = SUMMER;
                  sugar.south_season = WINTER;
              }
          }
      };

      MultiStep season_multi = new MultiStep(season_changer, season_rate, true);
      schedule.scheduleRepeating(Schedule.EPOCH, 3, season_multi, 1); //change seasons last in order

      if (agents_file!=null) { //config file for setting up agents spatial config
        try {
               File f = new File(agents_file);//AGENTS_PARAMS]);
               FileReader fr = new FileReader(f);
               BufferedReader br = new BufferedReader(fr);
               String line = br.readLine();
               int y = 0;
               int affiliation;
               String affil_code;
               while (line !=null) {
                   int x = 0;
                   int size = line.length();
                   while (x < size) {
                       affil_code = line.substring(x,x+1);
                       affiliation = Integer.parseInt(affil_code);
                       if (affiliation !=0) {
                           agent = this.newAgent();
                           agents_grid.field[x][y]= agent; //,new Int2D(x,y));
                           agent.my_loc.x = x;
                           agent.my_loc.y = y;
                       }
                       x++;
                   }
                   line = br.readLine();
                   y++;
                   //System.out.println(line);
               }
             } catch (Exception e) {
                 e.printStackTrace();
             }
      } else { //otherwise distribute randomly in space
           for (int a=0; a < numAgents; a++) {
               createNewAgent();
           }
      }

      ArrayList rows = new ArrayList(50);
      for (int a = 0; a < resources; a++) {
          rows.clear();
          try {
                   File f = new File(terrain_files[a]);//TERRAIN]);
                   FileReader fr = new FileReader(f);
                   BufferedReader br = new BufferedReader(fr);
                   String line = br.readLine();
                   while (line !=null) {
                       rows.add(line);
                       line = br.readLine();
                   }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
          int row_count = rows.size();
          int capacity;
          String max;
          int equator = row_count/2;
          int hemisphere = NORTH;
          Object[][] all_objs = scape_grid.field;
          //Object[][] all_pollut = pollution_grid.field;
          for (int y = 0; y < gridHeight; y++) {
              if (y >= equator) {
                hemisphere = SOUTH;
              }
              StringBuffer sb = new StringBuffer((String)rows.get(y));
              int type;
              for (int x = 0; x < gridWidth; x++) {
                 max = sb.substring(x,x+1);
                 capacity = Integer.parseInt(max);
                 if (capacity>4) {  //terrain file has values of 0-4, and 0,5-8
                     type = SPICE;
                     capacity = capacity - 4;
                 } else {
                     type = SUGAR;
                 }
                 if (all_objs[x][y]==null) {
                    Scape site = new Scape(x, y, hemisphere, pollution_diffusion, this);
                    site.setResource(type, capacity,1,capacity); /* idealy the growback rate, third param, should not be constant */
                    all_objs[x][y] = site;
                 } else {
                    ((Scape)all_objs[x][y]).setResource(type, capacity,1,capacity);
                 }
              }
          }
      }
      this.initializeEnvironment();

      //set up charts
      avg_agent_vision.clear();
      lorenz_curve.clear();
      gini_coeff.clear();
      agents_series.clear();
      blue_agents.clear();
      red_agents.clear();
      trade_series.clear();

      if (stats_rate>0) {
          Statistics stats = new Statistics(this, schedule, stats_out);
          MultiStep multi_lorenz = new MultiStep(stats, stats_rate, true);
          schedule.scheduleRepeating(Schedule.EPOCH, 4, multi_lorenz, 1);
          stats.step(this);
      }
       //calculate and output frequency count for metabolic 'bins'
       //that is the total metabolism for each each across the population
       Steppable metabolism_bins_updater = new Steppable() {
         Histogram h = new Histogram("Metabolism","Vision",1+metabolic_sugar + metabolic_spice, 5);
         public void step (SimState state) {
        	 //agents_grid.
        	 
             Bag all = null; //agents_grid.getAllObjects();
             int size = all.size();
             int total = 0;
             Object[] all_obj = all.objs;
             h.zero();
             for (int a=0; a < size; a++) {
                   Agent ag = (Agent)all_obj[a];
                   total = ag.metabolic_rate_spice + ag.metabolic_rate_sugar;
                   h.bins[total] = h.bins[total] + 1;
                   h.bins_assoc[total] = h.bins_assoc[total] + ag.vision;
             }
             h.render();
             System.out.println(size + " = size, " + (state.schedule.time() + 1) + " = steps.");

         }
      };


      if ( print_metabolism_bins) {
          MultiStep multi_metabolism_bins = new MultiStep(metabolism_bins_updater,metabolism_bins_freq,true);
          schedule.scheduleRepeating(Schedule.EPOCH, 6, multi_metabolism_bins,1);
          multi_metabolism_bins.step(this);
      }

      if (logger!=null) {
          MultiStep multi_logger = new MultiStep(logger, log_frequency, true);
          //use order 4 along with statistics steppable
          //System.out.println("Logging "+ logger.grids.size() + " grids.");
          schedule.scheduleRepeating(Schedule.EPOCH, 4, multi_logger,1);
          Thread t = new Thread(logger);
          t.start();
      }
      
       Steppable chart_updater = new Steppable() {
            private static final int BINS = 10;
            private float total;
            private float wealth_sugar_total;
            private double[] ages;
            

            public void step(SimState state) {

                Bag all = null; //agents_grid.getAllObjects();
                int size = active_agents_count;
                double[] values = null;
                if (size!=0) { /* what if there are zero agents? */
                    values = new double[size];
                } else {
                    values = new double[1];
                    values[0] = 0;
                }
                total = 0;
                wealth_sugar_total = 0;
                if (age_chart_on) {
                    ages = new double[size];
                }
                Iterator<Agent> i = active_agents.iterator();
                int a = 0;
                while (i.hasNext()) {
                    Agent agent = i.next();
                    values[a] = agent.wealth_sugar;
                    wealth_sugar_total = wealth_sugar_total + agent.wealth_sugar;
                    if (age_chart_on) {
                        ages[a] = agent.age;
                    }
                    a++;
                }

                if (age_chart_on) {
                    age_hist_dataset = new HistogramDataset();
                    age_hist_dataset.addSeries(Double.toString(state.schedule.time()), ages, 10);
                    XYPlot xy = ((Sugarscape)state).age_histo_chart.getXYPlot();
                    xy.setDataset(age_hist_dataset);
                }
                               
                if (wealth_chart_on) {
                	dataset = new HistogramDataset();
                    dataset.addSeries(Double.toString(state.schedule.time()), values, 10);
                    XYPlot xy = ((Sugarscape)state).chart4.getXYPlot();
                    xy.setDataset(dataset);
                    XYItemRenderer r = xy.getRenderer();
                    XYItemLabelGenerator xylg = new StandardXYItemLabelGenerator(
                            "{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
                    r.setItemLabelGenerator(xylg);
                    r.setItemLabelsVisible(true);
                }
            }
        };
        // Schedule the agent to update the chart
        if (chart_display) {
          MultiStep multi_chart = new MultiStep(chart_updater,chart_rate,true);
          schedule.scheduleRepeating(Schedule.EPOCH, 6, multi_chart,1);
          chart_updater.step(this);
      }
   }
    //1=LL, 2=UL, 3=UR, 4=LR
    public void initializeEnvironment() {
      int sz = environment_rules.size();
      EnvironmentRule r = null;
      RulesSequence rs = new RulesSequence(sz);
      for (int a = 0; a < sz; a++) {
          try {
            Class c = Class.forName((String)environment_rules.get(a));
            r = (EnvironmentRule)c.newInstance();
            r.setEnvironment(this.scape_grid);
          } catch (Exception e) {
              System.out.println((String)environment_rules.get(a) + " " + a + " Rule not implemented");
              e.printStackTrace();
              System.exit(1);
          }
            //don't forget to tell the rule the agent to which it applies
          rs.addRule(r);
      }
      rs.setup();
      Environment env = new Environment(this, rs);
      this.schedule.scheduleRepeating(Schedule.EPOCH,ENVIRONMENT_RULES_ORDER,env,1);
    }

    public void incAgentCount () {
        historical_agents = historical_agents + 1;
    }

    public float[] genStatistics () {
        float [] stats = new float[2];
        
        return stats;
    }

    public void setNorthSeason(int season) {
        north_season = season;
    }
    public int getSeasonRate(int hemisphere) {
        if (hemisphere==NORTH) {
            if (north_season==SUMMER) {
               //System.out.print("NS" + summer_grow + " ");
               return summer_grow;
            } else
               //System.out.print("NW"+ winter_grow + " ");
               return winter_grow;

            }
        else {
            if (south_season==SUMMER) {
               //System.out.print("SS"+ summer_grow + " ");
               return summer_grow;
            } else
               //System.out.print("SW"+ winter_grow + " ");
               return winter_grow;
        }
    }

    public void stopLogging () {
        if (logger!=null) {
            logger.done();
        }
    }

    public static void main(String[] args)
            {
            Sugarscape sugar = null;
            int loops = 1;
            String config = "";
            int steps = 5000;
            ParameterDatabase paramsdb = null;
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
                } else if (args[x].equals("-config")) {
                    config = args[x+1];
                } else if (args[x].equals("-steps")) {
                    steps = Integer.parseInt(args[x+1]);
                } else if (args[x].equals("-file"))
                        {
                        try
                            {
                            paramsdb=new ParameterDatabase(
                                    new File(args[x+1]).getAbsoluteFile(),
                                    args);
                            }catch(IOException ex){ex.printStackTrace();}
                        break;
                        }


        try {
        for (int a = 0; a < loops; a++) {
            System.out.println("loop "+a);
            Thread.sleep(500);
            if (sugar==null)
                {
                sugar = new Sugarscape(paramsdb, System.currentTimeMillis(),1);

                sugar.start();
                }
            double time;
            while((time = sugar.schedule.time()) < steps)
                {
                //System.out.println("I checked.");
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