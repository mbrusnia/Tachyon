package org.fhcrc.optides.apps.UPLCPeakTracerApp;

/**
 * Created by mbrusnia on 8/13/18.
 */
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.ArrayList;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class XYLineChart_AWT extends ApplicationFrame {
    private JFreeChart xylineChart;

    public XYLineChart_AWT(String applicationTitle, String chartTitle, double defaultYmax,
                           ArrayList<HPLCPeakComparable> list,
                           ArrayList<HPLCPeakComparable> peaks) {
        super(applicationTitle);
        xylineChart = ChartFactory.createXYLineChart(chartTitle, "rt", "mAu",
                createDataset(list, peaks, defaultYmax), PlotOrientation.VERTICAL, true,
                true, false);

        ChartPanel chartPanel = new ChartPanel(xylineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        final XYPlot plot = xylineChart.getXYPlot();

        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);
        plot.setDomainGridlinePaint(Color.black);
        ValueAxis rangeY = new NumberAxis("mAU (280nm wavelength)");
        rangeY.setRange(0.0, defaultYmax);
        plot.setRangeAxis(0, rangeY);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.RED);
        renderer.setSeriesPaint(2, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(0.01f));
        plot.getRenderer().setSeriesStroke(
                0,
                new BasicStroke(2.0f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 1.0f,
                        new float[] { 6.0f, 6.0f }, 0.0f));
        renderer.setSeriesStroke(2, new BasicStroke(2.0f));
        plot.setRenderer(renderer);
        setContentPane(chartPanel);
    }

    public JFreeChart getChart() {
        return xylineChart;
    }

    private XYDataset createDataset(ArrayList<HPLCPeakComparable> list,
                                    ArrayList<HPLCPeakComparable> peaks, double defaultYmax ) {
        final XYSeries plot = new XYSeries("Current Sample");
        final XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < list.size(); i++) {
            plot.add(list.get(i).getRt(), list.get(i).getAu() * 1000);
        }
        double standardYmax = defaultYmax - 1.0;
        for (int i = 0; i < 5; i++) {
            final XYSeries chrome = new XYSeries(peaks.get(i).getAu() * 100);
            chrome.add(peaks.get(i).getRt(), 0);
            chrome.add(peaks.get(i).getRt(), standardYmax);
            dataset.addSeries(chrome);
        }
        dataset.addSeries(plot);
        return dataset;
    }
}
