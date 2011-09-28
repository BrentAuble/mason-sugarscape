package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

/**
 * This class is a used to associate resources that agents can see
 * with distances.  It's useful for keeping these two associated when
 * all the sites/resources within view for each step are randomly shuffled.
 * And it prevents toroidal distance from being recalculated.
 */

public class ResourceDistPair {
  public Scape s;
  public int dist;

  public ResourceDistPair () {
  }
}
