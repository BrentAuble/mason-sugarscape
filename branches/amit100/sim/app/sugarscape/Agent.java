package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Int2D;
import sim.util.Bag;
import sim.util.IntBag;
import sim.util.MutableInt2D;
import sim.field.grid.ObjectGrid2D;

/**
 * @author tony.bigbee@gmail.com
 *
 * This is the primary agent class.  It contains state variables and various methods
 * for effecting state, such as dying, generating pollution, looking for a new site to occupy.
 * Most methods are only invoked by some rule in an instance of RulesSequence--which
 * is the primary mechanism for rules and behavior.
 */

public class Agent implements Steppable {

	  private static final long serialVersionUID = 6240949754135431600L;
	
	  /* Genetic and birth characteristics*/ 
      public int vision;
      public int metabolic_rate_sugar;
      public int metabolic_rate_spice;
      public int sex;
      public int fertility_start;
      public int fertility_end;
      public int initial_endowment_sugar, initial_endowment_spice;
      public static boolean replacement;
      public int repl_min, repl_max, max_age;
      public int repro_min;
      
      /* Trade variables*/
      public int my_trade_count;
      public int neighbors_for_trades;
      public int best_locations;
      public int vision_trade_neighbors;

      /* Current state*/
      public MutableInt2D my_loc;
      public float wealth_sugar, wealth_spice;
      public boolean alive;
      public int age;
      public int affiliation;
      public float generated_pollution;

      public Culture culture;
      public WelfareEstimation welfare;
      
      /* Diagnostic*/
      public boolean marked;
      public boolean diag;
      public int children;

      /* Space, housekeeping, and schedule*/
      public IntBag site_x;
      public IntBag site_y;
      public Int2D[] site_xy;
      public MutableInt2D[] site_xy_m;
      public MutableInt2D target;

      public Stoppable my_stoppable;
      public int total_vision_sites;
      public Bag vision_sites;
      public Sugarscape model;
      public ObjectGrid2D all_agents;
     
      /* Vision and movement*/
      public static final int SCAN_PATTERNS = 24;
      public static final int NEIGHBORS = 4;  //4-way neighborhood model
      public static final int NORTH = 0;
      public static final int EAST = 1;
      public static final int SOUTH = 2;
      public static final int WEST = 3;

      
      /* Trade variables */
      public boolean trade_partner; //diagnostic:  was I a trade partner last step?
      public boolean trading_neighbor;
      public Bag trading_partners;
      public Bag trading_neighbors;
      public double mrs;
      public double tau_1, tau_2;
      public RulesSequence rules_seq;
      public Bag repro_neighbors;
      public Bag neighbors;
      public Bag free_locations;
      public boolean neighbor1, neighbor2, neighbor3, neighbor4;
      
      /* to deal with floating point rounding error */ 
      public static final double MRS_TOLERANCE = .001;
      
      public Agent (boolean replace, int min, int max,  int vision, int metab_sugar, int metab_spice, int tagsize, int initial_endow_sugar, int initial_endow_spice, Sugarscape sugar, RulesSequence rules_s) {
        //The main model to access environment and enable other key interactions
    	model = sugar;
        all_agents= model.agents_grid;
        //not marked by user and no diagnostics
        marked = false;
        diag = false;       
        sex = sugar.randomSexGen();  //legal values are 1=MALE, 2=FEMALE
        replacement = replace;
        wealth_sugar = initial_endow_sugar;
        wealth_spice = initial_endow_spice;//sugar.random.nextInt(25)+1;
        initial_endowment_sugar = initial_endow_sugar;
        if (model.reproduction_min <= 0) {
            repro_min = initial_endowment_sugar;
        } else {
            repro_min = model.reproduction_min;
          }
        initial_endowment_spice = initial_endow_spice;
        this.vision = vision;
        this.metabolic_rate_sugar = metab_sugar;
        generated_pollution = 0;
        if (sugar.resources ==1) {
            this.metabolic_rate_spice = 0;
        } else {
            this.metabolic_rate_spice = metab_spice;
        }
        alive = true;
        age = 0;
        children = 0;
        repl_min = min;
        repl_max = max;
        
        /* GAS pp. 32-33 
         */
        max_age = sugar.random.nextInt(repl_max-repl_min)+repl_min;
        
        repro_neighbors = new Bag(NEIGHBORS);
        neighbors = new Bag(NEIGHBORS);
        free_locations = new Bag( (NEIGHBORS-1)*2);
        
        /*randomly assign culture tags
        if reproduction is on, children will have tags crossed from
        parents by the Sugarscape.reproduceNewAgent method.
        when fertility conditions are right, this method is eventually called
        to complete reproduction.
        may need to move culture seeding to Sugarscape class
        to enable parent crossover
        */
        boolean[] tagset = new boolean[tagsize];
        
        for (int a = 0; a<tagsize; a++) { 
               tagset[a] = false;
        }
        culture = new Culture(tagset);
        //must instantiate welfare *after* culture
        welfare = new WelfareEstimation(sugar, vision, this);
        total_vision_sites = vision*4;  //4-way neighborhood
        site_x = new IntBag(4);
        site_y = new IntBag(4);
        vision_sites = new Bag(4);
        site_xy = new Int2D[total_vision_sites];
        site_xy_m = new MutableInt2D[total_vision_sites];
        //constructor does not set initial location, creator must do this!
        my_loc = new MutableInt2D();
        for (int a = 0; a < total_vision_sites; a++ ) {
            site_xy_m[a] = new MutableInt2D(0,0);
        }
        target = new MutableInt2D(); //reused in findBestSite...
        if (sex == Sugarscape.MALE) {
           fertility_start = sugar.random.nextInt(sugar.male_fertility_start_max-sugar.male_fertility_start_min+1)+sugar.male_fertility_start_min;
           fertility_end = sugar.random.nextInt(sugar.male_fertility_end_max-sugar.male_fertility_end_min+1)+sugar.male_fertility_end_min;
        } else {
           fertility_start = sugar.random.nextInt(sugar.female_fertility_start_max-sugar.female_fertility_start_min+1)+sugar.female_fertility_start_min;
           fertility_end = sugar.random.nextInt(sugar.female_fertility_end_max-sugar.female_fertility_end_min+1)+sugar.female_fertility_end_min;
        }
        if (sugar.debug) {
        	System.out.println(fertility_start +  " fs " + fertility_end + " fe");
        }
        my_trade_count = 0;
        neighbors_for_trades = 0;
        trading_partners = new Bag(4);
        trading_neighbors = new Bag(4);
        trade_partner = false;//4 = max number of trading partners
        trading_neighbor = false;
        rules_seq = rules_s;
      }
      
      /* Randomly assign tags to each culture bin
       */     
      public void initCulture() {
    	  int tagsize = culture.tagset.length;
    	  for (int a = 0; a<tagsize; a++) {
	    	  if (model.random.nextInt(2) == 1) {
	              culture.tagset[a] = true;
	          } else culture.tagset[a] = false; 
    	  }
      }
      
     /*************************************************************************
      * Inspector methods
      ************************************************************************/
      public boolean getAlive() {
          return alive;
      }
      
      /*how many times last step did I trade with neighbors? */
      public int getTradeCount() {
          return my_trade_count;
      }

      public int getNeighborsForTrades () {
          return neighbors_for_trades;
      }
      
      /**This method **MUST** match resolveColorAffil()
       *this will output string equivalents of the Paint values
       *returned by resolveColorAffil().
       *
       *To do:  These two methods should be merged.
       **/
      public String getAffil() {
          if (!alive) {
              return "GRAY";
          }
          if (culture.getAffiliation()==0) {
              return "RED";
          }
          else return "BLUE";
      }
      
      /*
       * The rendered color depends on affiliation and aliveness.
       * dead = LIGHT_GRAY
       * if cultural affiliation=0, RED
       * else BLUE
       */
      public java.awt.Paint resolveColorAffil() {
         if (!alive) {
            return java.awt.Color.LIGHT_GRAY;
        }
         
        if (culture.getAffiliation()==0) {
           return java.awt.Color.red;
        }
        else return java.awt.Color.blue;
      }
      
      public int getChildren() {
          return children;
      }

      public int getSex() {
          return sex;
      }

      public int getVision() {
          return vision;
      }

      public int getMetabolicRateSugar() {
          return metabolic_rate_sugar;
      }

      public int getMetabolicRateSpice() {
          return metabolic_rate_spice;
      }

      public float getWealthSugar() {
          return wealth_sugar;
      }
      public float getWealthSpice() {
          return wealth_spice;
      }

      public int getAge() {
          return age;
      }
      public double getFractionZeros() {
          return culture.fractionZeros();
      }
      public double getWelfareSugarExp() {
          return welfare.welfare_sugar_exp;
      }

      public double getWelfareSpiceExp() {
          return welfare.welfare_spice_exp;
      }
    
      public int getBestLocation () {
          return best_locations;
      }
    
      public boolean getMarked() {
          return marked;
      }

      public void setMarked (boolean mark) {
          marked = mark;
      }

      public void setDiag (boolean d) {
          diag = d;
      }

      public boolean getDiag () {
          return diag;
      }

      public boolean getTradePartner() {
          return trade_partner;
      }    

      /*
       * This simply implements metabolic consumption per GAS p.
       */
      public void metabolize () {
        wealth_sugar  = wealth_sugar - metabolic_rate_sugar;
         if (model.resources == 2) {
         wealth_spice  = wealth_spice - metabolic_rate_spice;
         }
      }
      
      /*
       * This is the standard way to have an agent perform something in MASON Schedule.
       * But, rather than have a bunch of methods and code in here, we rely on a RulesSequence class to 
       * fire the appropriate rules per the configuration file.  This allows us to make
       * decisions at run time rather than hard-coding things.
       * @see sim.engine.Steppable#step(sim.engine.SimState)
       */
      public void step (SimState state) {
        if (alive) {
            rules_seq.step(state); //execute all agent rules
        }
      }
      /*
       * Sexual reproduction.
       */
      public void reproduce() {
            /* Only reproduce if this parameter is on and
             * agent is of reproductive age */
            if ( (age > fertility_end) || (age < fertility_start) || (wealth_sugar < repro_min) )
                return;
            Bag neighbors = idNeighbors();
            int size = neighbors.size();
            if (diag) {
                System.out.println(size + " neighbors");
            }
            /*to mate, must be fertile, neighbor must be opposite sex, and neighbor must be fertile , and must have enough wealth to do so*/
            for (int nn=0; nn<size; nn++) {
            	if (wealth_sugar < repro_min) 
                	return;   
                Agent neighbor = (Agent)neighbors.get(nn);                      
                if ( (neighbor.sex!=this.sex)
                     && (neighbor.age <= neighbor.fertility_end) && (neighbor.age >= neighbor.fertility_start )
                     && (neighbor.wealth_sugar >=neighbor.initial_endowment_sugar)
                     && (wealth_spice >= initial_endowment_spice) && (neighbor.wealth_spice>=neighbor.initial_endowment_spice)) {
                      //randomly select a free space to put the child
                     Int2D open_site = this.idFreeSpace(my_loc.x,  my_loc.y);
                     if (open_site == null) { //any open sites for child around partners?
                         //System.out.println("no open site for " + this + " " + model.schedule.time());
                         continue;
                     }
                     //spawn a child via crossed genetic features, appropriate endowment
                     //reduce the endowment given to the child from the parents
                     //put the child into the right space and add to the schedule
                     //check to see that the space is legal...
                     model.reproduceNewAgent(this, neighbor, open_site);
                     children++;
                     neighbor.children++;
                 } else {
                    if (diag) {
                        System.out.println(neighbor.sex + ":" + this.sex);
                        System.out.println(wealth_sugar + ":" + initial_endowment_sugar);
                        System.out.println(wealth_spice + ":" + initial_endowment_spice);
                        System.out.println(neighbor.wealth_sugar+":"+neighbor.initial_endowment_sugar);
                    }
                }
             }
      }
      
      /*Find adjacent neighbors and return in random order in a Bag
      */
      private Bag idNeighbors() {
          neighbors.clear();
          int index = model.random.nextInt(SCAN_PATTERNS);
            if (model.toroidal) {
               for (int a=0; a < 4; a++) {
                  int x = model.agents_grid.stx(WelfareEstimation.scan_deltas[WelfareEstimation.scan[index][a]][0]+my_loc.x);
                  int y = model.agents_grid.sty(WelfareEstimation.scan_deltas[WelfareEstimation.scan[index][a]][1]+my_loc.y);
                  Agent neighbor =  (Agent)model.agents_grid.field[x][y]; //getObjectsAtLocation(x,y);
                  if (neighbor!=null) {
                        neighbors.add(neighbor);
                    }
                }
            } else { //need to implement non-toroidal neighbor search

            }
            return neighbors;
      }

      /*return first free location found, random scan of neighboring locations.
       */
      private Int2D idFreeSpace(int x, int y) {
           //SparseGrid2D all_agents = model.agents_grid;
           free_locations.clear();
           int index = model.random.nextInt(SCAN_PATTERNS);
           if (model.toroidal) {
                for (int a=0; a < 4; a++) {
                    if (retrieveNeighborToroidal(a,x,y)) {
                        int x_n = model.agents_grid.stx(WelfareEstimation.scan_deltas[WelfareEstimation.scan[index][a]][0]+x);
                        int y_n = model.agents_grid.sty(WelfareEstimation.scan_deltas[WelfareEstimation.scan[index][a]][1]+y);
                        return new Int2D(x_n,y_n);
                    }
                }
           } else {
                for (int a=0; a < 4; a++) {
                    if (retrieveNeighborSquare(a,x,y)) {
                        return new Int2D(WelfareEstimation.scan_deltas[WelfareEstimation.scan[index][a]][0]+x,
                                         WelfareEstimation.scan_deltas[WelfareEstimation.scan[index][a]][1]+y);
                    }
                }
           }
           return null;
      }

      /*Look in one direction for a specified distance.  Return true if occupied.
       *Space is toroidal.
       */
      private boolean retrieveNeighborToroidal(int direction, int x, int y) {
           if (model.agents_grid.field
                  [model.agents_grid.stx(WelfareEstimation.scan_deltas[direction][0]+x)]
                  [model.agents_grid.sty(WelfareEstimation.scan_deltas[direction][1]+y)]==null)
              return false; //nobody here
           return true; //occupied
      }

      /*Look in one direction for a specified distance.  Return true if occupied.
       *Space is square.
       */
      private boolean retrieveNeighborSquare(int direction, int x, int y) {
          if (model.agents_grid.field
                  [WelfareEstimation.scan_deltas[direction][0]+x]
                  [WelfareEstimation.scan_deltas[direction][1]+y]==null)
              return false; //nobody here
          return true;
      }

      /*Add pollution from my activities to cell being occupied.
       */
      public void doPollution () {
            Scape s = (Scape) model.scape_grid.field[my_loc.x][my_loc.y];
            s.pollution = s.pollution + generated_pollution;
      }

      /*Calculate MRS GAS p. 
       * 
       */
      public double computeMRS () {
          return (wealth_spice/metabolic_rate_spice)/(wealth_sugar/metabolic_rate_sugar);
      }


      /******************************************************
       * Agent Trade rule T (GAS p. 105)
       * A very complicated rule!
       ******************************************************
       */
      public void trade(Sugarscape sugar) {
          // randomly loop through each neighbor
          //  loop until MRSs are approximately equal
          double mrs_diff;
          double my_mrs;
          double neighbor_mrs;
          double my_mrs_new;
          double neighbor_mrs_new;         
          double my_welfare_sugar;
          double my_welfare_spice;
          double neigh_welfare_sugar;
          double neigh_welfare_spice;
          double my_welfare_sugar_new;
          double my_welfare_spice_new;
          double neigh_welfare_sugar_new;
          double neigh_welfare_spice_new;
          double my_new_welfare_total;
          double neigh_new_welfare_total;
          int my_sugar_delta;
          int my_spice_delta;
          int neigh_sugar_delta;
          int neigh_spice_delta;
          int my_sugar_direct;
          int my_spice_direct;
          boolean direct;        
          Agent neighbor;        
          int neigh_x;
          int neigh_y;
          ObjectGrid2D agents = sugar.agents_grid;
          int order = sugar.random.nextInt(24);
          //int count =0;
          if (diag) {
              System.out.println(this);
          }
          if (trading_neighbors.size()!=0) {
              int tn_size = trading_neighbors.size();
              for (int c =0; c < tn_size;c++) {
                Agent tp = (Agent)trading_neighbors.objs[c];
                tp.trading_neighbor = false;
              }
              trading_neighbors.clear();
          }

          if (trading_partners.size()!=0) {
              int t_size = trading_partners.size();
              for (int b =0; b < t_size;b++) {
                Agent tp = (Agent)trading_partners.objs[b];
                tp.trade_partner = false;
              }
              trading_partners.clear();
          }

          for (int a = 0; a< 4; a++) {
              neighbor = null;
              neigh_x = WelfareEstimation.scan_deltas[WelfareEstimation.scan[order][a]][0];
              neigh_y = WelfareEstimation.scan_deltas[WelfareEstimation.scan[order][a]][1];
              neighbor = (Agent)agents.field[agents.stx(my_loc.x+neigh_x)][agents.sty(my_loc.y+neigh_y)];
              //if no neighbor at this 4-way location, proceed to next potential neighbor
              if (neighbor==null) {
                 continue;
              }
              sugar.neighbor_occurrence++;
              neighbors_for_trades++;
              Agent ag = neighbor;
              if (marked) {
                trading_neighbors.add(ag);
                ag.trading_neighbor = true;
              }
              my_mrs = (wealth_spice/(double)metabolic_rate_spice)/(wealth_sugar/(double)metabolic_rate_sugar);
              neighbor_mrs = (ag.wealth_spice/(double)ag.metabolic_rate_spice)/(ag.wealth_sugar/(double)ag.metabolic_rate_sugar);
              mrs_diff = my_mrs - neighbor_mrs;
              if (diag) {
                  System.out.println(my_mrs-neighbor_mrs + " = mrs_diff" + " " + ag);
              }
              //System.out.print(">"+mrs_diff + " ");
              while (mrs_diff!=0) {//(StrictMath.abs(mrs_diff) > MRS_TOLERANCE) {
                     //try the trade iff it will make everyone better off
                      double price = Math.sqrt(my_mrs*neighbor_mrs);
                      if ((my_mrs < 0) || (neighbor_mrs < 0)) {
                          System.out.print(">>> ");
                          System.out.print(wealth_sugar + " " + metabolic_rate_sugar);
                          System.out.print(" "+ag.wealth_spice + " " + ag.metabolic_rate_spice);
                          System.out.print(" "+ ag.wealth_sugar + " " + ag.metabolic_rate_sugar);
                          System.out.println(" <<<");
                      }
                      //double delta = my_mrs - neighbor_mrs;
                      //if my_MRS > neighbor_MRS, I barter sugar for spice from neighbor
                      if (mrs_diff < 0) {
                          my_sugar_direct = -1;
                          my_spice_direct = 1;
                          direct = false;
                      } else { //which way should barter go
                          my_sugar_direct = 1;
                          my_spice_direct = -1;
                          direct = true;
                      }
                         if (price > 1) {
                           my_sugar_delta = my_sugar_direct*1;
                           my_spice_delta = my_spice_direct*(int)Math.round(price);
                           neigh_sugar_delta = my_sugar_delta*-1;
                           neigh_spice_delta = my_spice_delta*-1;

                         } else { //I buy barter spice for sugar from neighbor
                           my_spice_delta = my_spice_direct*1;
                           my_sugar_delta = my_sugar_direct*(int)Math.round(1/price);
                           neigh_sugar_delta = my_sugar_delta*-1;
                           neigh_spice_delta = my_spice_delta*-1;
                         }
                         /* calculate theoretical sugar and spice deltas for each
                            and hypothetical new MRSs
                            do trade (and set mrs_diff = 0 to pop out of while loop)
                            iff:  welfare increases for each agent *and* MRSs do not 'switch'
                          */
                          neigh_welfare_sugar = StrictMath.pow(ag.wealth_sugar, ag.welfare.welfare_sugar_exp);
                          neigh_welfare_spice = StrictMath.pow(ag.wealth_spice, ag.welfare.welfare_spice_exp);

                          my_welfare_sugar = StrictMath.pow(wealth_sugar, welfare.welfare_sugar_exp);
                          my_welfare_spice = StrictMath.pow(wealth_spice, welfare.welfare_spice_exp);

                          my_welfare_sugar_new = StrictMath.pow(wealth_sugar+my_sugar_delta, welfare.welfare_sugar_exp);
                          my_welfare_spice_new = StrictMath.pow(wealth_spice+my_spice_delta, welfare.welfare_spice_exp);

                          neigh_welfare_sugar_new = StrictMath.pow(ag.wealth_sugar+neigh_sugar_delta, ag.welfare.welfare_sugar_exp);
                          neigh_welfare_spice_new = StrictMath.pow(ag.wealth_spice+neigh_spice_delta, ag.welfare.welfare_spice_exp);

                          my_new_welfare_total = my_welfare_sugar_new*my_welfare_spice_new;
                          neigh_new_welfare_total = neigh_welfare_sugar_new*neigh_welfare_spice_new;
                          if (diag) {
                              System.out.println(my_new_welfare_total + " " + (my_welfare_sugar*my_welfare_spice));
                              System.out.println(neigh_new_welfare_total + " " + (neigh_welfare_sugar*neigh_welfare_spice));
                          }
                          if ( (my_new_welfare_total > (my_welfare_sugar*my_welfare_spice)) && (neigh_new_welfare_total>(neigh_welfare_sugar*neigh_welfare_spice)) ) {
                             //calculate new MRS for each make sure there isn't a 'flip'
                              my_mrs_new =  ((wealth_spice+my_spice_delta)/(double)metabolic_rate_spice) / ((wealth_sugar+my_sugar_delta)/((double)metabolic_rate_sugar));
                              neighbor_mrs_new = ((ag.wealth_spice+neigh_spice_delta)/(double)ag.metabolic_rate_spice)/((ag.wealth_sugar+neigh_sugar_delta)/(double)ag.metabolic_rate_sugar);
                              double mrs_new_diff = my_mrs_new - neighbor_mrs_new;
                              //do the trade?
                              if ( ((direct) && (mrs_new_diff > 0)) || ((!direct) && (mrs_new_diff < 0)) ) {
                                  //System.out.println(this + " traded");
                                  wealth_sugar = wealth_sugar + my_sugar_delta;
                                  wealth_spice = wealth_spice  + my_spice_delta;
                                  if (diag) {
                                      System.out.println(my_sugar_delta + " sugar received, " + my_spice_delta + " spice received");
                                      System.out.print(ag.wealth_sugar + " = neighbor sugar before, " + ag.wealth_spice + " = neighbor spice before, ");
                                  }
                                  if (marked) {
                                      if (!trading_partners.contains(ag)) {
                                          trading_partners.add(ag);
                                          ag.trade_partner = true;
                                      }
                                  }
                                  ag.wealth_sugar = ag.wealth_sugar + neigh_sugar_delta;
                                  ag.wealth_spice = ag.wealth_spice + neigh_spice_delta;
                                  if (diag) {
                                      System.out.println(ag.wealth_sugar + " = neigh sugar after, " + ag.wealth_spice + " = wealth spice after.");
                                  }
                                  if ( (wealth_sugar < 0) || (wealth_spice < 0) || (ag.wealth_spice <0) || (ag.wealth_sugar <0)) {
                                      System.out.println(" < 0 in the trade");
                                  }
                                  mrs_diff = mrs_new_diff;
                                  my_mrs = my_mrs_new;
                                  neighbor_mrs = neighbor_mrs_new;
                                  sugar.trades++;
                                  my_trade_count++;
                                  sugar.trade_prices.add(new Double(price));
                                  /*The if block below should never be triggered. If so, some kind of bug exists */
                                  if (Double.isInfinite(price)) {
                                      System.out.println("Price is infinite! Details:");
                                      System.out.println(">"+my_mrs + ","+ neighbor_mrs + " "+ neigh_spice_delta + " " + ag.wealth_spice +  " price is infinite.");
                                      System.out.println(ag.wealth_sugar + " "+ neigh_sugar_delta);
                                      System.out.println(direct);
                                      System.out.println(my_sugar_delta + " " + my_spice_delta);
                                      System.out.println(my_sugar_direct + " " + my_spice_direct);
                                  }
                              } else { //we're all done trading
                                  mrs_diff = 0; 
                              }
                          } else {
                              mrs_diff = 0; //we're all done trading
                          }
              }
          }
      }

     /*
      * Compute the welfare change for unoccupied sites within vision
      * and return the site providing the most increase, possibly current
      * location.
      */
     public Scape findBestUnoccupiedSite() {
         best_locations = 0;  //for diagnostic purposes only
         welfare.estimate(my_loc.x,my_loc.y);
         return welfare.best_r.s;
     }
      /*
       * Die by removing self from the environment space and from the schedule.  This is the ****only****
       * place where global data structures are modified in terms of removal/deletion.
       */
     public void die(Sugarscape sugar) {
        alive = false;
        sugar.agents_grid.field[my_loc.x][my_loc.y]=null;
        /* take care of statistical bookkeeping 
         * TO DO:  Create a birth_and_death class and ask the model class
         * to take care of this.*/
        sugar.active_agents_count--;
        sugar.active_agents.remove(this);
        /* prevent agent from being reschedule and reexecuted */
        my_stoppable.stop(); 
        /* Check to see if Replacement rule (GAS pp. 32-33)  */
        if (replacement) {  
            sugar.dynamicNewAgent();
        }
     }
}
