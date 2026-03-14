package com.realkarim.ramesses;

import com.realkarim.ramesses.domain.model.MarketBar;
import com.realkarim.ramesses.util.BinanceHistoricalFetcher;
import java.text.SimpleDateFormat;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;

class RamessesApplicationTests {

    public static void main(String... args) {
        var historicalBars = new BinanceHistoricalFetcher().fetchHistoricalBars();
        var series = toBarSeries(historicalBars);

        var closePrice = new ClosePriceIndicator(series);
        var avg20 = new SMAIndicator(closePrice, 20);
        var sd = new StandardDeviationIndicator(avg20, 3);

        var middleBBand = new BollingerBandsMiddleIndicator(avg20);
        var lowBBand = new BollingerBandsLowerIndicator(middleBBand, sd);
        var upBBand = new BollingerBandsUpperIndicator(middleBBand, sd);

        var dataset = new TimeSeriesCollection();
        dataset.addSeries(buildChartSeries(series, closePrice, "ETH"));
        dataset.addSeries(buildChartSeries(series, lowBBand, "Low Bollinger Band"));
        dataset.addSeries(buildChartSeries(series, middleBBand, "Middle Bollinger Band"));
        dataset.addSeries(buildChartSeries(series, upBBand, "High Bollinger Band"));

        JFreeChart chart = ChartFactory.createTimeSeriesChart("ETH Close Prices",
            "Date", "Price Per Unit", dataset, true, true, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        ((DateAxis) plot.getDomainAxis()).setDateFormatOverride(
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        displayChart(chart);
    }

    private static BarSeries toBarSeries(List<MarketBar> bars) {
        var series = new BaseBarSeriesBuilder().withName("CHART").build();
        for (var bar : bars) {
            series.addBar(bar.getTimestamp(), bar.getOpen(), bar.getHigh(),
                bar.getLow(), bar.getClose(), bar.getVolume());
        }
        return series;
    }

    private static void displayChart(JFreeChart chart) {
        var panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));
        var frame = new ApplicationFrame("Ta4j example - Indicators to chart");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    private static org.jfree.data.time.TimeSeries buildChartSeries(
        BarSeries barSeries, Indicator<Num> indicator, String name) {
        var chartSeries = new org.jfree.data.time.TimeSeries(name);
        for (int i = 0; i < barSeries.getBarCount(); i++) {
            var bar = barSeries.getBar(i);
            chartSeries.addOrUpdate(
                new Millisecond(
                    bar.getEndTime().getNano() / 1000000,
                    bar.getEndTime().getSecond(),
                    bar.getEndTime().getMinute(),
                    bar.getEndTime().getHour(),
                    bar.getEndTime().getDayOfMonth(),
                    bar.getEndTime().getMonthValue(),
                    bar.getEndTime().getYear()),
                indicator.getValue(i).doubleValue());
        }
        return chartSeries;
    }
}
