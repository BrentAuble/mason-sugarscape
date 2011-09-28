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
 */
public class Reproduction implements Steppable, Rule {

    private Agent agent;

    public Reproduction () {
    }

    public void setAgent (Agent a) {
        agent = a;
    }

    public void step(SimState state) {
        Sugarscape sugar = (Sugarscape)state;
        agent.reproduce();
    }
}
