package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.util.Bag;

public class WelfareEstimation {
         public double best_so_far;
         //welfare function exponents m1/mT and m2/MT per page 97 of GAA
         public double welfare_sugar_exp, welfare_spice_exp;

         public static final int BEST_X = 0;
         public static final int BEST_Y = 1;
         public int[] best_loc;
         public ResourceDistPair[] best_sites;
         public double welfare_sugar;
         public double welfare_spice;
         public boolean culture_trade;
         public ResourceDistPair best_r;
         //public ResourceDistPair best_r_spice;
         //public int vision_chunk;

         int vision,total_sites;
         int smallest_delta;
         //int smallest_delta_spice;

         int selected_index;
         Sugarscape sugar;
         int agent_x, agent_y;
         Scape[] vision_sites;
         Bag vision_index;
         Agent ag;
         int self_loc;

         public static final int[][] scan = {{0,1,2,3},
                                          {0,1,3,2},
                                          {0,2,3,1},
                                          {0,2,1,3},
                                          {0,3,1,2},
                                          {0,3,2,1},

                                          {1,2,3,0},
                                          {1,2,0,3},
                                          {1,3,2,0},
                                          {1,3,0,2},
                                          {1,0,2,3},
                                          {1,0,3,2},

                                          {2,1,3,0},
                                          {2,1,0,3},
                                          {2,3,0,1},
                                          {2,3,1,0},
                                          {2,0,1,3},
                                          {2,0,3,1},

                                          {3,1,2,0},
                                          {3,1,0,2},
                                          {3,2,1,0},
                                          {3,2,0,1},
                                          {3,0,1,2},
                                          {3,0,2,1}
                                          };
    public static final int[][] scan_deltas = {{0,-1},
                                                   {1,0},
                                                   {0,1},
                                                   {-1,0}};

    public Bag res_dis;
    public Object[] rd_objs;
    public boolean toroidal;
    public double[] welfare_est_sugar;
    public double[] welfare_est_spice;
    private int resources;
    /* provide direct access to agents in tight loops */
    private Object[][] field;
    private int grid_width;
    private int grid_height;

    public WelfareEstimation (Sugarscape sugar, int vision, Agent ag) {
         this.ag = ag;
         this.sugar = sugar;
         field = sugar.agents_grid.field;
         grid_width = sugar.agents_grid.getWidth();
         grid_height = sugar.agents_grid.getWidth();
         best_loc = new int[2];
         //best_loc_spice = new int[2];
         toroidal = sugar.toroidal;
         vision_sites = new Scape[4];
         //we'll put the agent's current vision at the last index of the data structure
         self_loc = (4 * vision);
         //calcuate total number of sites that can be seen
         total_sites = self_loc+1; //include current location
         //create a holder for all these sites
         res_dis = new Bag(total_sites);
         this.vision = vision;
         vision_index = new Bag(vision);
         //generate all the ResourceDistPairs that will be reused each step
         for (int a = 0; a < total_sites; a++) {
             ResourceDistPair r = new ResourceDistPair();
             res_dis.add(r);
         }
         rd_objs = res_dis.objs; // we will reuse this
         resources = sugar.resources;
         double total;
         if (resources == 1) { //just sugar
           welfare_sugar_exp = 1.0d;
           total = (double)(ag.metabolic_rate_sugar);// + ag.metabolic_rate_spice);
           welfare_spice_exp = 1.0d;
         } else { //assume sugar *and* spice
           total = (double)(ag.metabolic_rate_sugar + ag.metabolic_rate_spice);
           //check to see if culturally dependent trading preferences
           if (sugar.culture_trade) {
               culture_trade = true;
               calcCultureWelfare();
           } else {
               culture_trade = false;
               welfare_spice_exp = ag.metabolic_rate_spice/total;
               welfare_sugar_exp = ag.metabolic_rate_sugar/total;
               //System.out.println(welfare_spice_exp + " welfare_spice_exp");
               //System.out.println(welfare_sugar_exp + " welfare_sugar_exp");
           }
         }

         //calcuate exponents for welfare function
         //System.out.println();
         //System.out.println(welfare_sugar_exp + " = welfare_sugar_exp");
         //System.out.println(welfare_spice_exp + " = welfare_spice_exp");
         best_sites = new ResourceDistPair[total_sites];
         welfare_est_sugar = new double[Scape.MAX_VALS+1];//we won't use index 0 for convenience
         welfare_est_spice = new double[Scape.MAX_VALS+1];//we won't use index 0 for convenience
    }

    /******************************************************************
     * Use this method to dynamically update the exponents for each
     * resource in the welfare function per page 125 of GAS
     ******************************************************************/
    public void calcCultureWelfare () {
         double f = ag.culture.fractionZeros();
         double mu = (ag.metabolic_rate_sugar*f) + (ag.metabolic_rate_spice)*(1-f);
         welfare_sugar_exp = ag.metabolic_rate_sugar/mu*f;
         welfare_spice_exp = ag.metabolic_rate_spice/mu*(1-f);
    }

    public void newStep(int x, int y) {

    }

    public double bestLevel() {
        return best_so_far;
    }

    public int[] bestLocation() {
        return best_loc;
    }

    public void getSitesAtDistanceToroid (int dist) {
        int t_x;
        int t_y;
    	int temp_x = agent_x;
        int temp_y = agent_y;
        int order = sugar.random.nextInt(24); //index to all possible examinations of 4-way vision
        //chunk size is vision
        int chunk = (dist-1)*4;  //index 0
        for (int a = 0; a < 4; a++) {
            t_x = sugar.scape_grid.stx(temp_x+(scan_deltas[scan[order][a]][0]*dist));
            t_y = sugar.scape_grid.sty(temp_y+(scan_deltas[scan[order][a]][1]*dist));
            ResourceDistPair r = (ResourceDistPair)rd_objs[chunk+a];
            if (field[t_x][t_y] ==null) {
                r.s = (Scape)sugar.scape_grid.field[t_x][t_y];
                r.dist = dist;
            } else {
            	/* We *have* to make this object null so that any previous site in rd_objs[chunk+a]
                are not evaluated in process().
            	*/
            	r.s = null;
            	r.dist = -1;
            }
        }
    }

    public void getSitesAtDistanceSquare(int dist) {
        int temp_x = agent_x;
        int temp_y = agent_y;
        //chunk size is 4-way (vision/movement)
        int chunk = (dist-1)*4;
        int order = sugar.random.nextInt(24);
        for (int a = 0; a < 4; a++) {
            int t_x = sugar.scape_grid.stx(temp_x+(scan_deltas[scan[order][a]][0]*dist));
            int t_y = sugar.scape_grid.sty(temp_y+(scan_deltas[scan[order][a]][1]*dist));
            ResourceDistPair r = (ResourceDistPair)rd_objs[chunk+a];
            if (field[t_x][t_y]==null) {
                r.s = (Scape)sugar.scape_grid.field[t_x][t_y];
            } else {
            	r.s = null;
            	r.dist = -1;
            }
            //here's how we constrain to square space
            if (t_x != (temp_x+(scan_deltas[scan[order][a]][0]*dist))) {
                r.s = null;
            } else if (t_y!=temp_y+((scan_deltas[scan[order][a]][1]*dist))) {
                r.s = null;
            }
            r.dist = dist; //so we *never* have to recalculate distance
        }
    }

    /*
     * x,y is current agent's location
     * identify the closest site yielding the highest welfare.
     */
    public void estimate(int x, int y) {
        best_so_far = 0;
        best_loc[BEST_X] = -1;
        best_loc[BEST_Y] = -1;
        smallest_delta = 999999;
        selected_index = -1;
        agent_x = x;
        agent_y = y;
        //load all sites within vision except current site
        if (sugar.toroidal) {
            for (int dist = 1; dist <= vision; dist++) {  
                 getSitesAtDistanceToroid(dist);
            }
        } else {
            for (int dist = 1; dist <= vision; dist++) {  
                 getSitesAtDistanceSquare(dist);
            }
        }
        /*
         * now add current location site.
         */
        ResourceDistPair r = (ResourceDistPair)rd_objs[self_loc];
        r.s = (Scape)sugar.scape_grid.field[x][y];
        r.dist = 0;      
        if (culture_trade) { //should we update exponent to welfare function via
                             //cultural preference ?
            calcCultureWelfare();
        }       
        //if (r.s.getLevel() < 1) {
        //    System.out.print("sitting on " + r.s.getLevel() + " " + r.s.previous[0]);
        //}       
        process();
    }

    public void process() {
    	int _sugar = Sugarscape.SUGAR;
    	int _spice = Sugarscape.SPICE;
        welfare_sugar = StrictMath.pow(ag.wealth_sugar, welfare_sugar_exp);
        //System.out.println(welfare_sugar + " welfare_sugar");
        welfare_spice = StrictMath.pow(ag.wealth_spice,welfare_spice_exp);
        //System.out.println(welfare_spice + " welfare_spice");
        /*since there are only 4 levels for each resource, we can precalculate the contribution
        of each type, for each level, just once and reuse in this.calcWelfare() */
        if (sugar.resources > 1) { //don't need to calculate all this stuff if only 1 resource
            welfare_est_sugar[1] = StrictMath.pow(ag.wealth_sugar+1, welfare_sugar_exp);
            welfare_est_sugar[2] = StrictMath.pow(ag.wealth_sugar+2, welfare_sugar_exp);
            welfare_est_sugar[3] = StrictMath.pow(ag.wealth_sugar+3, welfare_sugar_exp);
            welfare_est_sugar[4] = StrictMath.pow(ag.wealth_sugar+4, welfare_sugar_exp);

            welfare_est_spice[1] = StrictMath.pow(ag.wealth_spice+1, welfare_spice_exp);
            welfare_est_spice[2] = StrictMath.pow(ag.wealth_spice+2, welfare_spice_exp);
            welfare_est_spice[3] = StrictMath.pow(ag.wealth_spice+3, welfare_spice_exp);
            welfare_est_spice[4] = StrictMath.pow(ag.wealth_spice+4, welfare_spice_exp);
        }
        double level;
        int dist;
        int best_locs = 0; //number of "ties" (sites with same welfare) from which to randomly choose
        ResourceDistPair r;
        if (ag.diag) {
        //    System.out.println(agent_x + ":" + agent_y + " = my_loc");
        }
        int unoccupied_sites = total_sites - 1;
        for (int ss = 0; ss < unoccupied_sites; ss++) {
            r = (ResourceDistPair)rd_objs[ss];
            Scape s = r.s;
            if (s!=null) {
	            /*get the level of resources from a welfare view, that is the most there
	             and most being a function of the product of all resource levels
	             this really should be generalized to n resources, but is somewhat specific
	            to having either sugar, spice, or both. */
	            level = 0;
	            if (sugar.resources==2) { /* do we need to do the Cobb-Douglas calc per GAS p97? */
	                //direct access to sugar and spice at the site now instead of using a method
	                level = welfare_est_sugar[ s.current_level[_sugar] ];
	                //level = welfare_est_sugar[(int)s.getLevel(Sugarscape.SUGAR)];
	                level = level * welfare_est_spice[ s.current_level[_spice] ];
	                //level = level * welfare_est_spice[(int)s.getLevel(Sugarscape.SPICE)];
	            } else if (sugar.resources==1) {
	                /* to do:  is this possible make this a member lookup rather than a method */ 
	            	level = s.getLevel();   //could be either sugar or spice there; in the one resource setting we don't know which and don't care
	              }
	               /*devalue for pollution per page 47 of GAS
	                *if pollution rule is inactive, pollution will always be 0,
	                *and denomoniator will always be 1 */              
	              level = level / (1 + s.pollution);

	              if ( (level >= best_so_far) && (level > 0)) { //was getObjectsAtLocation(s.loc_x, s.loc_y)
	                  dist = r.dist;
	                  if (level > best_so_far) { //a new best site
	                     best_so_far = level;
	                     smallest_delta = dist;
	                     best_locs = 1;   //1 indicates a single best site
	                     ag.best_locations = 1;
	                     best_sites[0] = r;
	                     if (ag.diag) {
	                        System.out.println(r.s.getLevel() + " " + r.s.loc_x + ":" + r.s.loc_y + " dist < smallest_delta");
	                     }
	                     //best_r = r; //for debugging purposes
	                  } else { //level is equal, is distance smaller?
	                        if ( (dist < smallest_delta) ) { //  && (dist!=0)  if own site and other sites have equal resources, don't stay put!
	                           smallest_delta = dist;
	                           best_locs = 1;
	                           ag.best_locations = 1;
	                           best_sites[0] = r;
	                           if (ag.diag) {
	                              System.out.println(r.s.getLevel() + " " + r.s.loc_x + ":" + r.s.loc_y + " dist < smallest_delta");
	                           }
	                                        //best_r = r;  //for debugging purposes
	                        } else if (dist == smallest_delta) {   //same level and distance as other sites, pick a random one later
	                             best_sites[best_locs] = r;
	                             best_locs++;
	                             ag.best_locations++;
	                             if (ag.diag) {
	                                 System.out.println(r.s.getLevel() + " " + r.s.loc_x + ":" + r.s.loc_y + " dist = smallest delta.");
	                             }
	                          }
	                     }
	                }
             }
         }
        if (ag.diag) {
            System.out.println("---------------");
            System.out.println(best_locs + " = best_locs");
        }
        /* there were unoccupied sites where level > 1 not including current loc, randomly pick one that provide equal welfare */
        if (best_locs > 1) {
             best_r = best_sites[sugar.random.nextInt(best_locs)];        
        } /* there was one best unoccupied site not current location */ 
        else if (best_locs==1) {
              best_r = best_sites[0];
          } else {  
        	  /* no best sites, i.e. cannot go to a site with non-zero resource, so stay put.
        	   */
              best_r = (ResourceDistPair)rd_objs[total_sites-1];  /* rd_objs[total_sites-1] is *always* self location by design */
            }
    }
}
