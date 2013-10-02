/*
 *
 */

import java.lang.Iterable;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class MiscUtils
{
  
  public static int readLinesFromCommand(String command[], List<String> buffer)
  {
    if (command == null || buffer == null) {
      return -2;
    }
    Process cmdProc = null;
    try {
      cmdProc = Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      System.out.printf("`%s` failed\n", Arrays.toString(command));
      e.printStackTrace();
      return -1;
    }
    BufferedReader cmdOutput = new BufferedReader(new InputStreamReader(cmdProc.getInputStream()));
    int returnCode = -1;
    try { returnCode = cmdProc.waitFor(); }
    catch (InterruptedException e) {
      System.out.printf("Interrupted while waiting for `%s`\n", Arrays.toString(command));
      e.printStackTrace();
      return -1;
    }
    String line = null;
    try { line = cmdOutput.readLine(); }
    catch (IOException e) {
      System.out.printf("reading `%s` output failed\n", Arrays.toString(command));
      return -1;
    }
    while (line != null) {
      buffer.add(line);
      try { line = cmdOutput.readLine(); }
      catch (IOException e) {
        System.out.printf("reading `%s` output failed\n", Arrays.toString(command));
        return -1;
      }
    }
    if (cmdProc != null) {
      boolean waited = false;
      while (!waited) {
        try { cmdProc.waitFor(); waited = true; }
        catch (InterruptedException e) { }
      }
      if (cmdProc != null) {
        close(cmdProc.getOutputStream());
        close(cmdProc.getInputStream());
        close(cmdProc.getErrorStream());
        cmdProc.destroy();      
      }
    }
    return returnCode;
  }

  public static String readLineFromCommand(String command[])
  {
    List<String> lines = new LinkedList<String>();
    int exitCode = readLinesFromCommand(command, lines);
    if (lines.isEmpty()) {
      return null;
    }
    return lines.get(0);
  }

  private static class OutputDumper implements Runnable
  {
    BufferedReader r;
    String p;
    PrintStream outStream = null;

    public OutputDumper(String prefix, BufferedReader reader, PrintStream s)
    {
      p = prefix;
      r = reader;
      outStream = s;
    }

    public void run()
    {
      try {
        String line = r.readLine();
        while (line != null) {
          if (outStream != null) { outStream.printf("%s%s\n", p, line); }
          line = r.readLine();
        }
        if (false) {System.out.printf("%sEnd of output stream %s reached\n", p, r);}
      }
      catch (IOException e) {
        System.out.printf("%sIOException caught from stream %s\n", p, r);
      }
    }
  }

  public static int execAndWait(String[] cmdarray, String[] envp, File dir, PrintStream outStream)
  {
    if (outStream != null) {
      outStream.printf("About to exec %s\n", Arrays.toString(cmdarray));
    }
    Process cmdProc = null;
    try {
      if (dir == null) {
        cmdProc = Runtime.getRuntime().exec(cmdarray, envp);
      }
      else {
        cmdProc = Runtime.getRuntime().exec(cmdarray, envp, dir);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.out.printf("`%s` failed\n", cmdarray[0]);
      return -1;
    }
    BufferedReader cmdOutput = new BufferedReader(new InputStreamReader(cmdProc.getInputStream()));
    Thread stdOutThread = new Thread(new OutputDumper(". ", cmdOutput, outStream));
    stdOutThread.start();
    BufferedReader cmdError  = new BufferedReader(new InputStreamReader(cmdProc.getErrorStream()));
    Thread stdErrThread = new Thread(new OutputDumper("! ", cmdError, outStream));
    stdErrThread.start();
    int returnCode = -1;
    try {
      // System.out.printf("About to start waiting for %s\n", cmdarray[0]);
      System.out.flush();
      returnCode = cmdProc.waitFor();
      // System.out.printf("Done waiting for %s\n", cmdarray[0]);
    }
    catch (InterruptedException e) {
      System.out.printf("Interrupted while waiting for `%s`\n", cmdarray[0]);
    }

    boolean joined = false;
    while (!joined) {
      try {
        stdOutThread.join();
        stdErrThread.join();
        joined = true;
      }
      catch (InterruptedException e) {}
    }
    
    if (outStream != null) {
      outStream.printf("Exit code: %d\n", returnCode);
    }
    if (cmdProc != null) {
      boolean waited = false;
      while (!waited) {
        try { cmdProc.waitFor(); waited = true; }
        catch (InterruptedException e) { }
      }
      if (cmdProc != null) {
        close(cmdProc.getOutputStream());
        close(cmdProc.getInputStream());
        close(cmdProc.getErrorStream());
        cmdProc.destroy();      
      }
    }
    return returnCode;
  }

  public static class IterAdaptor <T> implements Iterator<T>
  {
    Iterator i = null;
    public IterAdaptor(Iterator it) { i = it; }
    public boolean hasNext()  { return i.hasNext(); }
    public T next()           { return (T)i.next(); }
    public void remove()      { i.remove(); }
  }

  public static class IAdaptor <T> implements Iterable<T>
  {
    IterAdaptor<T> i = null;
    public IAdaptor(Iterator it) { i = new IterAdaptor<T>(it); }
    public Iterator<T> iterator() { return i; }
  }

  private static void close(Closeable c) {
    if (c != null) {
      try { c.close(); }
      catch (IOException e) {
        // ignored
      }
    }
  }
}
