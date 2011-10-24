package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.Steppable;
import sim.engine.SimState;

public class Pollution implements Steppable, Rule {

    private Agent agent;

    public Pollution () {
    }

    public void setAgent (Agent a) {
        agent = a;
    }

    /*
     * Actually add generated pollution to the environment.  As usual,
     * a method in Agent does all the work. 
     */
    public void step(SimState state) {
        agent.doPollution();
    }
}
