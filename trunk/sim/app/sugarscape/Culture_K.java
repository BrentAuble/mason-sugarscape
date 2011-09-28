package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.Steppable;
import sim.engine.SimState;
import sim.app.sugarscape.Sugarscape;
import sim.app.sugarscape.Agent;
import sim.app.sugarscape.Culture;
import sim.util.Bag;

/*
****************************************************
*Implement Rule K of Sugarscape                    *
*actually implements 2 rules regarding culture that*
*are combined as rule K.                           *
****************************************************
*/

public class Culture_K implements Steppable, Rule {

    boolean[][] tags;
    Culture[] neighbor_tag;
    boolean [] cur_tag;
    int max_tag;
    public Agent agent;

    /* The scan array is different from the scan array in WelfareEstimation
     * in that the indexing goes from 1 to 4.  So to access the scan_deltas array
     * elements, 1 must be subtracted.  See the middle of step() below for this.
     */
    
    public static final int[][] scan =   {{1,2,3,4},
                                          {1,3,2,4},
                                          {1,3,4,2},
                                          {1,2,4,3},
                                          {1,4,2,3},
                                          {1,4,3,2},

                                          {2,1,3,4},
                                          {2,1,4,3},
                                          {2,3,4,1},
                                          {2,3,1,4},
                                          {2,4,1,3},
                                          {2,4,3,1},

                                          {3,1,2,4},
                                          {3,1,4,2},
                                          {3,2,1,4},
                                          {3,2,4,1},
                                          {3,4,1,2},
                                          {3,4,2,1},

                                          {4,1,2,3},
                                          {4,1,3,2},
                                          {4,2,1,3},
                                          {4,2,3,1},
                                          {4,3,1,2},
                                          {4,3,2,1}};
            
    public static final int[][] scan_deltas = {{0,-1},
                                               {1,0},
                                               {0,1},
                                               {-1,0}};

    Agent[] neighbor_list;

    public Culture_K () {
    }

    public void setAgent (Agent a) {
        agent = a;
        a.initCulture();
    }

    public void step (SimState state) {
        Sugarscape sugar = (Sugarscape)state;
        int loc_x = agent.my_loc.x;
        int loc_y = agent.my_loc.y;
        int curr_x, curr_y;
        int index, scan_index, random_tag;
        Culture neighbor_culture, current_agent_culture;
        boolean tag;
        Sugarscape model = (Sugarscape)state;
        current_agent_culture = agent.culture;
        /* identify 4-way neighbors */
        scan_index = state.random.nextInt((24));
        for (int zz = 0; zz < 4; zz++) {
            index = scan[scan_index][zz];
            curr_x = sugar.agents_grid.stx(loc_x + scan_deltas[index-1][0]);
            curr_y = sugar.agents_grid.sty(loc_y + scan_deltas[index-1][1]);           
            Agent neighbor= (Agent)sugar.agents_grid.field[curr_x][curr_y];
            if (neighbor!=null) {
                neighbor_culture = neighbor.culture;
                random_tag = state.random.nextInt(model.culture_tags);//max_tag);
                tag = current_agent_culture.tagset[random_tag];
                if (neighbor_culture.tagset[random_tag] != tag) {
                     neighbor_culture.tagset[random_tag] = tag;
                }
             }
        }
    }
}

