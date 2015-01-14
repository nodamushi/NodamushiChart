package nodamushi.jfx.chart.linechart;



import static java.lang.Math.*;

import java.util.BitSet;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineJoin;

/**
 * グラフを実際に描画するエリアです
 * @author nodamushi
 *
 */
public class GraphPlotArea extends Region{
  private Rectangle clip;
  private Group background,plotArea,foreground,userBackround,userForeground;
  private Path
  verticalGridLines,horizontalGridLines,
  verticalMinorGridLines,horizontalMinorGridLines,
  verticalRowFill,horizontalRowFill;

  public GraphPlotArea(){
    getStyleClass().setAll("chart-plot-background");
    widthProperty().addListener(getPlotValidateListener());
    heightProperty().addListener(getPlotValidateListener());
    clip = new Rectangle();
    clip.widthProperty().bind(widthProperty());
    clip.heightProperty().bind(heightProperty());
    setClip(clip);

    class Group_ extends Group{
      public Group_(){
        setAutoSizeChildren(false);
      }
      @Override
      public void requestLayout(){}
    }

    background = new Group_();
    plotArea = new Group_();
    foreground = new Group_();
    userBackround = new Group_();
    userForeground = new Group_();
    verticalGridLines = new Path();
    horizontalGridLines=new Path();
    verticalRowFill = new Path();
    horizontalRowFill = new Path();
    verticalMinorGridLines = new Path();
    horizontalMinorGridLines = new Path();
    verticalRowFill.getStyleClass().setAll("chart-alternative-column-fill");
    horizontalRowFill.getStyleClass().setAll("chart-alternative-row-fill");
    verticalGridLines.getStyleClass().setAll("chart-vertical-grid-lines");
    horizontalGridLines.getStyleClass().setAll("chart-horizontal-grid-lines");
    verticalMinorGridLines.getStyleClass().setAll(
        "chart-vertical-grid-lines",
        "chart-vertical-minor-grid-lines");
    horizontalMinorGridLines.getStyleClass().setAll(
        "chart-horizontal-grid-lines",
        "chart-horizontal-minor-grid-lines");
    getChildren().addAll(verticalRowFill,horizontalRowFill,
        verticalMinorGridLines,horizontalMinorGridLines,
        verticalGridLines,horizontalGridLines,
        background,userBackround,plotArea,foreground,userForeground);
  }
  /**
   * ユーザが任意に使える背景領域
   * @return
   */
  public ObservableList<Node> getBackgroundChildren(){
    return userBackround.getChildren();
  }
  /**
   * ユーザが任意に使える前景領域
   * @return
   */
  public ObservableList<Node> getForegroundChildren(){
    return userForeground.getChildren();
  }

  @Override
  protected void layoutChildren(){
    if(isAutoPlot() && !isPlotValidate()){
      plotData();
    }
  }

  public void plotData(){
    final Axis xaxis = getXAxis();
    final Axis yaxis = getYAxis();

    if(xaxis == null || yaxis ==null){
      setPlotValidate(true);
      return;
    }

    final double w = getWidth(),h = getHeight();
    //背景の線を描画

    V:{
      final Axis axis = xaxis;
      final List<Double> vTicks = axis.getMajorTicks();
      final List<Boolean> vFill = axis.getMajorTicksFill();
      final ObservableList<PathElement> lele = verticalGridLines.getElements();
      final ObservableList<PathElement> fele = verticalRowFill.getElements();
      int lelesize = lele.size();
      final int felesize = fele.size();
      final boolean fill = isAlternativeColumnFillVisible();
      final boolean line = isVerticalGridLinesVisible();
      verticalGridLines.setVisible(line);
      verticalRowFill.setVisible(fill);
      final int e = vTicks.size();
      if(!line){
        lele.clear();
      }else if(lelesize > e*2){
        lele.remove(e*2, lelesize);
        lelesize = e*2;
      }
      if(!fill){
        fele.clear();
      }
      int findex = 0;


      if(!line && !fill){
        break V;
      }
      for(int i=0;i<e;i++){
        final double d = vTicks.get(i);
        if(line){
          MoveTo mt;LineTo lt;
          if(i*2 < lelesize){
            mt = (MoveTo)lele.get(i*2);
            lt = (LineTo)lele.get(i*2+1);
          }else{
            mt = new MoveTo();
            lt = new LineTo();
            lele.addAll(mt,lt);
          }
          mt.setX(d);
          mt.setY(0);
          lt.setX(d);
          lt.setY(h);
        }
        if(fill){
          final boolean f = vFill.get(i);
          MoveTo m;
          LineTo l1,l2,l3;

          if(f || i==0){
            if(findex < felesize){
              m = (MoveTo)fele.get(findex);
              l1 = (LineTo)fele.get(findex+1);
              l2 = (LineTo)fele.get(findex+2);
              l3 = (LineTo)fele.get(findex+3);
              findex += 5;
            }else{
              m = new MoveTo();
              l1 = new LineTo();
              l2 = new LineTo();
              l3 = new LineTo();
              fele.addAll(m,l1,l2,l3,new ClosePath());
            }
          } else {
            continue;
          }
          double x0,x1;
          if(!f){
            x0 = 0;
            x1 = d;
          }else if(i == e-1){
            x0 =d;
            x1 = w;
          }else{
            x0=d;
            x1 = vTicks.get(i+1);
          }
          m.setX(x0);
          m.setY(0);
          l1.setX(x0);
          l1.setY(h);
          l2.setX(x1);
          l2.setY(h);
          l3.setX(x1);
          l3.setY(0);
        }//end fill
      }//end for
      if(findex < felesize){
        fele.remove(findex,felesize);
      }
    }//end V

    H:{
      final Axis axis = yaxis;
      final List<Double> hTicks = axis.getMajorTicks();
      final List<Boolean> hFill = axis.getMajorTicksFill();
      final ObservableList<PathElement> lele = horizontalGridLines.getElements();
      final ObservableList<PathElement> fele = horizontalRowFill.getElements();
      int lelesize = lele.size();
      final int felesize = fele.size();
      final boolean fill = isAlternativeRowFillVisible();
      final boolean line = isHorizontalGridLinesVisible();
      horizontalGridLines.setVisible(line);
      horizontalRowFill.setVisible(fill);
      final int e = hTicks.size();
      if(!line){
        lele.clear();
      }else if(lelesize > e*2){
        lele.remove(e*2, lelesize);
        lelesize = e*2;
      }
      if(!fill){
        fele.clear();
      }
      int findex = 0;
      if(!line && !fill){
        break H;
      }
      for(int i=0;i<e;i++){
        final double d = hTicks.get(i);
        if(line){
          MoveTo mt;LineTo lt;
          if(i*2 < lelesize){
            mt = (MoveTo)lele.get(i*2);
            lt = (LineTo)lele.get(i*2+1);
          }else{
            mt = new MoveTo();
            lt = new LineTo();
            lele.addAll(mt,lt);
          }
          mt.setX(0);
          mt.setY(d);
          lt.setX(w);
          lt.setY(d);
        }
        if(fill){
          final boolean f = hFill.get(i);
          MoveTo m;
          LineTo l1,l2,l3;
          if(f || i==0){
            if(findex < felesize){
              m = (MoveTo)fele.get(findex);
              l1 = (LineTo)fele.get(findex+1);
              l2 = (LineTo)fele.get(findex+2);
              l3 = (LineTo)fele.get(findex+3);
              findex+=5;
            }else{
              m = new MoveTo();
              l1 = new LineTo();
              l2 = new LineTo();
              l3 = new LineTo();
              fele.addAll(m,l1,l2,l3,new ClosePath());
            }
          } else {
            continue;
          }
          double y0,y1;
          if(!f){
            y0 = h;
            y1 = d;
          }else if(i == e-1){
            y0 =d;
            y1 = 0;
          }else{
            y0=d;
            y1 = hTicks.get(i+1);
          }
          m.setX(0);
          m.setY(y0);
          l1.setX(w);
          l1.setY(y0);
          l2.setX(w);
          l2.setY(y1);
          l3.setX(0);
          l3.setY(y1);
        }//end fill
      }//end for
      if(findex < felesize){
        fele.remove(findex,felesize);
      }
    }//end H

    V:{
      final Axis axis =xaxis;
      if(!isVerticalMinorGridLinesVisible()){
        verticalMinorGridLines.getElements().clear();
        break V;
      }
      final List<Double> minorTicks = axis.getMinorTicks();
      final ObservableList<PathElement> ele = verticalMinorGridLines.getElements();
      final int elesize = ele.size();
      final int e = minorTicks.size();
      if(elesize > e*2){
        ele.remove(e*2, elesize);
      }
      for(int i=0;i<e;i++){
        final double d = minorTicks.get(i);
        MoveTo mt;LineTo lt;
        if(i*2 < elesize){
          mt = (MoveTo)ele.get(i*2);
          lt = (LineTo)ele.get(i*2+1);
        }else{
          mt = new MoveTo();
          lt = new LineTo();
          ele.addAll(mt,lt);
        }
        mt.setX(d);
        mt.setY(0);
        lt.setX(d);
        lt.setY(h);
      }
    }

    H:{
      final Axis axis =yaxis;
      if(!isHorizontalMinorGridLinesVisible()){
        horizontalMinorGridLines.getElements().clear();
        break H;
      }
      final List<Double> minorTicks = axis.getMinorTicks();
      final ObservableList<PathElement> ele = horizontalMinorGridLines.getElements();
      final int elesize = ele.size();
      final int e = minorTicks.size();
      if(elesize > e*2){
        ele.remove(e*2, elesize);
      }
      for(int i=0;i<e;i++){
        final double d = minorTicks.get(i);
        MoveTo mt;LineTo lt;
        if(i*2 < elesize){
          mt = (MoveTo)ele.get(i*2);
          lt = (LineTo)ele.get(i*2+1);
        }else{
          mt = new MoveTo();
          lt = new LineTo();
          ele.addAll(mt,lt);
        }
        mt.setX(0);
        mt.setY(d);
        lt.setX(w);
        lt.setY(d);
      }
    }

    for(int i=0;i<2;i++){
      final List<GraphLine> lines = i==0?backGroundLines:foreGroundLines;
      if(lines!=null){
        for(final GraphLine gl:lines){
          final double v = gl.getValue();
          final Line l = gl.getLine();
          if(Double.isInfinite(v)|| v!=v || !gl.isVisible()){
            l.setVisible(false);
            continue;
          }
          if(gl.getOrientation()!=Orientation.VERTICAL){
            //横方向
            final double y=yaxis.getDisplayPosition(v);
            if(Double.isInfinite(y)|| y!=y|| y<0 || y>h){
              l.setVisible(false);
              continue;
            }
            l.setStartX(0);
            l.setEndX(w);
            l.setStartY(y);
            l.setEndY(y);
          }else{
            final double x=xaxis.getDisplayPosition(v);
            if(Double.isInfinite(x) || x!=x || x <0 || x >w){
              l.setVisible(false);
              continue;
            }
            l.setStartY(0);
            l.setEndY(h);
            l.setStartX(x);
            l.setEndX(x);
          }
          l.setVisible(true);
        }
      }
    }

    plotLineChartDatas(w,h);
    setPlotValidate(true);
  }

  protected void plotLineChartDatas(final double width,final double height){
    final Group g = plotArea;
    final ObservableList<Node> paths = g.getChildren();
    final List<LineChartData> datas = linechartData;
    if(datas==null){
      paths.clear();
    }else{
      final int size = datas.size();
      int psize = paths.size();
      if(size < psize){
        paths.remove(size, psize);
        psize = size;
      }

      for(int i=0;i<size;i++){
        final int defaultColorIndex = 2;
        final LineChartData d = datas.get(i);
        Path p;
        if(i < psize){
          p = (Path)paths.get(i);
        }else{
          p = new Path();
          p.setStrokeLineJoin(StrokeLineJoin.BEVEL);
          //順序とかあるのかね？
          p.getStyleClass().setAll(
              "chart-series-line",
              "series"+i,
              d.defaultColor);
          paths.add(p);
        }
        final ObservableList<String> sc = p.getStyleClass();

        if(!sc.get(defaultColorIndex).equals(d.defaultColor)){
          sc.set(defaultColorIndex, d.defaultColor);
        }

        plotLineChartData(d, p, width, height);
      }
    }
  }


  private final double DISTANCE_THRESHOLD = 0.5;
  protected void plotLineChartData(final LineChartData data,final Path path,
      final double width,final double height){
    final ObservableList<PathElement> elements = path.getElements();

    final int esize = elements.size();
    final Axis xaxis = getXAxis();
    final Axis yaxis = getYAxis();
    final Orientation orientation = getOrientation();
    if(orientation==Orientation.HORIZONTAL){//x軸方向昇順
      final Axis axis = xaxis;
      final double low=axis.getLowerValue();
      final double up =axis.getUpperValue();
      final int start = data.searchXIndex(low, false);
      final int end = data.searchXIndex(up, true);
      boolean moveTo=true;
      boolean fromInfinit=false;
      boolean positivInf=false;
      double beforeX = 0,beforeY=0;
      int elei=0;
      for(int i=start;i<=end;i++){
        double x = data.getX(i);
        double y = data.getY(i);

        //NaNの場合は線を途切れさせる
        if(y != y){
          moveTo = true;
          fromInfinit = false;
          continue;
        }
        //無限の場合は垂直な線を引く
        if(Double.isInfinite(y)){
          //線が途切れていたり、その前も無限の場合は何もしない
          positivInf = y >0;
          if(!moveTo && !fromInfinit){
            beforeY = positivInf?0:height;
            if(elei<esize){
              final PathElement pathElement = elements.get(elei);
              if(pathElement instanceof LineTo){
                final LineTo m=((LineTo)pathElement);
                m.setX(beforeX);
                m.setY(beforeY);
              }else{
                final LineTo m=new LineTo(beforeX,beforeY);
                elements.set(elei, m);
              }
              elei++;
            }else{
              final LineTo m=new LineTo(beforeX,beforeY);
              elements.add(m);
            }
          }
          //無限フラグを立てる
          fromInfinit = true;
          moveTo=false;
          //次の処理へ
          continue;
        }
        //実数の処理

        //座標変換
        x = xaxis.getDisplayPosition(x);
        y = yaxis.getDisplayPosition(y);

        //前回が無限の時は垂直線を書く
        if(fromInfinit){
          beforeX=x;
          beforeY=positivInf?0:height;
          if(elei < esize){
            final PathElement pathElement = elements.get(elei);
            if(pathElement instanceof MoveTo){//再利用
              final MoveTo m=((MoveTo)pathElement);
              m.setX(x);
              m.setY(beforeY);
            }else{
              final MoveTo m=new MoveTo(x,beforeY);
              elements.set(elei, m);//置換
            }
            elei++;
          }else{
            final MoveTo m=new MoveTo(x,beforeY);
            elements.add(m);
          }
          moveTo = false;//moveToは不要になる
        }

        fromInfinit = false;

        if(moveTo){//線が途切れている場合
          if(elei < esize){
            final PathElement pathElement = elements.get(elei);
            if(pathElement instanceof MoveTo){//再利用
              final MoveTo m=((MoveTo)pathElement);
              m.setX(x);
              m.setY(y);
            }else{
              final MoveTo m=new MoveTo(x,y);
              elements.set(elei, m);//置換
            }
            elei++;
          }else{
            final MoveTo m=new MoveTo(x,y);
            elements.add(m);
          }
          moveTo = false;
          beforeX=x;
          beforeY=y;
        }else{//線が続いている場合
          final double l = hypot(x-beforeX, y-beforeY);
          //距離が小さすぎる場合は無視
          if(l < DISTANCE_THRESHOLD) {
            continue;
          }
          if(elei < esize){
            final PathElement pathElement = elements.get(elei);
            if(pathElement instanceof LineTo){
              final LineTo m=((LineTo)pathElement);
              m.setX(x);
              m.setY(y);
            }else{
              final LineTo m=new LineTo(x,y);
              elements.set(elei, m);
            }
            elei++;
          }else{
            final LineTo m=new LineTo(x,y);
            elements.add(m);
          }
          beforeX=x;
          beforeY=y;
        }
      }//end for

      if(elei < esize){
        elements.remove(elei, esize);
      }
    }else{

      final Axis axis = yaxis;
      final double low=axis.getLowerValue();
      final double up =axis.getUpperValue();
      final int start = data.searchXIndex(low, false);
      final int end = data.searchXIndex(up, true);
      boolean moveTo=true;
      boolean fromInfinit=false;
      boolean positivInf=false;
      double beforeX = 0,beforeY=0;
      int elei=0;
      for(int i=start;i<=end;i++){
        double x = data.getX(i);
        double y = data.getY(i);

        //NaNの場合は線を途切れさせる
        if(x != x){
          moveTo = true;
          fromInfinit = false;
          continue;
        }
        //無限の場合は垂直な線を引く
        if(Double.isInfinite(x)){
          //線が途切れていたり、その前も無限の場合は何もしない
          positivInf = x >0;
          if(!moveTo && !fromInfinit){
            beforeY = positivInf?0:height;
            if(elei<esize){
              final PathElement pathElement = elements.get(elei);
              if(pathElement instanceof LineTo){
                final LineTo m=((LineTo)pathElement);
                m.setX(beforeX);
                m.setY(beforeY);
              }else{
                final LineTo m=new LineTo(beforeX,beforeY);
                elements.set(elei, m);
              }
              elei++;
            }else{
              final LineTo m=new LineTo(beforeX,beforeY);
              elements.add(m);
            }
          }
          //無限フラグを立てる
          fromInfinit = true;
          moveTo=false;
          //次の処理へ
          continue;
        }
        //実数の処理

        //座標変換
        x = xaxis.getDisplayPosition(x);
        y = yaxis.getDisplayPosition(y);

        //前回が無限の時は垂直線を書く
        if(fromInfinit){
          beforeY=y;
          beforeX=positivInf?0:width;
          if(elei < esize){
            final PathElement pathElement = elements.get(elei);
            if(pathElement instanceof MoveTo){//再利用
              final MoveTo m=((MoveTo)pathElement);
              m.setX(beforeX);
              m.setY(y);
            }else{
              final MoveTo m=new MoveTo(beforeX,y);
              elements.set(elei, m);//置換
            }
            elei++;
          }else{
            final MoveTo m=new MoveTo(beforeX,y);
            elements.add(m);
          }
          moveTo = false;//moveToは不要になる
        }

        fromInfinit = false;

        if(moveTo){//線が途切れている場合
          if(elei < esize){
            final PathElement pathElement = elements.get(elei);
            if(pathElement instanceof MoveTo){//再利用
              final MoveTo m=((MoveTo)pathElement);
              m.setX(x);
              m.setY(y);
            }else{
              final MoveTo m=new MoveTo(x,y);
              elements.set(elei, m);//置換
            }
            elei++;
          }else{
            final MoveTo m=new MoveTo(x,y);
            elements.add(m);
          }
          moveTo = false;
          beforeY=y;
          beforeX=x;
        }else{//線が続いている場合
          final double l = hypot(x-beforeX, y-beforeY);
          //距離が小さすぎる場合は無視
          if(l < DISTANCE_THRESHOLD) {
            continue;
          }
          if(elei < esize){
            final PathElement pathElement = elements.get(elei);
            if(pathElement instanceof LineTo){
              final LineTo m=((LineTo)pathElement);
              m.setX(x);
              m.setY(y);
            }else{
              final LineTo m=new LineTo(x,y);
              elements.set(elei, m);
            }
            elei++;
          }else{
            final LineTo m=new LineTo(x,y);
            elements.add(m);
          }
          beforeY=y;
          beforeX=x;
        }
      }//end for

      if(elei < esize){
        elements.remove(elei, esize);
      }


    }

  }



  //----------------------------------------------------------------



  /**
   * 横方向のグリッド線を表示するかどうかのプロパティ
   * @return
   */
  public BooleanProperty horizontalGridLinesVisibleProperty(){
    if (horizontalGridLinesVisibleProperty == null) {
      horizontalGridLinesVisibleProperty = new SimpleBooleanProperty(this, "horizontalGridLinesVisible", true);
    }
    return horizontalGridLinesVisibleProperty;
  }

  public boolean isHorizontalGridLinesVisible(){
    return horizontalGridLinesVisibleProperty == null ? true : horizontalGridLinesVisibleProperty.get();
  }

  public void setHorizontalGridLinesVisible(final boolean value){
    horizontalGridLinesVisibleProperty().set(value);
  }

  private BooleanProperty horizontalGridLinesVisibleProperty;



  /**
   * 縦方向のグリッド線を表示するかどうかのプロパティ
   * @return
   */
  public BooleanProperty verticalGridLinesVisibleProperty(){
    if (verticalGridLinesVisibleProperty == null) {
      verticalGridLinesVisibleProperty = new SimpleBooleanProperty(this, "verticalGridLinesVisible", true);
    }
    return verticalGridLinesVisibleProperty;
  }

  public boolean isVerticalGridLinesVisible(){
    return verticalGridLinesVisibleProperty == null ? true : verticalGridLinesVisibleProperty.get();
  }

  public void setVerticalGridLinesVisible(final boolean value){
    verticalGridLinesVisibleProperty().set(value);
  }

  private BooleanProperty verticalGridLinesVisibleProperty;




  /**
   * 横方向minor tickの線の可視性
   * @return
   */
  public BooleanProperty horizontalMinorGridLinesVisibleProperty(){
    if (horizontalMinorGridLinesVisibleProperty == null) {
      horizontalMinorGridLinesVisibleProperty = new SimpleBooleanProperty(this, "horizontalMinorGridLinesVisible", false);
      horizontalMinorGridLines.visibleProperty()
      .bind(horizontalMinorGridLinesVisibleProperty);
    }
    return horizontalMinorGridLinesVisibleProperty;
  }

  public boolean isHorizontalMinorGridLinesVisible(){
    return horizontalMinorGridLinesVisibleProperty == null ? false : horizontalMinorGridLinesVisibleProperty.get();
  }

  public void setHorizontalMinorGridLinesVisible(final boolean value){
    horizontalMinorGridLinesVisibleProperty().set(value);
  }

  private BooleanProperty horizontalMinorGridLinesVisibleProperty;


  /**
   * 縦方向minor tickの線の可視性
   * @return
   */
  public BooleanProperty verticalMinorGridLinesVisibleProperty(){
    if (verticalMinorGridLinesVisibleProperty == null) {
      verticalMinorGridLinesVisibleProperty = new SimpleBooleanProperty(this, "verticalMinorGridLinesVisible", false);
      verticalMinorGridLines.visibleProperty()
      .bind(verticalMinorGridLinesVisibleProperty);
    }
    return verticalMinorGridLinesVisibleProperty;
  }

  public boolean isVerticalMinorGridLinesVisible(){
    return verticalMinorGridLinesVisibleProperty == null ? false : verticalMinorGridLinesVisibleProperty.get();
  }

  public void setVerticalMinorGridLinesVisible(final boolean value){
    verticalMinorGridLinesVisibleProperty().set(value);
  }

  private BooleanProperty verticalMinorGridLinesVisibleProperty;
  /**
   * 縦方向に相互に背景を塗りつぶすかどうか
   * @return
   */
  public BooleanProperty alternativeColumnFillVisibleProperty(){
    if (alternativeColumnFillVisibleProperty == null) {
      alternativeColumnFillVisibleProperty = new SimpleBooleanProperty(this, "alternativeColumnFillVisible", true);
    }
    return alternativeColumnFillVisibleProperty;
  }

  public boolean isAlternativeColumnFillVisible(){
    return alternativeColumnFillVisibleProperty == null ? true : alternativeColumnFillVisibleProperty.get();
  }

  public void setAlternativeColumnFillVisible(final boolean value){
    alternativeColumnFillVisibleProperty().set(value);
  }

  private BooleanProperty alternativeColumnFillVisibleProperty;


  /**
   * 自動的に生成されたプロパティ
   * @return
   */
  public BooleanProperty alternativeRowFillVisibleProperty(){
    if (alternativeRowFillVisibleProperty == null) {
      alternativeRowFillVisibleProperty = new SimpleBooleanProperty(this, "alternativeRowFillVisible", true);
    }
    return alternativeRowFillVisibleProperty;
  }

  public boolean isAlternativeRowFillVisible(){
    return alternativeRowFillVisibleProperty == null ? true : alternativeRowFillVisibleProperty.get();
  }

  public void setAlternativeRowFillVisible(final boolean value){
    alternativeRowFillVisibleProperty().set(value);
  }

  private BooleanProperty alternativeRowFillVisibleProperty;

  private ChangeListener<Axis> axisListener =new ChangeListener<Axis>(){
    @Override
    public void changed(final ObservableValue<? extends Axis> observable ,
        final Axis oldValue ,final Axis newValue){
      final InvalidationListener listener = getPlotValidateListener();
      final boolean isX = observable == xAxisProperty;

      if(oldValue!=null){
        oldValue.lowerValueProperty().removeListener(listener);
        oldValue.upperValueProperty().removeListener(listener);
        if(isX){
          verticalMinorGridLines.visibleProperty().unbind();
        }else{
          horizontalMinorGridLines.visibleProperty().unbind();
        }
      }

      if(newValue!=null){
        newValue.lowerValueProperty().addListener(listener);
        newValue.upperValueProperty().addListener(listener);
      }
      if (isPlotValidate()) {
        setPlotValidate(false);
        requestLayout();
      }

    }
  };

  /**
   * x-axis
   * @return
   */
  public ObjectProperty<Axis> xAxisProperty(){
    if (xAxisProperty == null) {
      xAxisProperty = new SimpleObjectProperty<>(this, "xAxis", null);
      xAxisProperty.addListener(axisListener);
    }
    return xAxisProperty;
  }

  public Axis getXAxis(){
    return xAxisProperty == null ? null : xAxisProperty.get();
  }

  public void setXAxis(final Axis value){
    xAxisProperty().set(value);
  }

  private ObjectProperty<Axis> xAxisProperty;



  /**
   * y-axis
   * @return
   */
  public ObjectProperty<Axis> yAxisProperty(){
    if (yAxisProperty == null) {
      yAxisProperty = new SimpleObjectProperty<>(this, "yAxis", null);
      yAxisProperty.addListener(axisListener);
    }
    return yAxisProperty;
  }

  public Axis getYAxis(){
    return yAxisProperty == null ? null : yAxisProperty.get();
  }

  public void setYAxis(final Axis value){
    yAxisProperty().set(value);
  }

  private ObjectProperty<Axis> yAxisProperty;


  private ObservableList<GraphLine> backGroundLines,foreGroundLines;
  public ObservableList<GraphLine> getBackGroundLine(){
    if(backGroundLines == null){
      backGroundLines = FXCollections.observableArrayList();
      final ListChangeListener<GraphLine> l =new ListChangeListener<GraphLine>(){
        @Override
        public void onChanged(final Change<? extends GraphLine> c){
          c.next();
          final Group g = background;
          final ObservableList<Node> ch = g.getChildren();
          for(final GraphLine gl: c.getRemoved()){
            ch.remove(gl.getLine());
          }
          for(final GraphLine gl: c.getAddedSubList()){
            ch.add(gl.getLine());
          }
          if(isPlotValidate()){
            setPlotValidate(false);
            requestLayout();
          }
        }
      };
      backGroundLines.addListener(l);
    }
    return backGroundLines;
  }

  public ObservableList<GraphLine> getForeGroundLine(){
    if(foreGroundLines == null){
      foreGroundLines = FXCollections.observableArrayList();
      final ListChangeListener<GraphLine> l =new ListChangeListener<GraphLine>(){
        @Override
        public void onChanged(final Change<? extends GraphLine> c){
          c.next();
          final Group g = foreground;
          final ObservableList<Node> ch = g.getChildren();
          for(final GraphLine gl: c.getRemoved()){
            ch.remove(gl.getLine());
          }
          for(final GraphLine gl: c.getAddedSubList()){
            ch.add(gl.getLine());
          }
          if(isPlotValidate()){
            setPlotValidate(false);
            requestLayout();
          }
        }
      };
      foreGroundLines.addListener(l);
    }
    return foreGroundLines;
  }

  private ObservableList<LineChartData> linechartData;
  private ListChangeListener<LineChartData> dataListListener;
  private InvalidationListener dataListener;
  private BitSet colorIndex = new BitSet(8);
  protected InvalidationListener getDataListener(){
    if(dataListener == null){
      dataListener = new InvalidationListener(){
        @Override
        public void invalidated(final Observable observable){
          final ReadOnlyBooleanProperty b =(ReadOnlyBooleanProperty)observable;
          if(!b.get() && isPlotValidate()){
            setPlotValidate(false);
            requestLayout();
          }
        }
      };
    }
    return dataListener;
  }

  public void setLineChartDataList(final ObservableList<LineChartData> datalist){
    if(dataListener==null){
      dataListListener = new ListChangeListener<LineChartData>(){
        @Override
        public void onChanged(final Change<? extends LineChartData> c){
          c.next();
          final InvalidationListener dataListener = getDataListener();
          for(final LineChartData d:c.getRemoved()){
            colorIndex.clear(d.defaultColorIndex);
            d.validateProperty().removeListener(dataListener);
          }
          for(final LineChartData d:c.getAddedSubList()){
            d.defaultColorIndex = colorIndex.nextClearBit(0);
            colorIndex.set(d.defaultColorIndex, true);
            d.defaultColor="default-color"+(d.defaultColorIndex%8);
            d.validateProperty().addListener(dataListener);
          }
        }
      };
    }

    final ObservableList<LineChartData> old = linechartData;
    final InvalidationListener dataListener = getDataListener();
    if(old != null){
      old.removeListener(dataListListener);
      for(final LineChartData d:old){
        d.validateProperty().removeListener(dataListener);
      }
      colorIndex.clear();
    }

    if(datalist!=null){
      datalist.addListener(dataListListener);
      for(final LineChartData d:datalist){
        d.defaultColorIndex = colorIndex.nextClearBit(0);
        colorIndex.set(d.defaultColorIndex, true);
        d.defaultColor="default-color"+(d.defaultColorIndex%8);
        d.validateProperty().addListener(dataListener);
      }
    }

    linechartData = datalist;
    if(isPlotValidate()){
      setPlotValidate(false);
      requestLayout();
    }
  }


  /**
   * layoutChildrenを実行時に自動的にグラフエリアの描画も行うかどうか。
   * falseにした場合は必要なときに自分でplotDataを呼び出す必要がある。
   * デフォルトはtrue
   * @return
   */
  public BooleanProperty autoPlotProperty(){
    if (autoPlotProperty == null) {
      autoPlotProperty = new SimpleBooleanProperty(this, "autoPlot", true);
    }
    return autoPlotProperty;
  }

  public boolean isAutoPlot(){
    return autoPlotProperty == null ? true : autoPlotProperty.get();
  }

  public void setAutoPlot(final boolean value){
    autoPlotProperty().set(value);
  }

  private BooleanProperty autoPlotProperty;


  protected InvalidationListener getPlotValidateListener(){
    return plotValidateListener;
  }

  protected boolean isPlotValidate(){
    return plotValidate;
  }

  protected void setPlotValidate(final boolean bool){
    plotValidate = bool;
  }

  /** 状態の正当性を示すプロパティ*/
  private boolean plotValidate = false;

  /** 直接フィールドを利用せずに、 getValidateListener() を利用すること*/
  private InvalidationListener  plotValidateListener = new InvalidationListener(){
    @Override
    public void invalidated(final Observable observable){
      if (isPlotValidate()) {
        setPlotValidate(false);
        requestLayout();
      }
    }
  };



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






  private GraphLine verticalZeroLine,horizontalZeroLine;

  public GraphLine getVerticalZeroLine(){
    if(verticalZeroLine == null){
      verticalZeroLine = new GraphLine();
      verticalZeroLine.setOrientation(Orientation.VERTICAL);
      verticalZeroLine.getStyleClass().setAll("chart-vertical-zero-line");
    }
    return verticalZeroLine;
  }

  public GraphLine getHorizontalZeroLine(){
    if(horizontalZeroLine == null){
      horizontalZeroLine = new GraphLine();
      horizontalZeroLine.setOrientation(Orientation.HORIZONTAL);
      horizontalZeroLine.getStyleClass().setAll("chart-horizontal-zero-line");
    }
    return horizontalZeroLine;
  }


  public void showVerticalZeroLine(){
    final ObservableList<GraphLine> backGroundLine = getBackGroundLine();
    final GraphLine l = getVerticalZeroLine();
    l.setVisible(true);
    if(!backGroundLine.contains(l)){
      backGroundLine.add(l);
    }
  }
  public void showHorizontalZeroLine(){
    final ObservableList<GraphLine> backGroundLine = getBackGroundLine();
    final GraphLine l = getHorizontalZeroLine();
    l.setVisible(true);
    if(!backGroundLine.contains(l)){
      backGroundLine.add(l);
    }
  }


}























