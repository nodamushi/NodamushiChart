package test;

import static java.lang.Math.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import nodamushi.jfx.chart.linechart.LineChartData;
import nodamushi.jfx.chart.linechart.LinerAxis;
import nodamushi.jfx.chart.linechart.LineChart;
/**
 * NLineChartのテスト
 * @author nodamushi
 *
 */
public class Test3 extends Application{
  public static void main(final String[] args){
    launch(args);
  }
  private double[] x = new double[200];
  private double[] data = new double[200];
  private double[] data2 = new double[200];
  private LineChartData lcd,lcd2;
  private double phase = 0;
  @Override
  public void start(final Stage stage) throws Exception{
    final LinerAxis axis = new LinerAxis();
    final LinerAxis yaxis = new LinerAxis();
    final LineChart c = new LineChart();
    c.setRangeMarginX(1);
    c.setXAxis(axis);
    c.setYAxis(yaxis);
    axis.setLowerValue(0);
    axis.setVisibleAmount(0.3);
//    yaxis.setLowerValue(0);
//    yaxis.setVisibleAmount(0.3);
    final ObservableList<LineChartData> datas = c.getDataList();
    lcd = new LineChartData(200);
    lcd2 = new LineChartData(200);
    //sinを
    calcData();
    phase += 0.1;
    datas.add(lcd);
    datas.add(lcd2);
    final BorderPane p = new BorderPane();
    p.setPrefWidth(600);
    p.setPrefHeight(400);
    p.setCenter(c);
    p.setStyle("-fx-padding:50");
    final Scene s = new Scene(p);
    stage.setScene(s);
    stage.show();

    final Timeline timer = new Timeline(new KeyFrame(Duration.millis(1000/60), new EventHandler<ActionEvent>(){
      @Override
      public void handle(final ActionEvent e){
        calcData();
        phase+=0.1;
      }
    }));
    timer.setCycleCount(Timeline.INDEFINITE);
    timer.play();
  }

  private void calcData(){
    for(int i=0;i < 200;i++){
      final double x = (i-100)*0.05;
      this.x[i] = x;
      data[i] = sin(x + phase);
      data2[i] = sin(x+phase+0.5);
    }
    lcd.setData(x, data);
    lcd2.setData(x, data2);
  }


}