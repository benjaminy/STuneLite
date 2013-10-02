/*
 *
 */

import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;
import java.util.Iterator;

public class BufferedReaderIterable extends BufferedReader implements Iterable<String> {
  private Iterator<String> i;

  public BufferedReaderIterable(Reader in)
  {
    super(in);
    i = new BufferedReaderIterator();
  }
  
  public BufferedReaderIterable(Reader in, int sz)
  {
    super(in, sz);
    i = new BufferedReaderIterator();
  }
  
  public Iterator<String> iterator() {
    return i;
  }

  private class BufferedReaderIterator implements Iterator<String> {
    private String line;

    public BufferedReaderIterator() {
      advance();
    }

    public boolean hasNext() {
      return line != null;
    }

    public String next() {
      String retval = line;
      advance();
      return retval;
    }

    public void remove() {
      throw new UnsupportedOperationException("Remove not supported on BufferedReader iteration.");
    }

    private void advance() {
      line = null;
      try {
        line = readLine();
      }
      catch (IOException e) {
        System.err.printf("\nA BufferedReaderIterable IOException in advance\n");
      }
      if ( line == null ) {
        try {
          close();
        }
        catch (IOException e) {
          System.err.printf("\nA BufferedReaderIterable IOException on close\n");
        }
      }
    }
  }
}
