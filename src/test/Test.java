package test;

import static java.lang.Math.*;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nodamushi.jfx.chart.linechart.AxisZoomHandler;
import nodamushi.jfx.chart.linechart.GraphTracker;
import nodamushi.jfx.chart.linechart.Legend;
import nodamushi.jfx.chart.linechart.LineChart;
import nodamushi.jfx.chart.linechart.LineChartData;
import nodamushi.jfx.chart.linechart.LinearAxis;
/**
 * NLineChartのテスト
 * @author nodamushi
 *
 */
public class Test extends Application{
  //x軸方向に連続なデータか、y軸方向に連続なデータかを変更するフラグ
  private static final boolean testModeX=true;

  public static void main(final String[] args){
    launch(args);
  }

  @Override
  public void start(final Stage stage) throws Exception{
    final LinearAxis axis = new LinearAxis();
    final LinearAxis yaxis = new LinearAxis();
    axis.setName("ももも");
    yaxis.setName("ままま");
    final AxisZoomHandler zoom = new AxisZoomHandler();
    zoom.install(axis);
    zoom.install(yaxis);
    final LineChart c = new LineChart();
    c.setXAxis(axis);
    c.setYAxis(yaxis);
    final ObservableList<LineChartData> datas = c.getDataList();
    final LineChartData data = new LineChartData(200);
    final LineChartData data2 = new LineChartData(200);
    //sinc関数を表示してみる
    for(int i=0;i<200;i++){
      final double x = (i-100)*0.1;
      //x = 0は本当は1だけど、無限のテストもかねて
      final double y = x==0? Double.POSITIVE_INFINITY:sin(x)/x;
      final double y2 = cos(x);
      if(testModeX){
        data.addData(x, y);
        data2.addData(x, y2);
      }else{
        data.addData(y, x);
        data2.addData(y2, x);
      }
    }
    data.setName("sin(x)/x");
    data2.setName("cos(x)");
    datas.addAll(data,data2);

    if(!testModeX) {
      c.setOrientation(Orientation.VERTICAL);
    }

    if(testModeX){
      c.setRangeMarginX(1);
    }else{
      c.setRangeMarginY(1);
    }

    final GraphTracker traker = new GraphTracker();
    traker.install(c);


    final Legend legend = new Legend();
    legend.setDataList(c.getDataList());

    final BorderPane p = new BorderPane();
    p.setTop(legend);
    p.setPrefWidth(600);
    p.setPrefHeight(400);
    p.setCenter(c);
    p.setStyle("-fx-padding:50");
    final Scene s = new Scene(p);
    stage.setScene(s);
    stage.show();
  }
}
