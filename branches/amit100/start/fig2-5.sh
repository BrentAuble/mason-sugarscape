#!/bin/zsh

libjar=lib/jfreechart-1.0.1.jar:lib/jcommon-1.0.0.jar:lib/quaqua-colorchooser-only.jar:lib/itext-1.4.5.jar

#command line parameters to ParamSweeper are:
#-config configfile, configfile contains all model parameter settings
#-loops n, how many times you want each parameter set to be run
#-steps m, how many simulation steps per run
#-sweep sweepfile, which parameters in model parameter configfile should be swept 
#-id x, where x is the starting id number.  Useful if you partition the sweep sets 'manually' and run them on different nodes (like a poor man's grid). 

java  -classpath .:$libjar sim.app.sugarscape.util.ParamSweeper -file conf/fig2-5.conf -loops 10 -steps 500 -sweep conf/fig2-5_sweep.conf -id 1
