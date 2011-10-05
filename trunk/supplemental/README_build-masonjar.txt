The build-masonjar.xml file in this directory is used to build a jar that has all the required MASON and ECJ (ec.util) classes and resources to run Sugarscape.

1.  Download MASON and ECJ.
2.  Unpack ECJ and copy over the ec utility to the main MASON directory so that sim and ec directories are in the same directory.
3.  remove ec/util/Code.java. 
3.  Copy supplemental/build-masonjar.xml to that directory.
4.  Change to that directory and execute:
 
ant -f build-masonjar.xml

This will compile MASON and ECJ classes and also copy all necessary resources that the MASON GUI requires into appropriate directories a new mason.jar file.

If there is a compile error, remove the additional .java file in ec/util that is causing it and reexecute this step.

5.  Copy the resulting mason.jar to mason-sugarscape/lib/

Include mason.jar in your classpath when trying to run Sugarscape.  See start/anim2-3.sh for example.
