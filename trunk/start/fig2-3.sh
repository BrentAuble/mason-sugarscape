libjar=lib/jfreechart-1.0.1.jar:lib/jcommon-1.0.0.jar:lib/quaqua-colorchooser-only.jar:lib/itext-1.4.5.jar

java  -classpath .:$libjar sim.app.sugarscape.util.ParamSweeper -file conf/fig2-3.conf -loops 10 -steps 500 -sweep sweep/fig2-3.sweep -id 1
