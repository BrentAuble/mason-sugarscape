package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.display.GUIState;
import sim.display.Display2D;
import sim.display.Console;
import sim.display.Controller;
import sim.portrayal.grid.ObjectGridPortrayal2D;
import sim.engine.SimState;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import org.jfree.chart.ChartPanel;

import ec.util.ParameterDatabase;
import java.io.InputStream;

public class SugarscapeWithUIHigh extends GUIState {
    public Display2D display;
    public JFrame displayFrame;
    public JFrame chartFrame;
    public Console console;
    public Charts charts;

    ObjectGridPortrayal2D agentsPortrayal = new ObjectGridPortrayal2D();
    ObjectGridPortrayal2D scapePortrayal = new ObjectGridPortrayal2D();
    ObjectGridPortrayal2D pollutionPortrayal = new ObjectGridPortrayal2D();

    public Object getSimulationInspectedObject()
            {
            return state;
            }


    public static void main(String[] args)
    {
        ParameterDatabase parameters = null;
        for(int x=0;x<args.length-1;x++)
            if (args[x].equals("-file")) {
                try {
                    InputStream is = Class.forName(sim.app.sugarscape.SugarscapeWithUIHigh.class.getCanonicalName()).getClassLoader().getResourceAsStream(args[x+1]);
                    parameters=new ParameterDatabase(is);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.err);
                    }
                        break;
            }
        if (parameters == null)
            {
            throw new RuntimeException("No parameter file was provided.  You need to include it with the arguments:\n\n\t-file myparameters.conf");
            }
        SugarscapeWithUIHigh t = new SugarscapeWithUIHigh(parameters);
        Console c = new Console(t);
        t.console = c;
        c.setVisible(true);
    }

    public SugarscapeWithUIHigh(ParameterDatabase parameters) { super(new Sugarscape(parameters, System.currentTimeMillis(),1)); }

    public SugarscapeWithUIHigh(SimState state) { super(state); }

    public static String getName() { return "Sugarscape v1.2"; }

    public static Object getInfo()
    {
        return "<H2>MASON Sugarscape</H2><p>Version 1.2 of MASON Sugarscape";
    }

    public void quit()
    {
        super.quit();

        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;  // let gc
        display = null;       // let gc
    }

    public void start()
    {
        super.start();
        // set up our portrayals
        setupPortrayals();
    }

    public void load(SimState state)
    {
        super.load(state);
        // we now have new grids.  Set up the portrayals to reflect that
        setupPortrayals();
        chartFrame.setVisible(false);
        controller.unregisterFrame(chartFrame);   // unregister previous frame
        addChartPanel( controller, (Sugarscape)state );   // make new frame and register it
    }

public void setupPortrayals() {
    // tell the portrayals what to
    // portray and how to portray them
    agentsPortrayal.setField(((Sugarscape)state).agents_grid);
    AgentPortrayal2D ap = new AgentPortrayal2D(Color.blue);
    agentsPortrayal.setPortrayalForNull(ap);
    agentsPortrayal.setPortrayalForAll(ap); /* does all cover null objects? */
    scapePortrayal.setField(((Sugarscape)state).scape_grid);
    scapePortrayal.setPortrayalForAll(
				       new sim.app.sugarscape.ScapePortrayal2D(Color.yellow)) ;

    pollutionPortrayal.setField(((Sugarscape)state).scape_grid);
    pollutionPortrayal.setPortrayalForAll(
				       new sim.app.sugarscape.PollutionPortrayal2D(Color.yellow)) ;

    ((Sugarscape)state).console = console;
    // reschedule the displayer
    display.reset();
    // redraw the display
    display.repaint();
}

public void init(Controller c) {
    super.init(c);
    display = new Display2D(600,600,this,1);

    //per Sean Luke to set antialias on directly
    /* 
    display.optionPane.antialias.setSelected (true);    
    display.insideDisplay.setupHints
	(display.optionPane.antialias.isSelected(),
	 display.optionPane.alphaInterpolation.isSelected(),
	 display.optionPane.interpolation.isSelected());
    */

    displayFrame = display.createFrame();
    c.registerFrame(displayFrame);
    displayFrame.setVisible(true);
    display.setBackdrop(Color.white);
    display.attach(scapePortrayal,"Resources");
    display.attach(pollutionPortrayal,"Pollution");
    display.attach(agentsPortrayal,"Agents");
    if ( ((Sugarscape)state).chart_display) {
        addChartPanel(c,(Sugarscape)state);
    }
}

void addChartPanel( Controller c, Sugarscape model )
        {
            charts = new Charts(model);
            ChartPanel ginipanel = null;
            ChartPanel wealthpanel = null;
            ChartPanel agentspanel = null;
            ChartPanel agepanel = null;
            ChartPanel evolutionpanel = null;
            ChartPanel culturetagpanel = null;
            ChartPanel tradepanel = null;

            if (model.gini_chart_on) {
                ginipanel = new ChartPanel(charts.createGiniChart());
            }
            if (model.wealth_chart_on) {
               wealthpanel = new ChartPanel(charts.createChart4());
            }
            if (model.population_chart_on) {
               agentspanel = new ChartPanel(charts.createAgentsChart());
            }
            if (model.age_chart_on) {
               agepanel = new ChartPanel(charts.createAgeHistoChart());
            }
            if (model.evolution_chart_on) {
               evolutionpanel = new ChartPanel(charts.createEvolution());
            }
            if (model.culture_tag_chart_on) {
               culturetagpanel = new ChartPanel(charts.createCultureTagChart());
            }
            if (model.trade_chart_on) {
               tradepanel = new ChartPanel(charts.createTradeChart());
            }
            if ( (!model.gini_chart_on) && (!model.wealth_chart_on) && (!model.population_chart_on)
                    && (!model.age_chart_on) && (!model.evolution_chart_on) && (!model.culture_tag_chart_on)
                    && (!model.trade_chart_on)) {
                return;
            }
            // create the chart frame
            chartFrame = new JFrame();
            chartFrame.setResizable(true);
            Container cp = chartFrame.getContentPane();
            cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));
            if (agepanel!=null) {
                cp.add(agepanel);
                cp.add(Box.createRigidArea(new Dimension(0, 6)));
              }
            if (tradepanel!=null) {
                cp.add(tradepanel);
                cp.add(Box.createRigidArea(new Dimension(0, 6)));
             }
            if (ginipanel!=null) {
              cp.add(ginipanel);
              cp.add(Box.createRigidArea(new Dimension(0, 6)));
            }
            if (wealthpanel!=null) {
              cp.add(wealthpanel);
              cp.add(Box.createRigidArea(new Dimension(0, 6)));
            }
            if (agentspanel!=null) {
              cp.add(agentspanel);
              cp.add(Box.createRigidArea(new Dimension(0, 6)));
            }
            if (culturetagpanel!=null) {
              cp.add(culturetagpanel);
              cp.add(Box.createRigidArea(new Dimension(0, 6)));
            }
            if (evolutionpanel!=null) {
              cp.add(evolutionpanel);
              cp.add(Box.createRigidArea(new Dimension(0, 6)));
            }
           
            //cp.add(chartPanel2);
            //cp.add(Box.createRigidArea(new Dimension(0,6)));
            //cp.add(chartPanel3);
            chartFrame.setTitle("Live Agent Statistics");
            chartFrame.pack();
            // register the chartFrame so it appears in the "Display" list
            c.registerFrame(chartFrame);
            // make the frame visible
            chartFrame.setVisible(true);
        }

}
