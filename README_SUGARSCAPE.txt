------------------------------------
LAST MODIFIED:   06 Oct 2011
CREATED:         25 Nov 2006
------------------------------------

This file describes how to install, run, and further develop the MASON Sugarscape implementation.
Questions should be directed to tony.bigbee@gmail.com

Contents and source code are generally covered by the Academic Free License version 3.0, granted by either Sean Luke et al. (George Mason University) or Tony Bigbee.

MASON Sugarscape homepage:
http://code.google.com/p/mason-sugarscape/

Thanks for the following for constructive comments, bug reports, and support:
Amit Goel, Claudio Cioffi-Revilla, Sean Luke, Joshua Epstein, Robert Axtell, Liviut Panait, Ann Palkovich, Randy Casstevens, Rafal Kicinger.

Contents
-------------------------------------
[1] BACKGROUND
[2] INSTALLATION
[3] BUILDING/COMPILING
[4] RUNNING AND DEVELOPMENT
[5] PERFORMANCE EXPECTATIONS
[6] TROUBLESHOOTING/FAQ
[7] FURTHER INFORMATION
[8] FUTURE RESEARCH, STUDENT PROJECTS
-------------------------------------

[1] BACKGROUND

This package contains an implementation of the classic Sugarscape model described in Growing Artificial Societies (1996, Epstein and Axtell).  It is an attempt to replicate both the model and many of the simulation outcomes described in GAS.  The MASON Sugarscape also provides some provisions for doing things like parameter sweeps that were not part of the original model per se, but are very useful in experiments.  I believe the core codebase is suitable for use in student class projects and small graduate research projects--see section 7 for some suggestions.

The most important concept to understand is that running models is organization around *outcomes* described in GAS.  Outcomes are Figures, Animations and are supplemented with qualitative and quantitative descriptions in the narrative text.  Outcomes depend on things like initial distributions, active rules, and output mechanisms that are in configuration files for each particular outcome.  Expectations regarding what MASON Sugarscape can and can't currently do should be initially shaped by reviewing the outcomes replication table in either paper referenced in section 6.   These papers, particularly the thesis, contain descriptions about MASON infrastructure and classes used to implement the model and design criteria.

The code contained in this distribution has several bug fixes and general improvements over the code documented in the thesis paper.  One ramification is that the simulation outcomes in this or future releases are likely to deviate from those in the paper.  I conjectured in the thesis that one of the reasons that I sometimes obtained different outcomes compared to GAS outcomes was partially due to unknown bugs that have now been found.

[2] INSTALLATION

Unpacking the latest distribution or checking out   The distribution includes all the MASON proper core classes and required third party .jar-based libraries in a subdirectory called lib.  If you want to use another version of MASON, you can copy the sim/app/sugarscape directory into another mason/sim/app directory.  Typically, you will also need to copy the [conf,start,ec,sweep] subdirectories, and the MASON Sugarscape also depends on some ECJ classes not supplied with the usual MASON 12 distribution.  

The directories supplied in the distribution are:

sim   -->  all the java source code for both the version of MASON included and the Sugarscape model itself
conf  -->  the .conf files used by the model for various simulation outcomes described in GAS
lib   -->  required .jar library files
ec    -->  ECJ utility java source code required by Sugarscape
start -->  shell scripts and a sample .bat file for executing Sugarscape models
sweep -->  Parameter ranges and steps that are swept given a supplied .conf file in conf/
docs  -->  Acrobat (PDF) files of two papers
supplemental -->  ant build script for compiling and creating mason.jar for MASON and ECJ 

Note that additional files from ECJ in ec/util/ are included in this package that are not in the standard MASON distribution.  These classes are referenced in the MASON tutorials.

The latest bleeding edge source code (Sugarscape only) is available using Subversion (svn) and going to:
http://code.google.com/p/mason-sugarscape/source/list


[3] BUILDING/COMPILING

An Apache Ant build.xml file is supplied which will compile all the MASON core classes as well as the Sugarscape classes.  Ant is very powerful and is the recommended way to build MASON Sugarscape.
Some operating systems, like Mac OS 10.x, include Ant, and it is easily used on the command line.  Simply change to the root MASON Sugarscape directory and execute:

% ant -f build-sugarscapeonly.xml

and this will build a consolidated jar file called sugarscape.jar.
NOTE:  This process depends on having third party jars and mason.jar in lib/.  I have provided an ant script to build mason.jar, supplemental/README_build-masonjar.txt for precise instructions on building MASON 16 and ECJ.

The default task, 'generic_javac_sugarscape', compiles all core MASON classes and all Sugarscape classes.  It does not compile the 3D classes in sim.display3d.*  .  Take a look at the beginning of the file and you will see exactly what is done.  The resulting classes are written to build/ 

You can change to another java compiler instead of the default one your OS configuration has by modifying the generic-build target and adding an executable=/path/to/javac.  See the mac-build target for how this was done for a Mac OS 10.4.x environment.

The Apache Ant project homepage:
http://ant.apache.org/

[4] RUNNING
This section assumes basic comptence with Java JVM invocation and shell or bat scripts.

Out of the box, the simulation outcomes described in GAS are runnable via shell scripts (for Linux/UNIX/Mac OS 10)  whose names match the simulation outcome of interest.  For Windows users, there is a sample .bat file. 

NOTE:  At this moment, only start/anim2-3.sh and start/anim2-2.bat have the correct jar versions specified.  The other scripts need to be updated accordingly. 

You should also be able to run Sugarscape inside an IDE just using the sugarscape.jar and lib/*.jar as the code has been modified to use resources rather files for configuration.

So let's run a model based on one of the earlier outcomes.

1.  cd to the root MASON Sugarscape directory
2.  invoke start/anim2-3.sh (UNIX-based OS) or start/anim2-2.bat (WINDOWS)
3.  press the play button

You'll notice that, for this outcome configuration, output goes to both graphical windows and the console/terminal.

Figure 2-2 (GAS, p. 22) is represented by the executable script start/fig2-2.sh and the configuration file conf/fig2-2.conf .  Some outcomes involved repeated runs and parameter search/sweep, such as Figure 2-5.  In these cases, there are corresponding files in sweep that define:  a) the parameters that will vary across runs, and b) how much each parameter will vary.  Sweeping parameters is directly supported and described later in this document.

The main invocation classes are:

sim.app.sugarscape.SugarscapeWithUIHigh  - Invoke graphical user interface, render resource sites as varying circle sizes, and use anti-aliasing
sim.app.sugarscape.util.ParamSweeper     - Invoke non user interactive model and sweep parameter space as defined in sweep file

In all three cases, one must supply a -file parameter to specify the .conf file.

USING PARAMSWEEPER

It's relatively straight forward to use the parameter sweeper infrastructure, although you have to use the right syntax in the sweep files.  The first thing to understand is the difference between 'run' and 'loop' and 'steps'.  Any particular combination of parameters is called a 'run' and will be executed N times ('loop' s).

When you invoke sim.app.sugarscape.util.ParamSweeper, the first thing it does is construct all possible combinations of parameters, assign a run 'id' to each, and write out each combination to a a .set file, whose prefix is a timestamp based on Java long system time.  This .set file is then used to specify the value for certain parameters in the .conf file for each set of runs.  Those runs are executed N times where N is the value of -loops.  Each of these N executions goes -steps timesteps.  The run id starts with the value given by -id command line parameter.  ParamSweeper produces a .set file whose suffix is System.currentTimeMillis() and enumerates the combination of parameters.

Putting this all together, the command line parameters passed to the JVM (omitting the classpath) in a startup script would be:

java sim.app.sugarscape.util.ParamSweeper -loops 10 -steps 500 -id 1 -file conf/sweep_test.conf conf/sweep_test.sweep

The contents and syntax for the .sweep file is as follows:

The first line is usually a comment line to remind users about the syntax of each line.  Since it's preceded by a '#', it is ignored.
#parameter=startval,endvalue,stepsize

Each line starts with a variable name that matches one of the variables/parameters specified in the corresponding .conf file.
Then an '=' character, then a starting value, then a ending value, then a step size.  Like:

vision_max=1,5,1

So the 'vision_max' parameter in the .conf file and in the simulation runs will start with 1, end with 5, and increase by 1 for every run id.
**WARNING**:  Just because you specify a starting and ending value in your .sweep file does not mean that the average or other measure of central tendency for that parameter will be exactly the same across the population of agents or whatever entity is involved.  The reason is that these parameters are often associated with uniform random distributions and specify the maximum value for that parameter--the minimum value is 1.  In the case of metabolism_sugar as specified above, the fifth value for this parameter, 5, will result in a mean starting value across the agents of 3 when the set of parameters includes a value of 5 for metabolism_sugar.  The actual random assignment looks like:
 
    int value = random.nextint(param_max) + 1;  //random is an instance of ec.util.MersenneTwisterFast

where value is an integer drawn uniformly from 0 to param_max, with one added.

If you don't want parameters in the .conf file to change the starting values, do not include them in the .sweep files.  This means that each .sweep file will typically only be a few lines long.  For Figure 2-5 (GAS, p. 31), it looks like:

#parameter=startval,endvalue,stepsize
vision_max=1,10,1
metabolism_sugar=5,5,1

The resulting .set file has 50 combinations/lines.  Values in the .conf file must be specified as integers.

A full, real instance of using ParamSweeper is available in conf/fig2-5.sh .  

Using the MASON GUI
-------------------
The graphical user interface behaves as you might expect any other MASON model. One special feature is the ability to 'Mark' one or more agents.  Pause the simulation, double click the agent on the display field, and in the inspector window check the 'Mark' box.  An 'M' will be displayed on top of that agent.  To turn off the M display, just uncheck that box.

You can make the simulation run much faster by closing the main display window.  You'll often want to do this because the final simulation values will be of more interest in many cases after 500 or N thousand steps and those are printed on the command line (if specified in the .conf file).

KEY PARAMETERS
Besides parameters specific to things like agent maximum vision and metabolism, there are two classes of parameters that require special explanation.

1.  The specification of rules, associated java classes, and rules-sequences

Agent rules must be specified like:

agent_rule1=Movement,M
agent_rule2=Biological,B

agent_rules are numbered sequentially via integers and coupled to a Java class via an equal '=' characters. For the first line,   The token 'Movement' specifies the exact Java class name (minus the preceding namespace), followed by a ',' character, followed by an abbreviation.

Agent rules are chained together via one and only one line like:
rules_sequence_agents=M,B

'rules_sequence_agents' is required, followed by an '=' character, followed by a comma-separated list of abbreviated rule names as specified in each rule's definition.  In this case, for each agent, rule M will be executed, the rule B.  These rules happen to be a division of the Sugarscape rule M (Movement), where biological processes have been split apart from Movement for convenience and comprehensibility.

Environment rules are specified in the same way:

environment_rule1=DiffuserPollution,D
environment_rule2=Growback,G

And which rules are active are specified like:
rules_sequence_environment=G

In this case, D is specified as an rule, but will not be active/fired because it is not in the environment rules sequence.


2.  Where to output state variables ('statistics'), such as to standard output or whether any JFreeChart graphics should be displayed.

I find it useful, when first testing code or examining outcomes, to have the statistics printed to standard output every time step.  In many of the configuration files, output is directed to standard output with a frequency for every time stamp.  The key parameters are:

#stats_rate = how often should statistics be output
#even a rate of 1 to standard out will not appreciably slow down execution
stats_rate=1

#stats_start=what timestep should stats begin being output
stats_start=1

#stats_out= file or print
stats_out=print

#chart_display=true or false
chart_display=false

#chart_rate=how often to update chart
chart_rate=1

Right now, statistics of interest are hard-coded into Statistics.java, and look like this when output to the console:

run,time,gini,agents_replaced,alive_agents,avg_vision,avg_metabolism_sugar,avg_metabolism_spice
1,-1.0,0.11,0,400,3.43,2.49,0

This is for time step -1, i.e. when everything has been initialized but no Steppables have been executed (except Statistics.java, which itself is a Steppable).  Given that this code itself is Steppable and the code is very simple, it can be easily modified or enhanced.  Investigation into the new charting and other capabilities in MASON 11/12 have not been done.

Charting support is rudimentary and the types are currently hard-coded.  The .conf files do offer the ability to turn on and off specific charts that graph some of the statistics calculated in Statistics.java.
 
By design, all parameters, rules, and rules sequences must be specified in the .conf files -- you cannot change them in the GUI at the initialization point.  The reason is that, although that would be good for demo or other purposes, having to specify what .conf you use and everything concretely described in the .conf file encourages replicability and hopefully reduces experimenter error.  You should always save your .conf files in an historical archive so that you can go back and link any outcome to those settings.

[5] PERFORMANCE EXPECTATIONS

The command line non-gui invocation will run much faster than the GUI version.  Also, the version of Java and which OS (Apple's hardware acceleration implementation varies across JVMs) one uses can have a dramatic effect on performance when using the graphical version.  The graphical version is important to understand certain aspects of the model and is a central feature of Epstein and Axtell's books, but is sometimes superfluous to the simulation outcomes (especially if spatial phenomena are not involved).  I suppose that one could further reduce the graphical usage by implementing some agent-spatial statistics that are output to the command line, though I have not seen this done with Sugarscape implementations.  By default, anti-aliasing is on.  This may or may not make a performance difference across platforms.   It is set programatically in SugarscapeWithUIHigh.init() and is toggled in the GUI via the Display Frame, click Options icon and anti-aliasing checkbox.

The use of JFreeChart in some configurations has a major effect on performance.  I have not taken a close look to determine if it is the nature of accumlated series in JFreeChart that causes performance slowdowns over time or whether I am using it in a suboptimal way.

Some classes/methods have been optimized for performance.  A good scrubbing is in order, however, using profiling tools and improved data structures.  In particular, while Bag objects offer a lot of flexibility and get one off the ground, casual creation and disposal in has performance impacts in tight loops.  In other models, I have used Object or primitive arrays.  When elements need to be added or removed, though, I make use of techniques similar to what is in a Bag methods for dynamic sizing.  This becomes an issue when the order of the array is important -- e.g. you replace a deleted element with the last element in the array so that it doesn't have to be resized.  Randomly shuffling an array is easy to do, see Bag.shuffle() or contact me for a source code snippet.

Performance metrics will vary according to what rules are active in any particular run.  In terms of optimization efforts, focusing on the most common rules (G,M, etc.) will have some benefit.

I have found that the commercial profiling toolkits such as JProfiler and Yourkit are much easier and more informative to use than the standard Java profiling tools.  One exception may be the Sun JFluid plugin (free) for Sun's Netbeans IDE (free).


[6] TROUBLESHOOTING/FAQ

THIS SECTION IS INCOMPLETE.

There are two direct mechanisms for diagnosing behavior and helping to understand the code.
Looking at Agent.java(in particular), you see that there are two boolean members called 'diag' and 'marked.'

If you double click on an agent in the display, these two members show up as check boxes in the inspector window.

diag = Diagnostic.  I output various diagnostic things to standard out if this is true/checked.  I used this a lot to try to figure out why I had different results in my Trade rule implementation.

marked = Marked by the user for visual tracking.  If you checked/true, the renderer for agents will an 'M' over the agent so that you can follow one or more around the landscape.


[7] FURTHER INFORMATION

Assumptions about the Sugarscape model, interpretation of Growing Artificial Societies, and explanations about data structures, simulation outcomes and other relevant information are available in two papers in pdf format:

  docs/ajb_thesis_revised.pdf
  docs/draft_SugarscapeMASON5_v2.pdf   

  1) The original M.A. thesis by Tony Bigbee while a student at George Mason University (May 2005).  See Table 3 (p. 34) and Table 2 (p. 18) as starting points.
  2) A draft extending a recent conference paper that summarizes the thesis and extends some of the ideas regarding social sciences model replication.  This paper also has a better space-time diagram for examining the wave phenomenon.


[8] FUTURE RESEARCH,STUDENT PROJECTS

Here are some things that could and should be done to improve or explore this implementation or advance research in general.  I estimate time required based on a Bachelor's degree, general Java competence, and focused effort (i.e. several hours a day).

0.  Refactor Sugarscape.java.  It is unnecessarily large. Maybe Agent.java as well.  [1-2 weeks]
1.  Implement Chapter 5, Disease Processes. [1-3 weeks]
2.  Implement Social Network statistics and visualization.  MASON has an adjunct module that would probably be good place to use. [2-4 weeks]
3.  Implement Foresight, Credit (GAS, p. 129-135). [2-4 weeks] 
4.  Implement Ringworld (GAS, p. 170-176). [1-3 weeks]
5.  Explore in much greater depth differences between the original Sugarscape source code, ASCAPE, and MASON Sugarscape.  Account for all simulation outcome differences between the original Sugarscape and MASON Sugarscape.  [1-3 months]
6.  Generalize the use of JFreeChart to any state value or combination of values. [1-3 months]
7.  Generalize the number of resources and welfare estimate to N. [1-2 months]
8.  Implement the Combat rule (GAS, p. 82-92). [2-4 weeks]
9.  Explore the differences in simulation outcomes between different scheduling metaphors. [1-3 months]
  o see a draft of the latest MASON Sugarscape paper for a description of these
10.  Implement fractal dimension measurement support to try to detect differences in state variable changes when source code or parameters are changed.  This should help understand how emergent features are linked to source code and parameters in a novel and perhaps very useful way.  See: http://citeseer.ist.psu.edu/jr00fast.html  [2-4 weeks]

----End of README----
