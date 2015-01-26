package test;

import static java.lang.Math.*;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
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
  //x軸方向に連続なデータか、y軸方向に連続なデータかを変更するフラグ
  private static final boolean testModeX=false;
  private static final int datasize = 100000;

  public static void main(final String[] args){
    launch(args);
  }

  @Override
  public void start(final Stage stage) throws Exception{
    final LinerAxis axis = new LinerAxis();
    final LinerAxis yaxis = new LinerAxis();
    axis.setName("ももも");
    yaxis.setName("まままfasdfasdfadsfasdfdas");
    final AxisZoomHandler zoom = new AxisZoomHandler();
    zoom.install(axis);
    zoom.install(yaxis);
    final LineChart c = new LineChart();
    c.setXAxis(axis);
    c.setYAxis(yaxis);
    final ObservableList<LineChartData> datas = c.getDataList();


    final LineChartData data = new LineChartData(datasize);
    //sinc関数を表示してみる
    for(int i=0;i<datasize;i++){
      final double x = i*0.1;
      final double y = sin(x);

      if(testModeX){
        data.addData(x, y);
      }else{
        data.addData(y, x);
      }
    }
    datas.add(data);

    if(!testModeX) {
      c.setOrientation(Orientation.VERTICAL);
    }

    if(testModeX){
      c.setRangeMarginX(1);
    }else{
      c.setRangeMarginY(1);
    }

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
