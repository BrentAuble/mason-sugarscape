package sim.app.sugarscape;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import java.awt.Color;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeriesCollection;



/**
 * Created by IntelliJ IDEA.
 * User: abigbee
 * Date: Mar 12, 2005
 * Time: 10:18:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class Charts {

    Sugarscape model;
    public Charts (Sugarscape sugar) {
       model = sugar;
    }


    JFreeChart createTradeChart() {
       JFreeChart chart = ChartFactory.createXYLineChart(
               "Trading and Population over Time",
               "Time",
               "Level",
                model.agents_series_coll,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
            model.trade_chart = chart;
            NumberAxis rangeAxis1 = new NumberAxis("Time");
            rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            org.jfree.chart.axis.NumberAxis domainAxis = new NumberAxis("Bins");
            XYPlot plot = chart.getXYPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            XYItemRenderer renderer = plot.getRenderer();
            renderer.setSeriesPaint(0, Color.BLUE);
            plot.setDataset(1, model.trade_coll);           
            XYItemRenderer rend2 = new StandardXYItemRenderer();
            //if (rend2 != null)
            rend2.setSeriesPaint(1, Color.BLACK);
            plot.setRenderer(1, rend2);
            return chart;
    }


    JFreeChart createCultureTagChart() {
       JFreeChart chart = ChartFactory.createXYLineChart(
               "Culture Tag Time Series",
               "Time",
               "Fraction Blue",
                model.culture_tag_coll,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
            model.culture_tag_chart = chart;
            NumberAxis rangeAxis1 = new NumberAxis("Time");
            rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

            XYPlot plot = chart.getXYPlot();
            ValueAxis xAxis = plot.getDomainAxis();

            XYItemRenderer renderer = plot.getRenderer();
            renderer.setSeriesPaint(0, Color.BLACK);

            return chart;
    }

    JFreeChart createEvolution() {
       JFreeChart chart = ChartFactory.createXYLineChart(
               "Evolution of Mean Agent Vision and Metabolism",
               "Time",
               "Level",
                model.evolution_vision_coll,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
            model.evolution_chart = chart;
            NumberAxis rangeAxis1 = new NumberAxis("Time");
            rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            org.jfree.chart.axis.NumberAxis domainAxis = new NumberAxis("Bins");
            XYPlot plot = chart.getXYPlot();
            ValueAxis xAxis = plot.getDomainAxis();

            XYItemRenderer renderer = plot.getRenderer();
            renderer.setSeriesPaint(0, Color.BLACK);
            plot.setDataset(1, model.evolution_metabolism_coll);
            renderer.setSeriesPaint(1, Color.BLUE);
            return chart;
    }


    JFreeChart createAgeHistoChart() {
            JFreeChart chart = ChartFactory.createHistogram(
               "Age Distribution",
               "Age",
               "Count",
                model.age_hist_dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
            model.age_histo_chart = chart;

            //CategoryDataset dataset1 = createDataset1();
            NumberAxis rangeAxis1 = new NumberAxis("Age");
            rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

            org.jfree.chart.axis.NumberAxis domainAxis = new NumberAxis("Bins");
            XYPlot plot = chart.getXYPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            xAxis.setRange(0,100);
            XYItemRenderer renderer1 = plot.getRenderer();
            renderer1.setSeriesPaint(0, Color.MAGENTA);
            return chart;
    }

    JFreeChart createChart4() {
            JFreeChart chart4 = ChartFactory.createHistogram(
               "Wealth Distribution",
               "Wealth",
               "Count",
                model.dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
            model.chart4 = chart4;

            //CategoryDataset dataset1 = createDataset1();
            NumberAxis rangeAxis1 = new NumberAxis("Wealth");
            rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            //XYItemRenderer renderer1 = new XYItemRenderer();


            //renderer1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
            //CategoryPlot subplot1 = new CategoryPlot((IntervalXYDataset)dataset, null, rangeAxis1, renderer1);
            //XYPlot subplot1 = new XYPlot((IntervalXYDataset)model.dataset, rangeAxis1, null,  renderer1);
            //ValueAxis yAxis = subplot1.getRangeAxis();
            //yAxis.setRange(0,MAX_AGENT_VISION);
            //subplot1.setDomainGridlinesVisible(true);
            //XYPlot plot = chart4.getXYPlot();
            //org.jfree.chart.plot.CombinedDomainXYPlot
             //Axis domainAxis = new CategoryAxis("Bin");
            org.jfree.chart.axis.NumberAxis domainAxis = new NumberAxis("Bins");
            XYPlot plot = chart4.getXYPlot();
            XYItemRenderer renderer1 = plot.getRenderer();
            renderer1.setSeriesPaint(0, Color.MAGENTA);
           //new CombinedDomainXYPlot(domainAxis);
            //subplot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);


            //xy.setRenderer(1,renderer1);


            //plot.add(subplot1, 1);

            //XYItemRenderer renderer2 = new DefaultXYItemRenderer();
            //ValueAxis yAxis = subplot1.getRangeAxis();

            //XYPlot subplot2 = new XYPlot(gini_coeff, null, rangeAxis2, renderer2);


            //JFreeChart chart = new JFreeChart("Wealth Distribution", plot);
            //chart.getXYPlot();

                    //xAxis.setFixedDimension(100);
            //yAxis.setFixedDimension(12);
            return chart4;
    }

    JFreeChart createGiniChart() {
        JFreeChart chart3 = ChartFactory.createXYLineChart(
                 "Lorenz Curve",
                 "Population Percentage",
                 "Percentage of Total Wealth",
                  new XYSeriesCollection(model.lorenz_curve),
                  PlotOrientation.VERTICAL,
                  true,
                  true,
                  false);
                  XYPlot plot = chart3.getXYPlot();
                  ValueAxis yAxis = plot.getRangeAxis();
                    //xAxis.setFixedDimension(100);
                  yAxis.setFixedDimension(1.0);
                  //yAxis.setRange(0,1);
                  ValueAxis xAxis = plot.getDomainAxis();
                  xAxis.setFixedDimension(50);

                  //StandardXYItemRenderer 
                  XYItemRenderer renderer = plot.getRenderer();
                  renderer.setSeriesPaint(0, Color.black);
                  NumberAxis axis2 = new NumberAxis("Average Agent Vision");

            //axis2.setAutoRangeIncludesZero(false);
            axis2.setRange(0,12);
            plot.setRangeAxis(1, axis2);
            plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
            //XYSeriesCollection vision = new XYSeriesCollection(lorenz_agent_vision);
            //plot.setDataset(1, vision);
            return chart3;
    }


    JFreeChart createChart2() {
        JFreeChart chart2 =
                    ChartFactory.createXYLineChart(
                    "Wealth Distribution",     // the title of the chart
                    "Time step",
                    //"% Population",                                          // the label for the X axis
                    "Gini Coefficient",
                    // % Wealththe label for the Y axis
                    new XYSeriesCollection(model.gini_coeff),              // the dataset for the chart
                    PlotOrientation.VERTICAL,                             // the orientation of the chart
                    true,                                                 // a flag specifying whether or not a legend is required
                    true,                                                 // a flag specifying whether or not tooltips should be generated
                    false);                                               // a flag specifying whether or not the chart should generate URLs

            XYPlot plot = chart2.getXYPlot();

            ValueAxis xAxis = plot.getDomainAxis();
            xAxis.setFixedDimension(100);
            //xAxis.setRange(0,100);
            //yAxis.setRange(0,1);
           XYItemRenderer renderer = plot.getRenderer();
           renderer.setSeriesPaint(0, Color.black);
            //System.out.println("done creating chart");
            return chart2;
    }

    JFreeChart createAgentsChart()
        {
            JFreeChart chart =
                    ChartFactory.createXYLineChart(
                    "Alive Agents",     // the title of the chart
                    "Time Step",                                          // the label for the X axis
                    "Alive Agents",                               // the label for the Y axis
                    model.agents_series_coll,              // the dataset for the chart
                    PlotOrientation.VERTICAL,                             // the orientation of the chart
                    true,                                                 // a flag specifying whether or not a legend is required
                    true,                                                 // a flag specifying whether or not tooltips should be generated
                    false);                                               // a flag specifying whether or not the chart should generate URLs


            XYPlot plot = chart.getXYPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            //xAxis.setFixedDimension(50);
            //System.out.println("agents time series chart created");
            ValueAxis axis1 = plot.getRangeAxis();
            //axis1.setRange(0,1750);
            //StandardXYItemRenderer renderer = (StandardXYItemRenderer) plot.getRenderer(0);

            /*Color purple = new Color(140,0,164);
            renderer.setSeriesPaint(0,purple);

            ((NumberAxis)axis1).setLabelPaint(purple);
            ((NumberAxis)axis1).setTickLabelPaint(purple);

            NumberAxis axis2 = new NumberAxis("Average Metabolic Rate");

            //axis2.setAutoRangeIncludesZero(false);

            plot.setRangeAxis(1, axis2);
            plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
            XYSeriesCollection metab = new XYSeriesCollection(model.metabolism);
            plot.setDataset(1, metab);

            StandardXYItemRenderer rend2 = new StandardXYItemRenderer();

            //if (rend2 != null)
            rend2.setSeriesPaint(1, Color.BLACK);
            ((NumberAxis)axis2).setLabelPaint(Color.BLACK);
            ((NumberAxis)axis2).setTickLabelPaint(Color.BLACK);

            plot.setRenderer(1, rend2);
            plot.mapDatasetToRangeAxis(1, 1);

            NumberAxis axis3 = new NumberAxis("Age");
            plot.setRangeAxis(2, axis3);
            plot.setRangeAxisLocation(2, AxisLocation.BOTTOM_OR_RIGHT);
            XYSeriesCollection ageXYSeries= new XYSeriesCollection(model.age);
            plot.setDataset(2, ageXYSeries);
            //ValueAxis yAgeAxis = plot.getRangeAxis();

            axis3.setRange(0,80);
            axis3.setLabelPaint(Color.blue);
            axis3.setTickLabelPaint(Color.blue);
            StandardXYItemRenderer rend3 = new StandardXYItemRenderer();
            //if (rend2 != null)
            rend3.setSeriesPaint(2, Color.BLUE);
            plot.setRenderer(2, rend3);
            plot.mapDatasetToRangeAxis(2, 2);

            NumberAxis axis4 = new NumberAxis("Alive Agents");
            plot.setRangeAxis(3, axis4);
            axis4.setRange(0,1500);
            axis4.setLabelPaint(Color.BLACK);
            axis4.setTickLabelPaint(Color.BLACK);
            plot.setRangeAxisLocation(3, AxisLocation.BOTTOM_OR_RIGHT);
            XYSeriesCollection agentsXYSeries= new XYSeriesCollection(model.agents_series);
            plot.setDataset(3, agentsXYSeries);
            //ValueAxis yAgentsAxis = plot.getRangeAxis();


            StandardXYItemRenderer rend4 = new StandardXYItemRenderer();
            //if (rend2 != null)
            rend4.setSeriesPaint(3, Color.BLACK);
            plot.setRenderer(3, rend4);
            plot.mapDatasetToRangeAxis(3, 3);

            //}
            //System.out.println("done creating chart"); */
            return chart;
        }
}
