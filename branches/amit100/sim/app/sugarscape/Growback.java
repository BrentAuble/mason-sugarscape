package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.Steppable;
import sim.engine.SimState;
import sim.field.grid.ObjectGrid2D;

/**
 * Created by IntelliJ IDEA.
 * User: abigbee
 * Date: Mar 23, 2005
 * Time: 9:12:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class Growback implements Steppable, EnvironmentRule {

   ObjectGrid2D resources;
   int all_objs_xx;
   int all_objs_yy;
   Object[][] all_objs;
   Scape s;

   public Growback () {
   }

   public void setEnvironment (ObjectGrid2D grid) {
        resources = grid;
        all_objs_xx = resources.getWidth();
        all_objs_yy = resources.getHeight();
        all_objs = resources.field;

    }
    /* To do:  optimize this method.  It's currently the #4 most time-consuming method in the codebase
     * The getSeasonRate method call is a big drag */
    public void step(SimState state) {
        Sugarscape model = (Sugarscape)state;
        for (int xx = 0; xx < all_objs_xx; xx++) {
           for (int yy = 0; yy < all_objs_yy; yy++) {
                s = (Scape)all_objs[xx][yy];
                int seasonal_rate = model.getSeasonRate(s.hemisphere);
                for (int a = 0; a < model.resources; a++) {
                    //Int2D location = sugar.scape_grid.getObjectLocation(this);
                    s.time_since_last_regen[a]++;
                    /*
                     * If enough time has elapsed per the seasonal growback rate (i.e. winter or summer) and 
                     * the site is less than full capacity, need to up the site's level per the regen rate.
                     */
                    if ((s.time_since_last_regen[a] >= seasonal_rate) && (s.current_level[a] < s.capacity[a])) {
                        //System.out.println(previous + ":" + current_level );
                        //s.previous[a] = (int)s.current_level[a];
                        s.current_level[a] = s.current_level[a] + s.regen_rate[a];
                        /* Set time since last regen to 0 or 1 to turn on/off wave effect
                         */
                        s.time_since_last_regen[a] = 1;
                    }                   
                    else {
                      //s.time_since_last_regen[a]++; //s.time_since_last_regen[a]++;                   
                    }                  
                }
           }
        }

    }
}
