/*
 * DataSetLinePlot.java
 *
 * Created on 12 Οκτώβριος 2007, 4:07 μμ
 *
 */

package com.panayotis.gnuplot.plot;

import com.panayotis.gnuplot.dataset.ArrayDataSet;
import com.panayotis.gnuplot.dataset.DataSet;
import com.panayotis.gnuplot.dataset.PointDataSet;


/**
 * This plot uses data sets as coordinates of the points to e displayed. The user
 * can provide data either statically (through the specialized constructors with
 * native base types) or with the more flexible generic object PointDataSet.
 * @author teras
 */
public class DataSetLinePlot extends DataSetPlot {
    protected int pointsPerLine;
    /**
     * Create a new data set with a default data set.
     */
    public DataSetLinePlot(int ppl) {
      super();
      assert (ppl > 0);
      pointsPerLine = ppl;
    }

    /**
     * Create a new data set with the specified double-precision array as a data set
     * @param dataset A 2D double table with the data set
     */
    public DataSetLinePlot(int ppl, double[][] dataset)
    {
      super(dataset);
      assert (ppl > 0);
      pointsPerLine = ppl;
    }

    /**
     * Create a new data set with the specified float-precision array as a data set
     * @param dataset A 2D float table with the data set
     */
    public DataSetLinePlot(int ppl, float[][] dataset)
    {
      super(dataset);
      assert (ppl > 0);
      pointsPerLine = ppl;
    }

    /**
     * Create a new data set with the specified int-precision array as a data set
     * @param dataset A 2D int table with the data set
     */
    public DataSetLinePlot(int ppl, int[][] dataset)
    {
      super(dataset);
      assert (ppl > 0);
      pointsPerLine = ppl;
    }

    /**
     * Create a new data set with the specified long-precision array as a data set
     * @param dataset A 2D long table with the data set
     */
    public DataSetLinePlot(int ppl, long[][] dataset)
    {
      super(dataset);
      assert (ppl > 0);
      pointsPerLine = ppl;
    }

    /**
     * Create a new object with a specific data set
     * @param dataset The data set to use
     */
    public DataSetLinePlot(int ppl, DataSet dataset)
    {
      super(dataset);
      assert (ppl > 0);
      pointsPerLine = ppl;
    }

    /**
     * Retrieve the data set of this plot command.
     * It is used internally by JavaPlot library.
     * @param bf The buffer to store the data set
     */
    public void retrieveData(StringBuffer bf) {
      int i1, i2, j;
      int isize, jsize;
      
      if (dataset!=null) {
        isize = dataset.size();
        jsize = dataset.getDimensions();
        assert(isize % pointsPerLine == 0);
        bf.append(NL);
        for (i1 = 0; i1 < isize; i1 += pointsPerLine) {
          // this funky arrangement should draw a line between each pair of points
          for (i2 = 0; i2 < pointsPerLine; i2++) {
            for (j = 0; j < jsize; j++) {
              bf.append(dataset.getPointValue(i1+i2, j)).append(' ');
            }
            bf.append(NL);
          }
          bf.append(NL);
        }
      }
      bf.append("e").append(NL);
    }
}
