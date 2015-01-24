package test;

import static java.lang.Math.*;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nodamushi.jfx.chart.linechart.AxisZoomHandler;
import nodamushi.jfx.chart.linechart.LineChart;
import nodamushi.jfx.chart.linechart.LineChartData;
import nodamushi.jfx.chart.linechart.LinerAxis;

/**
 * 大量データのレンダリングテスト
 * @author nodamushi
 *
 */
public class Test5 extends Application{
  public static void main(final String[] args){
    launch(args);
  }

  @Override
  public void start(final Stage stage) throws Exception{
    final LinerAxis axis = new LinerAxis();
    final LinerAxis yaxis = new LinerAxis();
    final AxisZoomHandler zoom = new AxisZoomHandler();

    zoom.install(axis);
    zoom.install(yaxis);
    final LineChart c = new LineChart();
    c.setRangeMarginX(1);
    c.setXAxis(axis);
    c.setYAxis(yaxis);
    final ObservableList<LineChartData> datas = c.getDataList();
    final int datasize = 100000;
    final LineChartData data = new LineChartData(datasize);

    for(int i=0;i<datasize;i++){
      final double x = 0.1*i;
      final double y = sin(x);
      data.addData(x, y);
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
