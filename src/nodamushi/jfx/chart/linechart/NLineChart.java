package nodamushi.jfx.chart.linechart;

import static java.lang.Math.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;




public class NLineChart extends Region{

  public NLineChart(){
    getStyleClass().add("chart");
    graph.setAutoPlot(false);
    graph.setLineChartDataList(getDatas());
    graph.xAxisProperty().bind(xAxisProperty());
    graph.yAxisProperty().bind(yAxisProperty());
    graph.orientationProperty().bind(orientationProperty());
    graph.showHorizontalZeroLine();
    graph.showVerticalZeroLine();
    title = new Label();
    title.textProperty().bind(titleProperty());
    getChildren().addAll(graph,title);
  }

  @Override
  protected final void layoutChildren(){

    final double w = getWidth();
    final double h = getHeight();
    if(w == -1 || h == -1) {
      return;
    }
    layoutChildren(w,h);
  }
  protected void layoutChildren(double width,double height){
    if(!isDataValidate()){
      dealWithData();
    }

    final Insets in = getInsets();
    width -=in.getLeft()+in.getRight();
    height -=in.getTop()+in.getBottom();
    layoutChart(width, height,in.getLeft(),in.getTop());

  }

  protected void dealWithData(){
    setXAxisRange();
    setYAxisRange();
  }


  @Override
  protected double computePrefHeight(final double width) {return 150;}

  @Override
  protected double computePrefWidth(final double height){return 150;}

  private Label title;
  private GraphArea graph=new GraphArea();

  public <T extends Event> void addEventHandlerToGraphArea(
      final EventType<T> eventType, final EventHandler<? super T> eventHandler){
    graph.addEventHandler(eventType, eventHandler);
  }

  public <T extends Event> void addEventFilterToGraphArea(
      final EventType<T> eventType, final EventHandler<? super T> eventFilter){
    graph.addEventFilter(eventType, eventFilter);
  }

  public <T extends Event> void removeEventHandlerFromGraphArea(
      final EventType<T> eventType, final EventHandler<? super T> eventHandler){
    graph.removeEventHandler(eventType, eventHandler);
  }

  public <T extends Event> void removeEventFilterFromGraphArea(
      final EventType<T> eventType, final EventHandler<? super T> eventFilter){
    graph.removeEventFilter(eventType, eventFilter);
  }

  protected void layoutChart(final double w,final double h,final double x0,final double y0){
    final double titleh = title.prefHeight(w);
    final double titlew = title.prefWidth(titleh);
    title.resizeRelocate(x0+(w-titlew)*0.5, 0, titlew, titleh);
    layoutChartArea(w, h-titleh,x0,titleh);
  }

  protected void layoutChartArea(final double w,final double h,final double x0,final double y0){
    if(getXAxis() == null || getYAxis() == null){
      graph.setVisible(false);
    }else {
      graph.setVisible(true);
      final LineChartAxis xAxis = getXAxis();
      xAxis.setOrientation(Orientation.HORIZONTAL);
      if(xAxis.getSide().isVertical()){
        xAxis.setSide(Side.BOTTOM);
      }

      final LineChartAxis yAxis = getYAxis();
      yAxis.setOrientation(Orientation.VERTICAL);
      if(yAxis.getSide().isHorizontal()){
        yAxis.setSide(Side.LEFT);
      }
      double graphWidth,graphHeight;
      double xAxisHeight,yAxisWidth;
      if(xAxis.isVisible() && yAxis.isVisible()){
        double gap = 10;
        int loop=0;
        double xH=0,yW=0;
        while(gap >5){
          double xaxisH = xAxis.prefHeight(w);
          double yaxisW = yAxis.prefWidth(h-xaxisH);
          final double xaxisH2 = xAxis.prefHeight(w-yaxisW);
          gap = abs(xaxisH2-xaxisH);
          xaxisH = xaxisH2;
          if(gap >5){
            final double yaxisW2 = yAxis.prefWidth(h-xaxisH);
            gap = abs(yaxisW-yaxisW2);
            yaxisW=yaxisW2;
          }
          xH = xaxisH;
          yW = yaxisW;
          loop++;
          if(loop == 5){
            break;
          }
        }
        graphWidth = w-yW;
        graphHeight = h-xH;
        xAxisHeight = xH;
        yAxisWidth = yW;
      }else{
        if(xAxis.isVisible()){
          xAxisHeight = xAxis.prefHeight(w);
          graphHeight = h-xAxisHeight;
          graphWidth = w;
          yAxisWidth = yAxis.prefWidth(graphHeight);
        }else if(yAxis.isVisible()){
          yAxisWidth = yAxis.prefWidth(h);
          graphWidth = w-yAxisWidth;
          graphHeight = h;
          xAxisHeight = xAxis.prefHeight(graphWidth);
        }else{
          xAxisHeight = xAxis.prefHeight(w);
          yAxisWidth = yAxis.prefWidth(h);
          graphWidth = w;
          graphHeight = h;
        }
      }
      xAxis.resize(graphWidth, xAxisHeight);
      yAxis.resize(yAxisWidth,graphHeight);
      xAxis.layout();
      yAxis.layout();
      final boolean isLeft = yAxis.getSide()!=Side.RIGHT;
      final boolean isBottom = xAxis.getSide()!=Side.TOP;
      final double
      x=yAxis.isVisible()&&isLeft?yAxisWidth:0,
      y=!xAxis.isVisible()||isBottom?0:xAxisHeight;
      if(xAxis.isVisible()) {
        if(isBottom){
          xAxis.relocate(x+x0, graphHeight+y0);
        }else{
          xAxis.relocate(x+x0, y0);
        }
      }
      if(yAxis.isVisible()) {
        if(isLeft){
          yAxis.relocate(x0,y+y0);
        }else{
          yAxis.relocate(graphWidth+x0,y+y0);
        }
      }
      graph.relocate(x+x0, y+y0);
      final double oldgW = graph.getWidth();
      final double oldgH = graph.getHeight();
      final boolean resize = oldgW != graphWidth || oldgH != graphHeight;
      if(resize){
        graph.resize(graphWidth, graphHeight);
      }
      if(resize || !isDataValidate()){
        graph.plotData();
        setDataValidate(true);
      }
    }

  }


  private void setXAxisRange(){
    if(!isAutoRangeX()) {
      return;
    }
    final LineChartAxis xAxis = getXAxis();
    if(xAxis==null) {
      return;
    }
    if(datas == null){
      xAxis.setMaxValue(1);
      xAxis.setMinValue(0);
    }else{
      double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
      for(final LineChartData d:datas){
        if(getOrientation()==Orientation.HORIZONTAL){
          final double i = d.getX(0);
          final double a = d.getX(d.size()-1);
          min = Math.min(min, i);
          max = Math.max(max,a);
        }else{
          final double[] minmax = d.getMinMaxX(0, d.size(), true);
          min = Math.min(min,minmax[0]);
          max = Math.max(max,minmax[1]);
        }
      }
      if(Double.isInfinite(min)) {
        min = 0;
      }
      if(Double.isInfinite(max)) {
        max = min+1;
      }
      final double l = max-min;
      final double ll = l*getRangeMarginX();
      double u = min + ll;
      double b = max -ll;
      if(max == 0 || signum(u)*signum(max)<0){
        u = 0;
      }
      if(min ==0 || signum(b)*signum(min)<0){
        b = 0;
      }

      xAxis.setMaxValue(u);
      xAxis.setMinValue(b);
    }
  }

  private void setYAxisRange(){
    if(!isAutoRangeY()) {
      return;
    }
    final LineChartAxis yAxis = getYAxis();
    if(yAxis==null) {
      return;
    }
    if(datas == null){
      yAxis.setMaxValue(1);
      yAxis.setMinValue(0);
    }else{
      double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
      for(final LineChartData d:datas){
        if(getOrientation()==Orientation.VERTICAL){
          final double i = d.getY(0);
          final double a = d.getY(d.size()-1);
          min = Math.min(min, i);
          max = Math.max(max,a);
        }else{
          final double[] minmax = d.getMinMaxY(0, d.size()-1, true);
          min = Math.min(min,minmax[0]);
          max = Math.max(max,minmax[1]);
        }
      }
      if(Double.isInfinite(min)) {
        min = 0;
      }
      if(Double.isInfinite(max)) {
        max = min+1;
      }
      final double l = max-min;
      final double ll = l*getRangeMarginY();
      double u = min + ll;
      double b = max -ll;
      if(max == 0 || signum(u)*signum(max)<0){
        u = 0;
      }
      if(min ==0 || signum(b)*signum(min)<0){
        b = 0;
      }

      yAxis.setMaxValue(u);
      yAxis.setMinValue(b);
    }
  }





  protected InvalidationListener getDataValidateListener(){
    if (dataValidateListener == null) {
      dataValidateListener = new InvalidationListener(){
        @Override
        public void invalidated(final Observable observable){
          if (isDataValidate()) {
            setDataValidate(false);
            requestLayout();
          }
        }
      };
    }
    return dataValidateListener;
  }

  protected boolean isDataValidate(){
    return datavalidate;
  }

  protected void setDataValidate(final boolean bool){
    datavalidate = bool;
  }

  /** 直接フィールドを利用せずに、 getValidateListener() を利用すること*/
  private InvalidationListener dataValidateListener = null;

  /** 状態の正当性を示すプロパティ*/
  private boolean datavalidate = false;

  private InvalidationListener lineChartDataListener;
  protected InvalidationListener getLineChartDataListener(){
    if(lineChartDataListener == null){
      lineChartDataListener = new InvalidationListener(){
        @Override
        public void invalidated(final Observable o){
          if(!((ReadOnlyBooleanProperty)o).get() && isDataValidate()){
            setDataValidate(false);
            requestLayout();
          }
        }
      };
    }
    return lineChartDataListener;
  }

  private ObservableList<LineChartData> datas;
  public ObservableList<LineChartData> getDatas(){
    if(datas == null){
      datas = FXCollections.observableArrayList();
      datas.addListener(getDataValidateListener());
      datas.addListener(new ListChangeListener<LineChartData>(){
        @Override
        public void onChanged(final Change<? extends LineChartData> c){
          final InvalidationListener l = getLineChartDataListener();

          while(c.next()){
            for(final LineChartData d:c.getRemoved()){
              d.validateProperty().removeListener(l);
            }

            for(final LineChartData d:c.getAddedSubList()){
              d.validateProperty().addListener(l);
            }

            if(isDataValidate()){
              setDataValidate(false);
              requestLayout();
            }
          }
        }
      });
    }
    return datas;
  }




  /**
   * x軸方向に連続なデータか、y軸方向に連続なデータかを指定するプロパティ
   * @return
   */
  public ObjectProperty<Orientation> orientationProperty(){
    if (orientationProperty == null) {
      orientationProperty = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL);
    }
    return orientationProperty;
  }

  public Orientation getOrientation(){
    return orientationProperty == null ? Orientation.HORIZONTAL : orientationProperty.get();
  }

  public void setOrientation(final Orientation value){
    orientationProperty().set(value);
  }

  private ObjectProperty<Orientation> orientationProperty;



  protected InvalidationListener getLayoutInvalidationListener(){
    if (layoutInvalidationListener == null) {
      layoutInvalidationListener = new InvalidationListener(){
        @Override
        public void invalidated(final Observable observable){
          requestLayout();
        }
      };
    }
    return layoutInvalidationListener;
  }


  /** 直接フィールドを利用せずに、 getValidateListener() を利用すること*/
  private InvalidationListener layoutInvalidationListener = null;



  private ChangeListener<LineChartAxis> axisListener =new ChangeListener<LineChartAxis>(){
    @Override
    public void changed(final ObservableValue<? extends LineChartAxis> observable ,
        final LineChartAxis oldValue ,final LineChartAxis newValue){
      final InvalidationListener listener = getDataValidateListener();
      if(oldValue!=null){
        getChildren().remove(oldValue);
        oldValue.lowerValueProperty().removeListener(listener);
        oldValue.upperValueProperty().removeListener(listener);
      }

      if(newValue!=null){
        getChildren().add(newValue);
        newValue.lowerValueProperty().addListener(listener);
        newValue.upperValueProperty().addListener(listener);

      }
      if(xAxisProperty == observable){
        newValue.setOrientation(Orientation.HORIZONTAL);
        if(newValue.getSide().isVertical()){
          newValue.setSide(Side.BOTTOM);
        }
      }else{
        newValue.setOrientation(Orientation.VERTICAL);
        if(newValue.getSide().isHorizontal()){
          newValue.setSide(Side.LEFT);
        }
      }
    }
  };

  /**
   * x軸
   * @return
   */
  public ObjectProperty<LineChartAxis> xAxisProperty(){
    if (xAxisProperty == null) {
      xAxisProperty = new SimpleObjectProperty<>(this, "xAxis", null);
      xAxisProperty.addListener(getDataValidateListener());
      xAxisProperty.addListener(axisListener);
    }
    return xAxisProperty;
  }

  public LineChartAxis getXAxis(){
    return xAxisProperty == null ? null : xAxisProperty.get();
  }

  public void setXAxis(final LineChartAxis value){
    xAxisProperty().set(value);
  }

  private ObjectProperty<LineChartAxis> xAxisProperty;



  /**
   * x軸の範囲を自動的に設定するかどうか
   * @return
   */
  public BooleanProperty autoRangeXProperty(){
    if (autoRangeXProperty == null) {
      autoRangeXProperty = new SimpleBooleanProperty(this, "autoRangeX", true);
    }
    return autoRangeXProperty;
  }

  public boolean isAutoRangeX(){
    return autoRangeXProperty == null ? true : autoRangeXProperty.get();
  }

  public void setAutoRangeX(final boolean value){
    autoRangeXProperty().set(value);
  }

  private BooleanProperty autoRangeXProperty;




  /**
   * y軸
   * @return
   */
  public ObjectProperty<LineChartAxis> yAxisProperty(){
    if (yAxisProperty == null) {
      yAxisProperty = new SimpleObjectProperty<>(this, "yAxis", null);
      yAxisProperty.addListener(getDataValidateListener());
      yAxisProperty.addListener(axisListener);
    }
    return yAxisProperty;
  }

  public LineChartAxis getYAxis(){
    return yAxisProperty == null ? null : yAxisProperty.get();
  }

  public void setYAxis(final LineChartAxis value){
    yAxisProperty().set(value);
  }

  private ObjectProperty<LineChartAxis> yAxisProperty;


  /**
   * y軸の範囲を自動的に設定するかどうか
   * @return
   */
  public BooleanProperty autoRangeYProperty(){
    if (autoRangeYProperty == null) {
      autoRangeYProperty = new SimpleBooleanProperty(this, "autoRangeY", true);
    }
    return autoRangeYProperty;
  }

  public boolean isAutoRangeY(){
    return autoRangeYProperty == null ? true : autoRangeYProperty.get();
  }

  public void setAutoRangeY(final boolean value){
    autoRangeYProperty().set(value);
  }

  private BooleanProperty autoRangeYProperty;




  /**
   * 自動的に設定する範囲に対して持たせる余裕
   * @return
   */
  public DoubleProperty rangeMarginXProperty(){
    if (rangeMarginXProperty == null) {
      rangeMarginXProperty = new SimpleDoubleProperty(this, "rangeMarginX", 1.25);
    }
    return rangeMarginXProperty;
  }

  public double getRangeMarginX(){
    return rangeMarginXProperty == null ? 1.25 : rangeMarginXProperty.get();
  }

  public void setRangeMarginX(final double value){
    rangeMarginXProperty().set(value);
  }

  private DoubleProperty rangeMarginXProperty;


  /**
   * 自動的に設定する範囲に対して持たせる余裕
   * @return
   */
  public DoubleProperty rangeMarginYProperty(){
    if (rangeMarginYProperty == null) {
      rangeMarginYProperty = new SimpleDoubleProperty(this, "rangeMarginY", 1.25);
    }
    return rangeMarginYProperty;
  }

  public double getRangeMarginY(){
    return rangeMarginYProperty == null ? 1.25 : rangeMarginYProperty.get();
  }

  public void setRangeMarginY(final double value){
    rangeMarginYProperty().set(value);
  }

  private DoubleProperty rangeMarginYProperty;


  /**
   * タイトル
   * @return
   */
  public StringProperty titleProperty(){
    if (titleProperty == null) {
      titleProperty = new SimpleStringProperty(this, "title", "");
    }
    return titleProperty;
  }

  public String getTitle(){
    return titleProperty == null ? "" : titleProperty.get();
  }

  public void setTitle(final String value){
    titleProperty().set(value);
  }

  private StringProperty titleProperty;

}
