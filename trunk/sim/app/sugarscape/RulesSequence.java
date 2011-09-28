package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.Steppable;
import sim.engine.Sequence;
import sim.engine.SimState;
import sim.util.Bag;

 /*****************************************************************************
 * This class is instantiated by the model for each agent.  Each agent's step()
 * invokes its RulesSequence.step() and the rules added by the model--based on
 * the configuration file/parameters--are invoked in the order originally added
 * to the Sequence.  
 * After all the rules are added, setup() must be called so that 
 * sim.engine.Sequence can be instantiated.
 *****************************************************************************/
public class RulesSequence implements Steppable {

    private Sequence sequence;
    private Steppable[] steps;
    Bag rules;
    /* Maximum number of anticipated rules, but not constrained to this many*/
    public static final int MAX_RULES = 10;

    public RulesSequence () {
      rules = new Bag(MAX_RULES);
    }

    /**********************************************************************
     * Alternate constructor
    ***********************************************************************/
    public RulesSequence (int num_rules) {
       rules = new Bag(num_rules);
    }

    /**************************************************************************
    * Called by the model to indicate adding rules is complete.  Set up steps[]
    * steps[] for the sequence so that we're using an array and not some 
    * other slower data structure.
    **************************************************************************/
    public void setup () {
        steps = new Steppable[rules.size()];
        System.arraycopy(rules.objs,0,steps,0,rules.size());
        sequence = new Sequence(steps);
    }

    public void step (SimState state) {
        sequence.step(state);        
    }
    
    /**************************************************************************
     * Add rules one at time.  When done, call setup()
     *************************************************************************/
    public void addRule (Object rule) {
       rules.add(rule);
    }
}
