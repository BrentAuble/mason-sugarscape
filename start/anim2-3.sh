lib=lib/mason.jar:lib/jfreechart-1.0.13.jar:lib/jcommon-1.0.16.jar:lib/quaqua-colorchooser-only.jar:lib/itext-2.1.5.jar

java -classpath .:$lib sim.app.sugarscape.SugarscapeWithUIHigh -file conf/anim2-3.conf
