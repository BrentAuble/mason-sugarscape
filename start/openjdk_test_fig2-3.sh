#!/bin/zsh

JAVA_HOME=/Users/abigbee/work/devel/openjdk/jdk6_devpreview_r1/control/build/bsd-i586-fastdebug/j2re-image
export JAVA_HOME

libjar=lib/jfreechart-1.0.1.jar:lib/jcommon-1.0.0.jar:lib/quaqua-colorchooser-only.jar:lib/itext-1.4.5.jar
#openjdk_cp=

/Users/abigbee/work/devel/openjdk/jdk6_devpreview_r1/control/build/bsd-i586-fastdebug/bin/java -server -Xmx512m -Xms256m -classpath .:${libjar} sim.app.sugarscape.util.ParamSweeper -file conf/fig2-3.conf -loops 10 -steps 500 -sweep sweep/fig2-3.sweep -id 1
