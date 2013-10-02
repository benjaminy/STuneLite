/*
 *
 */

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashMap;
import java.util.logging.Logger;

public class CSVReader
{
  protected TuningKnobSearchProperties props = null;
  protected Logger l = null;
  protected Iterable<String> lines = null;
  protected Map<String, Integer> headings = null;
  protected int width = 0, height = 0;
  protected String table[][] = null;

  public CSVReader(TuningKnobSearchProperties p, Iterable<String> ls)
  {
    assert (p != null);
    assert (ls != null);
    props = p;
    l = Logger.getLogger(props.defaultLoggerName);
    lines = ls;
    headings = new HashMap<String, Integer>();

    List<List<String>> rawTable = new LinkedList<List<String>>();
    for (String line : lines) {
      String cells[] = (new String(line)).split(",");
      List<String> rawRow = new LinkedList<String>();
      rawTable.add(rawRow);
      int rowLength = 0;
      for (String cell : cells) {
        rawRow.add(cell.trim());
        rowLength++;
      }
      width = Math.max(width, rowLength);
      height++;
    }

    table = new String[height][width];
    int rowIdx = 0;
    for (List<String> rawRow : rawTable) {
      int colIdx = 0;
      for (String cell : rawRow) {
        table[rowIdx][colIdx] = cell;
        colIdx++;
      }
      rowIdx++;
    }

    for (int colIdx = 0; colIdx < width; colIdx++) {
      if (table[0][colIdx] != null) {
        String heading = table[0][colIdx].toUpperCase();
        if (headings.containsKey(heading)) {
          l.warning("Redundant heading in CSV file: \""+heading+"\"");
        }
        headings.put(heading, colIdx);
      }
    }
  }

  public int getNumberOfRows()
  {
    return height - 1;
  }

  public Integer getHeadingColumnIdx(String heading)
  {
    return headings.get(heading.toUpperCase());
  }

  public String getCell(int i, int j)
  {
    return table[i+1][j];
  }
}
