package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.Steppable;
import sim.engine.SimState;

/**
 * Created by IntelliJ IDEA.
 * User: abigbee
 * Date: Mar 9, 2005
 * Time: 1:12:53 AM
 *      Movement Rule M
        1.look randomly in 4 cardinal directions per vision ability
        2.pick unoccupied site with highest welfare
        3.go to that site
          for ties, go to nearest unoccupied site
        4.Harvest/get all the sugar+spice at that position
 */
public class Movement implements Steppable, Rule {

    private Agent agent;

    public Movement () {
    }

    public void setAgent (Agent a) {
        agent = a;
    }

    public void step(SimState state) {
        Scape s = agent.findBestUnoccupiedSite();
        int mb;
        Sugarscape model = (Sugarscape)state;
        /* have to set old site to null.  Only one agent per site. */
        model.agents_grid.field[agent.my_loc.x][agent.my_loc.y] = null;
        agent.my_loc.x = s.loc_x;
        agent.my_loc.y = s.loc_y;
        /* do the actual move in the grid */
        model.agents_grid.field[s.loc_x][s.loc_y] = agent;
        for (int a = 0; a < model.resources; a++) {
            if (a==model.SUGAR) { //harvest
                 agent.wealth_sugar = agent.wealth_sugar + s.current_level[a];
                 mb = agent.metabolic_rate_sugar;
            } else {
                 agent.wealth_spice = agent.wealth_spice + s.current_level[a];
                 mb = agent.metabolic_rate_spice;
            }
              /* Generate the pollution.  The Pollution rule/class will take
               * care of actually adding it to the environment */
            if ((model.schedule.time() >=model.pollution_start )) {             
                  agent.generated_pollution =  (s.current_level[a] * model.pollution_harvest);
                  agent.generated_pollution = agent.generated_pollution + (model.pollution_metabolize * mb);  //consider moving this to Biology rule
            }
            s.current_level[a] = 0;
              /***************************************************************************
              * The statement below controls whether growback occurs during a period
              * of harvesting.
              * set the time since last regen to zero to prohibit growback
              * set it to 1 to allow growback during harvesting period/time step [normal]
              * set it to 0 to prevent growback for one time period
              ****************************************************************************/
              //s.time_since_last_regen[a] = 1; //or 1
            }
    }
}
