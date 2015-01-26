package nodamushi.jfx.chart.linechart;

import static java.lang.Math.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;



/**
 * GraphPlotAreaとLineChartAxisだけのグラフです。<br>
 * データとしてはタイトル情報も保持していますが、タイトルを表示する機能はありません
 * @author nodamushi
 *
 */
public class LineChart extends Region{

  private boolean prelayout=false;

  public LineChart(){
    getStyleClass().setAll("chart");
    graph.setAutoPlot(false);
    graph.setChartData(getDataList());
    graph.xAxisProperty().bind(xAxisProperty());
    graph.yAxisProperty().bind(yAxisProperty());
    graph.orientationProperty().bind(orientationProperty());
    graph.showHorizontalZeroLine();
    graph.showVerticalZeroLine();
    getChildren().add(graph);
  }

  @Override
  public void requestLayout(){
    final Parent p = getParent();
    if(p!=null && p.getClass().getAnnotation(GraphContainer.class)!=null){
      p.requestLayout();
    }
    super.requestLayout();
  }

  public void preLayout(final double x,final double y,final double width,final double height){
    prelayout = true;
    resizeRelocate(x, y, width, height);
    layout();
    prelayout = false;
  }

  @Override
  protected final void layoutChildren(){


    final double w = getWidth();
    final double h = getHeight();
    if(w == -1 || h == -1) {
      return;
    }
    layoutChildren(w,h);
    setLayoutedSize(new Point2D(w, h));
  }
  protected void layoutChildren(double width,double height){
    if(isDataInvalidate()){
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

  GraphPlotArea graph=new GraphPlotArea();

  public final <T extends Event> void addEventHandlerToGraphArea(
      final EventType<T> eventType, final EventHandler<? super T> eventHandler){
    graph.addEventHandler(eventType, eventHandler);
  }

  public final <T extends Event> void addEventFilterToGraphArea(
      final EventType<T> eventType, final EventHandler<? super T> eventFilter){
    graph.addEventFilter(eventType, eventFilter);
  }

  public final <T extends Event> void removeEventHandlerFromGraphArea(
      final EventType<T> eventType, final EventHandler<? super T> eventHandler){
    graph.removeEventHandler(eventType, eventHandler);
  }

  public final <T extends Event> void removeEventFilterFromGraphArea(
      final EventType<T> eventType, final EventHandler<? super T> eventFilter){
    graph.removeEventFilter(eventType, eventFilter);
  }

  private static boolean isLeft(final Side s){
    return s != Side.RIGHT;
  }

  private static boolean isBottom(final Side s){
    return s != Side.TOP;
  }


  protected void layoutChart(final double w,final double h,final double x0,final double y0){
    final Rectangle2D bounds = getPlotAreaPrefferedBounds();
    if(bounds==null){
      layoutChartArea(w, h,x0,y0);
    }else{
      final double ww = bounds.getWidth();
      final double hh =bounds.getHeight();
      final boolean resized = ww != graph.getWidth() || hh!= graph.getHeight();
      if(!prelayout){
        graph.resizeRelocate(
            bounds.getMinX(), bounds.getMinY(),
            ww,hh);
      }
      setPlotAreaBounds(bounds);
      final Axis xAxis = getXAxis();
      final Axis yAxis = getYAxis();
      if(xAxis!=null){
        final double xh=xAxis.prefHeight(ww);
        xAxis.resize(ww, xh);
        if(isBottom(xAxis.getSide())){
          xAxis.relocate(bounds.getMinX(), bounds.getMaxY());
        }else{
          xAxis.relocate(bounds.getMinX(), bounds.getMinY()-xh);
        }
      }
      if(yAxis!=null){
        final double yw = yAxis.prefWidth(hh);
        yAxis.resize(yw, hh);
        if(isLeft(yAxis.getSide())){
          yAxis.relocate(bounds.getMinX()-yw, bounds.getMinY());
        }else{
          yAxis.relocate(bounds.getMaxX(), bounds.getMinY());
        }
      }
      if(!prelayout){
        if(resized || isDataInvalidate()){
          graph.plotData();
          setDataInvalidate(false);
        }
        if(graph.isGraphShapeInvalidate()){
          graph.drawGraphShapes();
        }
      }
    }
  }

  protected void layoutChartArea(final double w,final double h,final double x0,final double y0){
    if(getXAxis() == null || getYAxis() == null){
      graph.setVisible(false);
    }else {
      graph.setVisible(true);
      final Axis xAxis = getXAxis();
      xAxis.setOrientation(Orientation.HORIZONTAL);
      if(xAxis.getSide().isVertical()){
        xAxis.setSide(Side.BOTTOM);
      }

      final Axis yAxis = getYAxis();
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
      graphHeight = max(0, graphHeight);
      graphWidth = max(0,graphWidth);
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
      if(!prelayout){
        if(resize){
          graph.resize(graphWidth, graphHeight);
        }
        if(resize || isDataInvalidate()){
          graph.plotData();
          setDataInvalidate(false);
        }
        if(graph.isGraphShapeInvalidate()){
          graph.drawGraphShapes();
        }
      }

      setPlotAreaBounds(new Rectangle2D(x+x0, y+y0, graphWidth, graphHeight));
    }

  }


  private void setXAxisRange(){
    if(!isAutoRangeX()) {
      return;
    }
    final Axis xAxis = getXAxis();
    if(xAxis==null) {
      return;
    }
    if(datalist == null){
      xAxis.setMaxValue(1);
      xAxis.setMinValue(0);
    }else{
      double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
      for(final LineChartData d:datalist){
        if(d.size() == 0) {
          continue;
        }
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
    final Axis yAxis = getYAxis();
    if(yAxis==null) {
      return;
    }
    if(datalist == null){
      yAxis.setMaxValue(1);
      yAxis.setMinValue(0);
    }else{
      double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
      for(final LineChartData d:datalist){
        if(d.size() == 0) {
          continue;
        }
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





  protected final InvalidationListener getDataInvalidateListener(){
    if (dataInvalidateListener == null) {
      dataInvalidateListener = new InvalidationListener(){
        @Override
        public void invalidated(final Observable observable){
          if (!isDataInvalidate()) {
            setDataInvalidate(true);
            requestLayout();
          }
        }
      };
    }
    return dataInvalidateListener;
  }

  protected final boolean isDataInvalidate(){
    return datainvalidate;
  }

  protected final void setDataInvalidate(final boolean bool){
    datainvalidate = bool;
  }

  /** 直接フィールドを利用せずに、 getValidateListener() を利用すること*/
  private InvalidationListener dataInvalidateListener = null;

  /** 状態の正当性を示すプロパティ*/
  private boolean datainvalidate = false;

  private InvalidationListener lineChartDataListener;
  protected final InvalidationListener getLineChartDataListener(){
    if(lineChartDataListener == null){
      lineChartDataListener = new InvalidationListener(){
        @Override
        public void invalidated(final Observable o){
          if(((ReadOnlyBooleanProperty)o).get() && !isDataInvalidate()){
            setDataInvalidate(true);
            requestLayout();
          }
        }
      };
    }
    return lineChartDataListener;
  }

  private ObservableList<LineChartData> datalist;
  public final ObservableList<LineChartData> getDataList(){
    if(datalist == null){
      datalist = FXCollections.observableArrayList();
      datalist.addListener(getDataInvalidateListener());
      datalist.addListener(new ListChangeListener<LineChartData>(){
        @Override
        public void onChanged(final Change<? extends LineChartData> c){
          final InvalidationListener l = getLineChartDataListener();

          while(c.next()){
            for(final LineChartData d:c.getRemoved()){
              d.invalidateProperty().removeListener(l);
            }

            for(final LineChartData d:c.getAddedSubList()){
              d.invalidateProperty().addListener(l);
            }

            if(!isDataInvalidate()){
              setDataInvalidate(true);
              requestLayout();
            }
          }
        }
      });
    }
    return datalist;
  }




  /**
   * x軸方向に連続なデータか、y軸方向に連続なデータかを指定するプロパティ
   * @return
   */
  public final ObjectProperty<Orientation> orientationProperty(){
    if (orientationProperty == null) {
      orientationProperty = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL);
    }
    return orientationProperty;
  }

  public final Orientation getOrientation(){
    return orientationProperty == null ? Orientation.HORIZONTAL : orientationProperty.get();
  }

  public final void setOrientation(final Orientation value){
    orientationProperty().set(value);
  }

  private ObjectProperty<Orientation> orientationProperty;



  protected final InvalidationListener getLayoutInvalidationListener(){
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

  private InvalidationListener layoutInvalidationListener = null;



  private ChangeListener<Axis> axisListener =new ChangeListener<Axis>(){
    @Override
    public void changed(final ObservableValue<? extends Axis> observable ,
        final Axis oldValue ,final Axis newValue){
      final InvalidationListener listener = getDataInvalidateListener();
      if(oldValue!=null){
        getChildren().remove(oldValue);
        oldValue.lowerValueProperty().removeListener(listener);
        oldValue.visibleAmountProperty().removeListener(listener);
      }

      if(newValue!=null){
        getChildren().add(newValue);
        newValue.lowerValueProperty().addListener(listener);
        newValue.visibleAmountProperty().addListener(listener);

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
  public final ObjectProperty<Axis> xAxisProperty(){
    if (xAxisProperty == null) {
      xAxisProperty = new SimpleObjectProperty<>(this, "xAxis", null);
      xAxisProperty.addListener(getDataInvalidateListener());
      xAxisProperty.addListener(axisListener);
    }
    return xAxisProperty;
  }

  public final Axis getXAxis(){
    return xAxisProperty == null ? null : xAxisProperty.get();
  }

  public final void setXAxis(final Axis value){
    xAxisProperty().set(value);
  }

  private ObjectProperty<Axis> xAxisProperty;



  /**
   * x軸の範囲を自動的に設定するかどうか
   * @return
   */
  public final BooleanProperty autoRangeXProperty(){
    if (autoRangeXProperty == null) {
      autoRangeXProperty = new SimpleBooleanProperty(this, "autoRangeX", true);
    }
    return autoRangeXProperty;
  }

  public final boolean isAutoRangeX(){
    return autoRangeXProperty == null ? true : autoRangeXProperty.get();
  }

  public final void setAutoRangeX(final boolean value){
    autoRangeXProperty().set(value);
  }

  private BooleanProperty autoRangeXProperty;




  /**
   * y軸
   * @return
   */
  public final ObjectProperty<Axis> yAxisProperty(){
    if (yAxisProperty == null) {
      yAxisProperty = new SimpleObjectProperty<>(this, "yAxis", null);
      yAxisProperty.addListener(getDataInvalidateListener());
      yAxisProperty.addListener(axisListener);
    }
    return yAxisProperty;
  }

  public final Axis getYAxis(){
    return yAxisProperty == null ? null : yAxisProperty.get();
  }

  public final void setYAxis(final Axis value){
    yAxisProperty().set(value);
  }

  private ObjectProperty<Axis> yAxisProperty;


  /**
   * y軸の範囲を自動的に設定するかどうか
   * @return
   */
  public final BooleanProperty autoRangeYProperty(){
    if (autoRangeYProperty == null) {
      autoRangeYProperty = new SimpleBooleanProperty(this, "autoRangeY", true);
    }
    return autoRangeYProperty;
  }

  public final boolean isAutoRangeY(){
    return autoRangeYProperty == null ? true : autoRangeYProperty.get();
  }

  public final void setAutoRangeY(final boolean value){
    autoRangeYProperty().set(value);
  }

  private BooleanProperty autoRangeYProperty;




  /**
   * 自動的に設定する範囲に対して持たせる余裕
   * @return
   */
  public final DoubleProperty rangeMarginXProperty(){
    if (rangeMarginXProperty == null) {
      rangeMarginXProperty = new SimpleDoubleProperty(this, "rangeMarginX", 1.25);
    }
    return rangeMarginXProperty;
  }

  public final double getRangeMarginX(){
    return rangeMarginXProperty == null ? 1.25 : rangeMarginXProperty.get();
  }

  public final void setRangeMarginX(final double value){
    rangeMarginXProperty().set(value);
  }

  private DoubleProperty rangeMarginXProperty;


  /**
   * 自動的に設定する範囲に対して持たせる余裕
   * @return
   */
  public final DoubleProperty rangeMarginYProperty(){
    if (rangeMarginYProperty == null) {
      rangeMarginYProperty = new SimpleDoubleProperty(this, "rangeMarginY", 1.25);
    }
    return rangeMarginYProperty;
  }

  public final double getRangeMarginY(){
    return rangeMarginYProperty == null ? 1.25 : rangeMarginYProperty.get();
  }

  public final void setRangeMarginY(final double value){
    rangeMarginYProperty().set(value);
  }

  private DoubleProperty rangeMarginYProperty;




  /**
   * 横方向minor tickの線の可視性
   * @return
   */
  public final BooleanProperty horizontalMinorGridLinesVisibleProperty(){
    if (horizontalMinorGridLinesVisibleProperty == null) {
      horizontalMinorGridLinesVisibleProperty = new SimpleBooleanProperty(this, "horizontalMinorGridLinesVisible", false);
      graph.horizontalMinorGridLinesVisibleProperty()
      .bind(horizontalMinorGridLinesVisibleProperty);
    }
    return horizontalMinorGridLinesVisibleProperty;
  }

  public final boolean isHorizontalMinorGridLinesVisible(){
    return horizontalMinorGridLinesVisibleProperty == null ? false : horizontalMinorGridLinesVisibleProperty.get();
  }

  public final void setHorizontalMinorGridLinesVisible(final boolean value){
    horizontalMinorGridLinesVisibleProperty().set(value);
  }

  private BooleanProperty horizontalMinorGridLinesVisibleProperty;


  /**
   * 縦方向minor tickの線の可視性
   * @return
   */
  public final BooleanProperty verticalMinorGridLinesVisibleProperty(){
    if (verticalMinorGridLinesVisibleProperty == null) {
      verticalMinorGridLinesVisibleProperty = new SimpleBooleanProperty(this, "verticalMinorGridLinesVisible", false);
      graph.verticalMinorGridLinesVisibleProperty()
      .bind(verticalMinorGridLinesVisibleProperty);
    }
    return verticalMinorGridLinesVisibleProperty;
  }

  public final boolean isVerticalMinorGridLinesVisible(){
    return verticalMinorGridLinesVisibleProperty == null ? false : verticalMinorGridLinesVisibleProperty.get();
  }

  public final void setVerticalMinorGridLinesVisible(final boolean value){
    verticalMinorGridLinesVisibleProperty().set(value);
  }

  private BooleanProperty verticalMinorGridLinesVisibleProperty;



  /**
   * GraphPlotAreaの最適な大きさと位置。
   * この値が指定されたときは、このノードの大きさにかかわらず、この値が利用される。
   * @return
   */
  public final ObjectProperty<Rectangle2D> plotAreaPrefferedBoundsProperty(){
    if (plotAreaPrefferedBoundsProperty == null) {
      plotAreaPrefferedBoundsProperty = new SimpleObjectProperty<>(this, "plotAreaPrefferedBounds", null);
      plotAreaPrefferedBoundsProperty.addListener(getLayoutInvalidationListener());
    }
    return plotAreaPrefferedBoundsProperty;
  }

  public final Rectangle2D getPlotAreaPrefferedBounds(){
    return plotAreaPrefferedBoundsProperty == null ? null : plotAreaPrefferedBoundsProperty.get();
  }

  public final void setPlotAreaPrefferedBounds(final Rectangle2D value){
    plotAreaPrefferedBoundsProperty().set(value);
  }

  private ObjectProperty<Rectangle2D> plotAreaPrefferedBoundsProperty;




  /**
   * レイアウト結果のGraphPlotAreaの大きさと位置
   * @return
   */
  public final ReadOnlyObjectProperty<Rectangle2D> plotAreaBoundsProperty(){
    return plotAreaBoundsWrapper().getReadOnlyProperty();
  }

  public final Rectangle2D getPlotAreaBounds(){
    return plotAreaBoundsWrapper.get();
  }

  protected final void setPlotAreaBounds(final Rectangle2D value){
    plotAreaBoundsWrapper().set(value);
  }

  protected final ReadOnlyObjectWrapper<Rectangle2D> plotAreaBoundsWrapper(){
    return plotAreaBoundsWrapper;
  }

  private final ReadOnlyObjectWrapper<Rectangle2D> plotAreaBoundsWrapper=
      new ReadOnlyObjectWrapper<>(this, "plotAreaBounds", null);




  /**
   * layoutChildrenが最後に実行されたときのこのノードの大きさ。
   * xが幅を表し、yが高さを表す。
   * @return
   */
  public final ReadOnlyObjectProperty<Point2D> layoutedSizeProperty(){
    return layoutedSizeWrapper().getReadOnlyProperty();
  }

  public final Point2D getLayoutedSize(){
    return layoutedSizeWrapper.get();
  }

  protected final void setLayoutedSize(final Point2D value){
    layoutedSizeWrapper().set(value);
  }

  protected ReadOnlyObjectWrapper<Point2D> layoutedSizeWrapper(){
    return layoutedSizeWrapper;
  }

  private final ReadOnlyObjectWrapper<Point2D> layoutedSizeWrapper
  = new ReadOnlyObjectWrapper<>(this, "layoutedSize", Point2D.ZERO);




  /**
   * グラフのタイトル。<br>
   * 単なるデータであり、表示はされない
   * @return
   */
  public final StringProperty titleProperty(){
    if (titleProperty == null) {
      titleProperty = new SimpleStringProperty(this, "title", null);
    }
    return titleProperty;
  }

  public final String getTitle(){
    return titleProperty == null ? null : titleProperty.get();
  }

  public final void setTitle(final String value){
    titleProperty().set(value);
  }

  private StringProperty titleProperty;


  private Label titleLabel;
  public Label getTitleLabel(){
    if(titleLabel==null){
      final Label l = new Label();
      l.getStyleClass().setAll("chart-title");
      l.textProperty().bind(titleProperty());
      titleLabel = l;
    }
    return titleLabel;
  }

}
