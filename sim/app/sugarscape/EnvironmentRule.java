package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.field.grid.ObjectGrid2D;

/*
 * All environment rules must implement this interface to provide access
 * to the spatial data structure (an ObjectGrid2D)
 */

public interface EnvironmentRule {

   public void setEnvironment (ObjectGrid2D grid);

}
