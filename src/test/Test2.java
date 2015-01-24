package test;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nodamushi.jfx.chart.linechart.LineChartData;
import nodamushi.jfx.chart.linechart.LinerAxis;
import nodamushi.jfx.chart.linechart.LogarithmicAxis;
import nodamushi.jfx.chart.linechart.LineChart;

/**
 * 対数グラフのテスト
 * @author nodamushi
 *
 */
public class Test2 extends Application{
  public static void main(final String[] args){
    launch(args);
  }

  @Override
  public void start(final Stage stage) throws Exception{
    final LinerAxis axis = new LinerAxis();
    final LogarithmicAxis yaxis = new LogarithmicAxis();
    final LineChart c = new LineChart();
    c.setHorizontalMinorGridLinesVisible(true);
    c.setRangeMarginX(1);
    c.setXAxis(axis);
    c.setYAxis(yaxis);
    axis.setLowerValue(0);
    axis.setVisibleAmount(0.5);
//    yaxis.setLowerValue(100);
//    yaxis.setVisibleAmount(0.5);
    final ObservableList<LineChartData> datas = c.getDataList();
    final LineChartData data = new LineChartData(200);
    //e^x
    for(int i=0;i<200;i++){
      final double y = i*i;
      data.addData(i, y);
    }
    datas.add(data);

    final BorderPane p = new BorderPane();
    p.setPrefWidth(600);
    p.setPrefHeight(400);
    p.setCenter(c);
    p.setStyle("-fx-padding:50");
    final Scene s = new Scene(p);
    stage.setScene(s);
    stage.show();
  }
}
