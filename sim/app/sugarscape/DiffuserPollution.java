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
 * Date: Mar 20, 2005
 * Time: 8:05:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class DiffuserPollution implements Steppable, EnvironmentRule {

    ObjectGrid2D resources;
    int all_objs_xx;
            int all_objs_yy;
            Object[][] all_objs;
            float[][] scapes_flux = new float[all_objs_xx][all_objs_yy];
            Scape neighbor1, neighbor2, neighbor3, neighbor4;
            Scape s;
            int loc_x, loc_x_left, loc_x_right;
            int loc_y, loc_y_up, loc_y_down;
            float local_flux;
    public DiffuserPollution () {

    }

    public void setEnvironment (ObjectGrid2D grid) {
        resources = grid;
        all_objs_xx = resources.getWidth();
        all_objs_yy = resources.getHeight();
        all_objs = resources.field;
        scapes_flux = new float[all_objs_xx][all_objs_yy];
    }

    public void step(SimState state) {
        //System.out.println("DiffuserPollution.step()");
        if (state.schedule.time() < ((Sugarscape)state).pollution_diffuse_start) {
            return;
        }
        //System.out.println("DiffuserPollution active " + state.schedule.time());
        for (int xx = 0; xx < all_objs_xx; xx++) {
              for (int yy = 0; yy < all_objs_yy; yy++) {
                       s = (Scape)all_objs[xx][yy];
                       loc_x = s.loc_x;
                       loc_y = s.loc_y;
                       local_flux = 0;
                       loc_x_left = loc_x-1;
                       loc_x_right = loc_x + 1;
                       loc_y_up = loc_y - 1;
                       loc_y_down = loc_y + 1;
                       neighbor1 = (Scape)resources.get(resources.stx(loc_x_left),loc_y);//all_objs[loc_x_left][loc_y];
                       neighbor2 = (Scape)resources.get(resources.stx(loc_x_right),loc_y);
                       neighbor3 = (Scape)resources.get(loc_x,resources.sty(loc_y_up));
                       neighbor4 = (Scape)resources.get(loc_x,resources.sty(loc_y_down));
                       local_flux = local_flux + neighbor1.pollution;
                       local_flux = local_flux + neighbor2.pollution;
                       local_flux = local_flux + neighbor3.pollution;
                       local_flux = local_flux + neighbor4.pollution;
                       scapes_flux[xx][yy] = local_flux/(4);
                   }
               }
               //then apply new pollution to each site at the same time
               for (int xx=0; xx < all_objs_xx; xx++) {
                  for (int yy=0; yy < all_objs_yy; yy++) {
                   ((Scape)all_objs[xx][yy]).pollution=scapes_flux[xx][yy];
                  }
               }
    }
}
