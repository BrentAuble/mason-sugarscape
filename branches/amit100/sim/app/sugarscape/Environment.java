package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.Steppable;
import sim.engine.SimState;

/*
 * Primary class for holding all environment rules
 */

public class Environment implements Steppable {

    Sugarscape model;
    RulesSequence rules;

    public Environment (Sugarscape model, RulesSequence rules) {
        this.model = model;
        this.rules = rules;
    }

    public void step(SimState state) {
        rules.step(state);
    }

    
}
