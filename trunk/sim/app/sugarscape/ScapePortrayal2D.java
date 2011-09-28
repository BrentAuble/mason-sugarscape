package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.portrayal.*;
import sim.app.sugarscape.Sugarscape;
import sim.app.sugarscape.Scape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.*;

/**
   A simple portrayal for 2D visualization of agents as ovals. It extends the SimplePortrayal2D and
   it manages the drawing and hit-testing for oval shapes.
*/

public class ScapePortrayal2D extends SimplePortrayal2D
    {
    public Paint paint;
    public double scale;
    public static Color brown = new Color(188,130,99);

    public ScapePortrayal2D() { this(Color.gray,1.0); }
    public ScapePortrayal2D(Paint paint) { this(paint,1.0); }
    public ScapePortrayal2D(double scale) { this(Color.gray,scale); }
    
    public ScapePortrayal2D(Paint paint, double scale)
        {
        this.paint = paint;
        this.scale = scale;
        }
    
    // assumes the graphics already has its color set
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
        {
        final double width = info.draw.width*scale;
        final double height = info.draw.height*scale;
        //set color according to agent affiliation
        float proportion = ((sim.app.sugarscape.Scape)object).getSize();
        if (((sim.app.sugarscape.Scape)object).getType() == Sugarscape.SUGAR ) {
            graphics.setPaint(Color.yellow);
        }
        else {
            //System.out.print("orange ");
            graphics.setPaint(Color.orange);
        }
        //float proportion = ((sim.app.sugarscape.Scape)object).getPollutionSize();
        //graphics.setPaint(brown); 
        //graphics.setPaint(Color.gray);
        // we are doing a simple draw, so we ignore the info.clip

        final int x = (int)(info.draw.x - width / 2.0);
        final int y = (int)(info.draw.y - height / 2.0);
        final int w = (int)(width*proportion);
        final int h = (int)(height*proportion);

        // draw centered on the origin
        graphics.fillOval(x,y,w,h);
        //System.out.println(""+x+" "+y+" "+w+" "+h);
        }

    /** If drawing area intersects selected area, add last portrayed object to the bag */
    public boolean hitObject(Object object, DrawInfo2D range)
        {
        final double SLOP = 1.0;  // need a little extra area to hit objects
        final double width = range.draw.width*scale;
        final double height = range.draw.height*scale;
        Ellipse2D.Double ellipse = new Ellipse2D.Double( range.draw.x-width/2-SLOP, range.draw.y-height/2-SLOP, width+SLOP*2,height+SLOP*2 );
        return ( ellipse.intersects( range.clip.x, range.clip.y, range.clip.width, range.clip.height ) );
        }
    }
