package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.Steppable;
import sim.engine.SimState;

/*
        Movement Rule M
        1.look randomly in 4 cardinal directions per vision ability
        2.pick unoccupied site with highest sugar
        3.go to that site
          for ties, go to nearest unoccupied site
        4.get all the sugar/spice at that position
 */
public class Trade implements Steppable, Rule {

    private Agent agent;

    public Trade () {
    }

    public void setAgent (Agent a) {
        agent = a;
    }

    public void step(SimState state) {

        Sugarscape sugar = (Sugarscape)state;
        agent.my_trade_count = 0;
        agent.neighbors_for_trades =0;
        agent.trade(sugar);        
    }
}
