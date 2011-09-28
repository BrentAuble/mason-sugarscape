package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.SimState;
import sim.engine.Steppable;

/*
 * Biological functions
 * 1.  Metabolize resources
 * 2.  Age
 * 3.  Die if out of resources or at max age
 */

public class Biological implements Steppable, Rule {

    public Agent my_agent;

    public Biological () {
    }

    public void setAgent (Agent a) {
        my_agent = a;
    }

    public void step(SimState state) {
        my_agent.metabolize();
        my_agent.age = my_agent.age + 1;
        if ( (my_agent.wealth_sugar < 1) || (my_agent.wealth_spice < 1) || (my_agent.age > my_agent.max_age)) {
            my_agent.die((Sugarscape)state);
            return;
        }
    }
}
