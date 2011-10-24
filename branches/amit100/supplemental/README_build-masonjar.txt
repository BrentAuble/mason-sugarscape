The build-masonjar.xml file in this directory is used to build a jar that has all the required MASON and ECJ (ec.util) classes and resources to run Sugarscape.

1.  Download MASON and ECJ.
2.  Unpack both, and from the ECJ root directory,

2.1 remove ec/util/Code.java.
2.2 copy supplemental/build-masonjar.xml to the root MASON directory.
2.3 copy over the ec/util directory to the main MASON directory so that sim and ec directories are in the same directory.

In Linux/Unix, that would be:

tar -cf ecutil.tar ec/util
cp ecutil.tar path-to-mason-dir
cd path-to-mason-dir
tar -xf ecutil.tar


4.  Change to that directory and execute:
 
ant -f build-masonjar.xml

This will compile MASON and ECJ classes and also copy all necessary resources that the MASON GUI requires into appropriate directories a new mason.jar file.

If there is a compile error, remove the additional .java file in ec/util that is causing it and reexecute this step.

5.  Copy the resulting mason.jar to mason-sugarscape/lib/

Include mason.jar in your classpath when trying to run Sugarscape.  See start/anim2-3.sh for example.
