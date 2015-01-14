package test;

import static java.lang.Math.*;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nodamushi.jfx.chart.linechart.Graph;
import nodamushi.jfx.chart.linechart.LineChartData;
import nodamushi.jfx.chart.linechart.LinerAxis;
import nodamushi.jfx.chart.linechart.VGraphs;

public class Test4 extends Application{
  public static void main(final String[] args){
    launch(args);
  }

  @Override
  public void start(final Stage stage) throws Exception{
    Graph g1,g2;
    {
      final LinerAxis axis = new LinerAxis();
      final LinerAxis yaxis = new LinerAxis();
      yaxis.setSide(Side.RIGHT);
      axis.setSide(Side.TOP);
      final Graph c = new Graph();
      c.setTitle("sinc(x)   (*x=0 y=infinity)");
      c.setRangeMarginX(1);
      c.setXAxis(axis);
      c.setYAxis(yaxis);
      //    axis.setLowerValue(0);
      //    axis.setVisibleAmount(0.3);
      //    yaxis.setLowerValue(0);
      //    yaxis.setVisibleAmount(0.3);
      final ObservableList<LineChartData> datas = c.getDatas();
      final LineChartData data = new LineChartData(200);
      //sinc関数を表示してみる
      for(int i=0;i<200;i++){
        final double x = (i-100)*0.1;
        //x = 0は本当は1だけど、無限のテストもかねて
        final double y = x==0? Double.POSITIVE_INFINITY:sin(x)/x;

        data.addData(x, y);
      }
      datas.add(data);
      g1=c;
    }
    {
      final LinerAxis axis = new LinerAxis();
      final LinerAxis yaxis = new LinerAxis();
      final Graph c = new Graph();
      c.setTitle("sinc(x) * 1000   (*x=0 y=infinity)");
      c.setRangeMarginX(1);
      c.setXAxis(axis);
      c.setYAxis(yaxis);
      //    axis.setLowerValue(0);
      //    axis.setVisibleAmount(0.3);
      //    yaxis.setLowerValue(0);
      //    yaxis.setVisibleAmount(0.3);
      final ObservableList<LineChartData> datas = c.getDatas();
      final LineChartData data = new LineChartData(200);
      //sinc関数を表示してみる
      for(int i=0;i<200;i++){
        final double x = (i-100)*0.1;
        //x = 0は本当は1だけど、無限のテストもかねて
        final double y = x==0? Double.POSITIVE_INFINITY:sin(x)/x * 1000;

        data.addData(x, y);
      }
      datas.add(data);
      g2=c;
    }
    final BorderPane p = new BorderPane();
    p.setPrefWidth(600);
    p.setPrefHeight(400);
    final VGraphs c = new VGraphs();
    c.getGraphs().addAll(g1,g2);
    p.setCenter(c);
    p.setStyle("-fx-padding:50");
    final Scene s = new Scene(p);
    stage.setScene(s);
    stage.show();
  }
}