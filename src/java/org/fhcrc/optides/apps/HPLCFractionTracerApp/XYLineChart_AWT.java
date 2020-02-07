package org.fhcrc.optides.apps.HPLCFractionTracerApp;
import java.awt.*;
import java.util.ArrayList;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.*;
import org.jfree.chart.ChartFactory;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;


public class XYLineChart_AWT extends ApplicationFrame {
    private JFreeChart xylineChart;

    public XYLineChart_AWT(String applicationTitle, String chartTitle, double defaultYmax,
                           ArrayList<HPLCFractionPeakComparable> chromatogram,
                           ArrayList<FractionTime> fractions) {
        super(applicationTitle);
        xylineChart = ChartFactory.createXYLineChart(chartTitle, "rt", "mAu",
                createDataset(chromatogram, defaultYmax), PlotOrientation.VERTICAL, true,
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
        renderer.setSeriesStroke(0, new BasicStroke(0.01f));
        for(int i = 0; i < fractions.size(); ++i ){
            Marker fractionPaint = new IntervalMarker(fractions.get(i).getStart_rt(), fractions.get(i).getEnd_rt());
            fractionPaint.setPaint(new Color(255, 175, 51));
            plot.addDomainMarker(fractionPaint, Layer.BACKGROUND);
            Marker coolingStart = new ValueMarker(fractions.get(i).getStart_rt(), Color.red,
                    new BasicStroke(1.0f));
            Marker coolingEnd = new ValueMarker(fractions.get(i).getEnd_rt(), Color.red,
                    new BasicStroke(1.0f));
            plot.addDomainMarker(coolingStart, Layer.BACKGROUND);
            plot.addDomainMarker(coolingEnd, Layer.BACKGROUND);
        }
        plot.setRenderer(renderer);
        setContentPane(chartPanel);
    }

    public JFreeChart getChart() {
        return xylineChart;
    }

    private XYDataset createDataset(ArrayList<HPLCFractionPeakComparable> list, double defaultYmax ) {
        final XYSeries plot = new XYSeries("HPLC Trace");
        final XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < list.size(); i++) {
            plot.add(list.get(i).getRt(), list.get(i).getAu());
        }
        dataset.addSeries(plot);
        return dataset;
    }
}
