package sim.app.sugarscape.util;

/*
Copyright 2006 by Anthony Bigbee
Licensed under the Academic Free License version 3.0
See the file "LICENSE" for more information
*/

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.SparseGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.app.sugarscape.Sugarscape;
import sim.util.Bag;
import sim.util.Int2D;

import java.util.Hashtable;
import java.util.Vector;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class Logger implements Steppable, Runnable {

    public Bag grids;         //of grids
    public Bag ad_hoc_objects;      //of Bags
    public Hashtable objects_hash;  //index of ad_hoc_objects
    private Vector queue;
    private boolean done;
    private BufferedWriter buf;
    private double current_step;
    public Logger() {
       done = false;
       queue = new Vector(100000);
       grids = new Bag(10);   //types of grids
       ad_hoc_objects = new Bag(10); //types of objects, like agents
       //set up file and buffer
       try {
       File f = new File ("log."+System.currentTimeMillis());
       FileWriter fw = new FileWriter(f);
       buf = new BufferedWriter(fw);
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    public void addObject(Object o) {
       //get the type, and add to the right list

       if (o instanceof Field) {
           Field field = (Field)o;
           //System.out.println("Logger:  o instanceof Field");
           //System.out.println(field.getType().getName()+ " = logged field type name");
           if ( field.getType().getName().endsWith("SparseGrid2D")) {
               //System.out.println("added SparseGrid2D to logged classes...");
               addSparseGrid2D(o);
           } else if (field.getType().getName().endsWith("ObjectGrid2D")) {
               addObjectGrid2D(o);
           }
       }

    }


    public void addSparseGrid2D(Object grid) {
        grids.add(grid);
    }

    public void addObjectGrid2D(Object grid) {
        grids.add(grid);
    }

    public void done() {
        done = true;
    }
    //System.out.println(" ");

    public void push (String line) {
       queue.add(line);
    }


    public String pop() {
        //get the next line of output and write it
        if (queue.size()!=0) {
            return ((String)queue.remove(0));
        } else return null;
    }

    public void step (SimState state) {
      //iterate through each grid
      //get the objects in the grid
      //get the attributes for each object, format them for output
      //then pass to the filewriting queue handled by a separate thread
      int size = grids.size();
      Sugarscape sugar = (Sugarscape)state;
      current_step = sugar.schedule.getTime();
      for (int a = 0; a < size; a++) {

          Field field = (Field)grids.get(a);

          try {
              //System.out.println("Log step: "+  field.getName());
              if (field.getType().getName().endsWith("SparseGrid2D")) { //hardcode the class permanently???
                  SparseGrid2D inst = (SparseGrid2D)field.get(sugar);
                  Method m = field.getType().getMethod("getAllObjects", (Class[]) null);
                  outputSparseGrid2DObjects( inst, (Bag)m.invoke(inst, (Object[]) null));
              } else if (field.getType().getName().endsWith("ObjectGrid2D")) { //hardcode the class permanently???
                  ObjectGrid2D inst = (ObjectGrid2D)field.get(sugar);
                  outputObjectGrid2DObjects( inst, inst.field);
              }
          } catch (Exception e) {
              e.printStackTrace();
          }
      }
    }

    private void outputSparseGrid2DObjects(SparseGrid2D grid, Bag allobjs) {
       Int2D pos;
       try {
           //Bag all = grid.getAllObjects();
           int size = allobjs.size();
           if (allobjs.size()==0) {
               return;
           }
           StringBuffer vals = new StringBuffer(100);
           Object o = allobjs.get(0);
           Class c = o.getClass();
           Method[] methods = c.getDeclaredMethods();
           int meth_size = methods.length;
           int new_size = 0;
           Method[] get_methods = new Method[meth_size];
           for (int mm = 0; mm < meth_size; mm++) {
               if (methods[mm].getName().startsWith("get")) {
                   get_methods[new_size] = methods[mm];
                   vals.append(methods[mm].getName()+"\t");
                   new_size++;
               }
           }
           vals.append("\n");
           push(vals.toString());
           for (int a = 0; a<size; a++ ) {
               o = allobjs.get(a);
               vals.delete(0,vals.length());
               vals.append(current_step);
               vals.append("\t");
               vals.append(o.toString());
               vals.append("\t");
               //since we're dealing with grids, get the position of each object
               pos = grid.getObjectLocation(o);
               vals.append(pos.x+"\t"+pos.y+"\t");
               //access all the inspector methods.  Usually "get...()"
               for (int b = 0; b < new_size; b++) {
                  vals.append(get_methods[b].invoke(o, (Object[])null));
                  vals.append("\t");
               }
               vals.append("\n");
               push(vals.toString());
           }

       } catch (Exception e) {
           e.printStackTrace();
       }

    }

    private void outputObjectGrid2DObjects(ObjectGrid2D grid, Object[][] allobjs) {
       
       try {
           //Bag all = grid.getAllObjects();
           int x_size = grid.getHeight();
           int y_size = grid.getWidth();


           StringBuffer vals = new StringBuffer(100);
           Object o = allobjs[0][0]; //pray there is always an object here
           Class c = o.getClass();
           Method[] methods = c.getDeclaredMethods();
           int meth_size = methods.length;
           int new_size = 0;
           Method[] get_methods = new Method[meth_size];
           for (int mm = 0; mm < meth_size; mm++) {
               if (methods[mm].getName().startsWith("get")) {
                   get_methods[new_size] = methods[mm];
                   vals.append(methods[mm].getName()+"\t");
                   new_size++;
               }
           }
           vals.append("\n");
           push(vals.toString()) ;
           for (int x = 0; x <x_size; x++ ) {
               for (int y = 0; y < y_size; y++) {
                   o = allobjs[x][y];
                   if (o!=null) {
                       vals.delete(0,vals.length());
                       //since we're dealing with grids, get the position of each object
                       vals.append(current_step);
                       vals.append("\t");
                       vals.append(o.toString());
                       vals.append("\t");
                       vals.append(x+"\t"+y+"\t");
                       //access all the inspector methods.  Usually "get...()"
                       for (int b = 0; b < new_size; b++) {
                          vals.append(get_methods[b].invoke(o, (Object[])null));
                          vals.append("\t");
                       }
                       vals.append("\n");
                       push(vals.toString());
                   }
                }
           }

       } catch (Exception e) {
           e.printStackTrace();
       }

    }


    public void run () {
       String line;
       try {
           while (!done) {
              while ( (line=pop())!=null) {
                 if (buf!=null) {
                    buf.write(line);
                 }
              }
              Thread.sleep(100);
           }
           buf.close();

       } catch (Exception e) {
            e.printStackTrace();
       }
    }

}
