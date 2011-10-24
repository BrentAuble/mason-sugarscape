package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.portrayal.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.*;

/**
   A simple portrayal for 2D visualization of agents as ovals. It extends the SimplePortrayal2D and
   it manages the drawing and hit-testing for oval shapes.
*/

public class AgentPortrayal2D extends SimplePortrayal2D
    {
    public Paint paint;
    public double scale;
    public static final Font font = new Font("Monospace",Font.BOLD,11);
    public AgentPortrayal2D() { this(Color.gray,1.0); }
    public AgentPortrayal2D(Paint paint) { this(paint,1.0); }
    public AgentPortrayal2D(double scale) { this(Color.gray,scale); }
    
    public AgentPortrayal2D(Paint paint, double scale)
        {
        this.paint = paint;
        this.scale = scale;
        }
    
    // assumes the graphics already has its color set
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
        {
        if (object==null) {
        	return;
        }
    	final double width = info.draw.width*scale;
        final double height = info.draw.height*scale;

        //set color according to agent affiliation
        graphics.setPaint(((sim.app.sugarscape.Agent)object).resolveColorAffil());//getColorAffil());
        if (((sim.app.sugarscape.Agent)object).trading_neighbor) {
           graphics.setPaint(Color.GREEN); 
        }
        //graphics.setPaint(Color.gray);
        // we are doing a simple draw, so we ignore the info.clip

        final int x = (int)(info.draw.x - width / 2.0);
        final int y = (int)(info.draw.y - height / 2.0);
        final int w = (int)(width);
        final int h = (int)(height);

        // draw centered on the origin
        graphics.fillOval(x,y,w-1,h-1);

        if ( ((sim.app.sugarscape.Agent)object).marked==true) {
            graphics.setFont(font);
            graphics.setColor(Color.DARK_GRAY);
            graphics.drawString("M",x+1,y+h-2);
        }
        if ( ((sim.app.sugarscape.Agent)object).trade_partner==true) {
            graphics.setFont(font);
            graphics.setColor(Color.DARK_GRAY);
            graphics.drawString("T",x+3,y+h-2);
        }
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
