package nodamushi.jfx.chart.linechart;

import static java.lang.Math.*;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Callable;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;

public abstract class LineChartAxis extends Region{

  /**
   * NAxisの状態の変化を受け取る必要がある親を定義するインターフェース
   * @author nodamushi
   *
   */
  public static interface AxisParent{
    /**
     * Axisの状態が変化し、グラフの描画が不正確になった通知を受け取るメソッド。
     * @param axis 変化したNAxis
     */
    public void graphInvalidate(LineChartAxis axis);
  }


  public LineChartAxis(){
    getStyleClass().add("axis");
    dataValidateListener = new InvalidationListener(){
      @Override
      public void invalidated(final Observable observable){
        if(widthProperty() == observable){
          if(getOrientation() != Orientation.HORIZONTAL||
              getWidth()==lastLayoutWidth){
            return;
          }
        }
        if(heightProperty() == observable ){
          if(getOrientation()!=Orientation.VERTICAL||
              getHeight()==lastLayoutHeight){
            return;
          }
        }
        if (isDataValidate()) {
          setDataValidate(false);
          requestLayout();
        }
      }
    };
    widthProperty().addListener(dataValidateListener);
    heightProperty().addListener(dataValidateListener);
  }

  @Override
  protected final void layoutChildren(){
    if((isHorizontal() &&getWidth() == -1) ||
        (isVertical() && getHeight()==-1)) {
      return;
    }
    layoutChildren(getWidth(), getHeight());
  }


  protected final void layoutChildren(final double width,final double height){
    if(nowLayout) {
      return;
    }
    nowLayout = true;
    try{
      final boolean b=!isDataValidate() ||
          getAxisLength(width, height) != getAxisLength(lastLayoutWidth, lastLayoutHeight);
      if(b){
        computeAxisProperties(width, height);
        setLayoutValidate(false);
        setDataValidate(true);
      }

      if(!isLayoutValidate() ||
          width != lastLayoutWidth || height!=lastLayoutHeight){
        layoutAxis(width,height);
        setLayoutValidate(true);
      }
      lastLayoutWidth = width;
      lastLayoutHeight = height;

      if(b){
        final Parent parent = getParent();
        if(parent instanceof AxisParent){
          ((AxisParent)parent).graphInvalidate(this);
        }
      }
    }finally{
      nowLayout = false;
    }
  }
  private boolean nowLayout=false;

  private double lastLayoutWidth=-1,lastLayoutHeight=-1;

  /**
   * 軸方向のサイズを返す
   * @return
   */
  protected double getAxisLength(){
    return getAxisLength(getWidth(), getHeight());
  }

  /**
   * 軸方向のサイズを返す
   * @param width
   * @param height
   * @return
   */
  protected double getAxisLength(final double width,final double height){
    return isHorizontal()?width:height;
  }

  public abstract double getDisplayPosition(double v);
  public abstract double getValueForDisplay(double position);

  public double getZeroPosition(){
    return getDisplayPosition(0);
  }

  //----------------------------------------------------------------------
  //                       data
  //----------------------------------------------------------------------
  /**
   * Axisの描画に必要なプロパティを計算するメソッド
   * width,heightのどちらかは-1である場合がある。
   * @param width 描画横幅
   * @param height 描画高さ
   */
  protected abstract void computeAxisProperties(double width,double height);


  //-----------------layoutにしか関係ないデータ---------------------------


  /**
   * 軸の名前
   * @return
   */
  public StringProperty nameProperty(){
    if (nameProperty == null) {
      nameProperty = new SimpleStringProperty(this, "name", null);
      //nameはレイアウトにしか関わらない
      nameProperty.addListener(getLayoutValidateListener());
    }
    return nameProperty;
  }

  public String getName(){
    return nameProperty == null ? null : nameProperty.get();
  }

  public void setName(final String value){
    nameProperty().set(value);
  }

  private StringProperty nameProperty;



  /**
   * major tickの線の長さ
   * @return
   */
  public DoubleProperty majorTickLengthProperty(){
    if (majorTickLengthProperty == null) {
      majorTickLengthProperty = new SimpleDoubleProperty(this, "majorTickLength", 12);
      majorTickLengthProperty.addListener(getLayoutValidateListener());
    }
    return majorTickLengthProperty;
  }

  public double getMajorTickLength(){
    return majorTickLengthProperty == null ? 12 : majorTickLengthProperty.get();
  }

  public void setmMjorTickLength(final double value){
    majorTickLengthProperty().set(value);
  }

  private DoubleProperty majorTickLengthProperty;



  /**
   * minor tickの線の長さ
   * @return
   */
  public DoubleProperty minorTickLengthProperty(){
    if (minorTickLengthProperty == null) {
      minorTickLengthProperty = new SimpleDoubleProperty(this, "minorTickLength", 8);
      minorTickLengthProperty.addListener(getLayoutValidateListener());
    }
    return minorTickLengthProperty;
  }

  public double getMinorTickLength(){
    return minorTickLengthProperty == null ? 8 : minorTickLengthProperty.get();
  }

  public void setMinorTickLength(final double value){
    minorTickLengthProperty().set(value);
  }

  private DoubleProperty minorTickLengthProperty;




  /**
   * minor tickの可視性。デフォルトはtrue
   * @return
   */
  public BooleanProperty minorTickVisibleProperty(){
    if (minorTickVisibleProperty == null) {
      minorTickVisibleProperty = new SimpleBooleanProperty(this, "minorTickVisible", true);
      minorTickVisibleProperty.addListener(getLayoutValidateListener());
    }
    return minorTickVisibleProperty;
  }

  public boolean isMinorTickVisible(){
    return minorTickVisibleProperty == null ? true : minorTickVisibleProperty.get();
  }

  public void setMinorTickVisible(final boolean value){
    minorTickVisibleProperty().set(value);
  }

  private BooleanProperty minorTickVisibleProperty;



  /**
   * tickと数値ラベルの間の空間。
   * CSS化したい
   * @return
   */
  public DoubleProperty tickLabelGapProperty(){
    if (tickLabelGapProperty == null) {
      tickLabelGapProperty = new SimpleDoubleProperty(this, "tickLabelGap", 10);
      tickLabelGapProperty.addListener(getLayoutValidateListener());
    }
    return tickLabelGapProperty;
  }

  public double getTickLabelGap(){
    return tickLabelGapProperty == null ? 10 : tickLabelGapProperty.get();
  }

  public void setTickLabelGap(final double value){
    tickLabelGapProperty().set(value);
  }

  private DoubleProperty tickLabelGapProperty;



  /**
   * 数値ラベルの回転角度。
   * CSS化したい
   * @return
   */
  public DoubleProperty tickLabelRotateProperty(){
    if (tickLabelRotateProperty == null) {
      tickLabelRotateProperty = new SimpleDoubleProperty(this, "tickLabelRotate", 0);
      tickLabelRotateProperty.addListener(getLayoutValidateListener());
    }
    return tickLabelRotateProperty;
  }

  public double getTickLabelRotate(){
    return tickLabelRotateProperty == null ? 0 : tickLabelRotateProperty.get();
  }

  public void setTickLabelRotate(final double value){
    tickLabelRotateProperty().set(value);
  }

  private DoubleProperty tickLabelRotateProperty;


  /**
   * スクロールバーを表示するかどうか
   * @return
   */
  public BooleanProperty scrollBarVisibleProperty(){
    if (scrollBarVisibleProperty == null) {
      scrollBarVisibleProperty = new SimpleBooleanProperty(this, "scrollBarVisible", true);
    }
    return scrollBarVisibleProperty;
  }

  public boolean isScrollBarVisible(){
    return scrollBarVisibleProperty == null ? true : scrollBarVisibleProperty.get();
  }

  public void setScrollBarVisible(final boolean value){
    scrollBarVisibleProperty().set(value);
  }

  private BooleanProperty scrollBarVisibleProperty;

  //-----------------layoutにしか関係ないデータここまで--------------------


  /**
   * 方向の情報
   * @return
   */
  public ObjectProperty<Orientation> orientationProperty(){
    if (orientationProperty == null) {
      orientationProperty = new SimpleObjectProperty<Orientation>(
          this, "orientation", Orientation.HORIZONTAL){
        @Override
        public void set(final Orientation newValue){
          if(newValue==null) {
            return;
          }
          super.set(newValue);
        }
      };
      orientationProperty.addListener(getDataValidateListener());
    }
    return orientationProperty;
  }

  public Orientation getOrientation(){
    return orientationProperty == null ?
        Orientation.HORIZONTAL : orientationProperty.get();
  }

  public void setOrientation(final Orientation value){
    orientationProperty().set(value);
  }
  public boolean isHorizontal(){
    return getOrientation()==Orientation.HORIZONTAL;
  }

  public boolean isVertical(){
    return getOrientation()==Orientation.VERTICAL;
  }

  private ObjectProperty<Orientation> orientationProperty;




  /**
   * Side
   * @return
   */
  public ObjectProperty<Side> sideProperty(){
    if (sideProperty == null) {
      sideProperty = new SimpleObjectProperty<>(this, "side",
          getOrientation()!=Orientation.VERTICAL?Side.BOTTOM:Side.LEFT);
    }
    return sideProperty;
  }

  public Side getSide(){
    return sideProperty == null ?
        getOrientation()!=Orientation.VERTICAL?Side.BOTTOM:Side.LEFT :
      sideProperty.get();
  }

  public void setSide(final Side value){
    sideProperty().set(value);
  }

  private ObjectProperty<Side> sideProperty;

  /**
   * データの最大値（表示されているとは限らない）
   * @return
   */
  public DoubleProperty maxValueProperty(){
    if (maxValueProperty == null) {
      maxValueProperty = new SimpleDoubleProperty(this, "maxValue", 1);
      maxValueProperty.addListener(getDataValidateListener());
    }
    return maxValueProperty;
  }

  public double getMaxValue(){
    return maxValueProperty == null ? 1 : maxValueProperty.get();
  }

  public void setMaxValue(final double value){
    maxValueProperty().set(value);
  }

  private DoubleProperty maxValueProperty;


  /**
   * データの最小値の値（表示されているとは限らない）
   * @return
   */
  public DoubleProperty minValueProperty(){
    if (minValueProperty == null) {
      minValueProperty = new SimpleDoubleProperty(this, "minValue", 0);
      minValueProperty.addListener(getDataValidateListener());
    }
    return minValueProperty;
  }

  public double getMinValue(){
    return minValueProperty == null ? 0 : minValueProperty.get();
  }

  public void setMinValue(final double value){
    minValueProperty().set(value);
  }

  private DoubleProperty minValueProperty;


  /**
   * 実際に表示されている範囲の最小値。
   * NaNの時はminValueに同じ
   * @return
   */
  public DoubleProperty lowerValueProperty(){
    if (lowerValueProperty == null) {
      lowerValueProperty = new SimpleDoubleProperty(this, "lowerValue", Double.NaN);
      lowerValueProperty.addListener(getDataValidateListener());
    }
    return lowerValueProperty;
  }

  public double getLowerValue(){
    return lowerValueProperty == null ? Double.NaN : lowerValueProperty.get();
  }

  public void setLowerValue(final double value){
    lowerValueProperty().set(value);
  }

  private DoubleProperty lowerValueProperty;



  /**
   * 表示範囲を0より大、1以下で指定する。
   * 1の時、全ての範囲が表示され、スクロールバーは非表示になる
   * 必ずしも、ここで設定した値とscrollVisibleAmountが一致するとは限らない。
   * @return
   */
  public DoubleProperty visibleAmountProperty(){
    if (visibleAmountProperty == null) {
      visibleAmountProperty = new SimpleDoubleProperty(this, "visibleAmount", 1){
        @Override
        public void set(double newValue){
          if(newValue > 1) {
            newValue=1;
          } else if(newValue<=0) {
            return;
          }
          super.set(newValue);
        }
      };
      visibleAmountProperty.addListener(getDataValidateListener());
    }
    return visibleAmountProperty;
  }

  public double getVisibleAmount(){
    return visibleAmountProperty == null ? 1 : visibleAmountProperty.get();
  }

  public void setVisibleAmount(final double value){
    visibleAmountProperty().set(value);
  }

  private DoubleProperty visibleAmountProperty;



  /**
   * 実際に表示されている最大値
   * @return
   */
  public ReadOnlyDoubleProperty upperValueProperty(){
    return upperValueWrapper.getReadOnlyProperty();
  }

  public double getUpperValue(){
    return upperValueWrapper.get();
  }

  protected void setUpperValue(final double value){
    upperValueWrapper.set(value);
  }

  protected ReadOnlyDoubleWrapper upperValueWrapper(){
    return upperValueWrapper;
  }

  private ReadOnlyDoubleWrapper upperValueWrapper =
      new ReadOnlyDoubleWrapper(this, "upperValue", 1);


  /**
   * lowerValueを実際に利用可能な数値に変換して返す
   * @param up 最大値
   * @return
   */
  protected final double computeLowerValue(double up){
    double d = getLowerValue();
    final double m = getMinValue();
    if(up!=up){
      up = getMaxValue();
    }
    if(d!=d) {
      d = m;
    }
    if(d > up) {
      d = up;
    }
    if(d < m) {
      return m;
    } else {
      return d;
    }
  }



  private InvalidationListener scrollValueListener;
  private InvalidationListener getScrollValueListener(){
    if(scrollValueListener == null){
      scrollValueListener = new InvalidationListener(){
        @Override
        public void invalidated(final Observable observable){
          if(!nowLayout){
            if(scroll!=null&&observable==scroll.valueProperty()){
              final double position = scroll.getValue();
              final double size = getScrollVisibleAmount();
              if(position == -1 || size == 1){
                setLowerValue(Double.NaN);
                setVisibleAmount(1);
              }else{
                final double p = isHorizontal()?position:1-position;
                final double d=calcLowValue(p, size);
                setLowerValue(d);
              }
            }else if(scroll!=null){
              final double d = isHorizontal()?
                  getScrollBarValue():
                    1-getScrollBarValue();
              scroll.setValue(d);
             }
          }
        }
      };
    }
    return scrollValueListener;
  }
  /**
   * スクロールバーが変更されたときに呼び出されるメソッド。
   * 表示の最小値を計算する。
   * @param value scrollBarValueに相当する値
   * @param amount scrollVisibleAmountに相当する値
   * @return
   */
  protected double calcLowValue(final double value,final double amount){
    final double max = getMaxValue();
    final double min = getMinValue();
    final double l = max-min;
    final double bar = l*amount;
    final double low = (l-bar)*value + min;
    return low;
  }

  /**
   * スクロールバーの表示位置のプロパティ。縦方向の場合、1からこの値を引いた値を利用する。
   * -1の時、非表示となる。
   * bindする際にはbindBidirectionalを用いること
   * @return
   */
  protected DoubleProperty scrollBarValueProperty(){
    if (scrollBarValueProperty == null) {
      scrollBarValueProperty = new SimpleDoubleProperty(this, "scrollBarPosition", -1);
      scrollBarValueProperty.addListener(getScrollValueListener());
    }
    return scrollBarValueProperty;
  }

  public double getScrollBarValue(){
    if(scrollBarValueProperty == null) {
      return -1;
    }
    return scrollBarValueProperty.get();
  }

  protected void setScrollBarValue(final double value){
    scrollBarValueProperty().set(value);
  }

  private DoubleProperty scrollBarValueProperty;


  /**
   * スクロールバーのvisibleAmountを0～1で表現する。
   * この値は
   * @return
   */
  public ReadOnlyDoubleProperty scrollVisibleAmountProperty(){
    return scrollVisibleAmountWrapper().getReadOnlyProperty();
  }

  public double getScrollVisibleAmount(){
    return scrollBarSizeWrapper == null ? 1 : scrollBarSizeWrapper.get();
  }

  protected void setScrollVisibleAmount(final double value){
    scrollVisibleAmountWrapper().set(value);
  }

  protected ReadOnlyDoubleWrapper scrollVisibleAmountWrapper(){
    if (scrollBarSizeWrapper == null) {
      scrollBarSizeWrapper = new ReadOnlyDoubleWrapper(this, "scrollBarSize", 1);
      scrollBarSizeWrapper.addListener(getScrollValueListener());
    }
    return scrollBarSizeWrapper;
  }

  private ReadOnlyDoubleWrapper scrollBarSizeWrapper;



  /**
   * Axisの構成情報を書き換えるべきデータに対して付加するリスナ
   * @return
   */
  protected final InvalidationListener getDataValidateListener(){
    return dataValidateListener;
  }

  protected boolean isDataValidate(){
    return dataValidate;
  }

  protected void setDataValidate(final boolean bool){
    dataValidate = bool;
  }

  /** 状態の正当性を示すプロパティ*/
  private boolean dataValidate = false;

  protected final InvalidationListener dataValidateListener;




  //----------------------------------------------------------------------
  //                       layout
  //----------------------------------------------------------------------

  protected static class AxisLabel{
    private Node node;//複雑な形状のラベルを許可するために単にNode
    private boolean managed=false;
    private boolean beforeVisible = true;
    public Node getNode(){return node;}
    public void setNode(final Node node){this.node = node;}
    private boolean isManaged(){return managed;}
    private void setManaged(final boolean b){managed = true;}
    private boolean isBeforeVisible(){return beforeVisible;}
    private void setBeforeVisible(final boolean b){beforeVisible = b;}
    private double id;
    /**文字列等の比較以外で同値性を確認するための数値を設定する*/
    public void setID(final double id){this.id = id;}
    /**文字列等の比較以外で同値性を確認するための数値を得る*/
    public double getID(){return id;}
    /**設定されているIDと等しいか調べる*/
    public boolean match(final double id){return this.id == id;}
  }

  private Group lineGroup,labelGroup=new Group();
  private Path majorTickPath,minorTickPath;
  private Line baseLine;
  private ScrollBar scroll;

  private ObservableList<AxisLabel> labels;

  protected ObservableList<AxisLabel> getLabels(){
    if(labels==null){
      labels=FXCollections.observableArrayList();
      labels.addListener(new ListChangeListener<AxisLabel>(){
        @Override
        public void onChanged(final Change<? extends AxisLabel> c){
          final ObservableList<Node> list = labelGroup.getChildren();
          final DoubleProperty rp = tickLabelRotateProperty();
          while(c.next()){

            for(final AxisLabel a:c.getRemoved()){
              list.remove(a.getNode());
              a.setManaged(false);
              a.getNode().rotateProperty().unbind();
            }
            for(final AxisLabel a:c.getAddedSubList()){
              list.add(a.getNode());
              a.getNode().setVisible(false);
              a.getNode().rotateProperty().bind(rp);
            }
          }
        }
      });
    }
    return labels;
  }


  @Override
  protected double computePrefWidth(final double height){
    if(isHorizontal()){
      return 150d;
    }else{
      final double w1 = linesPrefSize();
      final double w2 = scroll!=null && scroll.isVisible()?scroll.prefWidth(height):0;

      layoutChildren(lastLayoutWidth, height);
      final double w3 = labelGroup.prefWidth(height);


      return w1+w2+w3;
    }
  }

  @Override
  protected double computePrefHeight(final double width){
    if(isVertical()){
      return 150d;
    }else{
      final double h1 = linesPrefSize();
      final double h2 = scroll!=null && scroll.isVisible()? scroll.prefHeight(width):0;

      layoutChildren(width, lastLayoutHeight);
      final double h3 = labelGroup.prefHeight(width);
      return h1+h2+h3;
    }
  }


  /**
   * layoutChildrenから呼び出されます。
   * このAxisのレイアウトを実際に行うメソッドです。
   */
  protected void layoutAxis(final double width,final double height){
    if(lineGroup == null){
      lineGroup = new Group();
      lineGroup.setAutoSizeChildren(false);
      labelGroup.setAutoSizeChildren(false);
      majorTickPath = new Path();
      minorTickPath = new Path();
      baseLine = new Line();

      scroll = new ScrollBar();
      scroll.orientationProperty().bind(orientationProperty());
      scroll.visibleProperty().bind(
          Bindings.createBooleanBinding(new Callable<Boolean>(){
        @Override
        public Boolean call() throws Exception{
          return isScrollBarVisible() &&
              getScrollBarValue()!=-1 &&
              getScrollVisibleAmount()!=1;
        }
      }, scrollBarValueProperty(),
      scrollBarVisibleProperty(),scrollVisibleAmountWrapper()));
      scroll.valueProperty().addListener(getScrollValueListener());
      scroll.setMin(0);
      scroll.setMax(1);
      scroll.visibleAmountProperty().bind(scrollVisibleAmountProperty());
      if(getScrollBarValue()!=-1 && getScrollVisibleAmount()!=1){
        scroll.setValue(getOrientation()!=Orientation.VERTICAL?
            getScrollBarValue():
              1-getScrollBarValue());
      }
      majorTickPath.getStyleClass().setAll("axis-tick-mark");
      minorTickPath.getStyleClass().setAll("axis-minor-tick-mark");
      baseLine.getStyleClass().setAll("axis-line");
      lineGroup.getChildren().addAll(majorTickPath,minorTickPath,baseLine);

      getChildren().addAll(lineGroup,labelGroup,scroll);
    }

    layoutLines(width,height);
    layoutLabels(width, height);
    layoutGroups(width,height);
  }

  private void layoutLabels(final double width,final double height){
    if(labels==null) {
      return;
    }
    final ObservableList<AxisLabel> labels = this.labels;
    final List<Double> majorTicks = getMajorTicks();
    final double lastL = getAxisLength(lastLayoutWidth, lastLayoutHeight);
    final double l = getAxisLength(width, height);
    final boolean isH=isHorizontal();
    int firstIndex = -1;//重なりを検出する基準位置
    Side s = getSide();
    if(isH){
      if(s.isVertical()){
        s = Side.BOTTOM;
      }
    }else{
      if(s.isHorizontal()){
        s = Side.LEFT;
      }
    }


    for(int i=0,e = majorTicks.size();i<e;i++){
      final AxisLabel a = labels.get(i);
      final double d = majorTicks.get(i);
      if(firstIndex==-1 && a.isManaged() && a.isBeforeVisible()){
        firstIndex =i;
      }
      a.setManaged(true);
      //位置を合わせる
      final Node n = a.getNode();
      n.setLayoutX(0);
      n.setLayoutY(0);
      final Bounds bounds= n.getBoundsInParent();
      if(isH){
        final double cx = (bounds.getMinX()+bounds.getMaxX())*0.5;
        n.setLayoutX(d-cx);
        if(s == Side.BOTTOM){
          //上を合わす
          final double bottom = bounds.getMinY();
          n.setLayoutY(-bottom);
        }else{
          //下を合わす
          final double top = bounds.getMaxY();
          n.setLayoutY(-top);
        }
      }else{
        final double cy = (bounds.getMinY()+bounds.getMaxY())*0.5;
        n.setLayoutY(d-cy);
        if(s == Side.LEFT){
          //右端を合わす
          final double right = bounds.getMaxX();
          n.setLayoutX(-right);
        }else{
          //左端を合わす
          final double left = bounds.getMinX();
          n.setLayoutX(-left);
        }
      }
    }

    //大きさが変わったときは前回の履歴を参照しない。
    if(lastL != l){
      firstIndex=-1;
    }

    //重なるラベルを不可視にする

    for(int k=0;k<2;k++){
      //TODO 本当に重なっている領域で判定するようにしたい
      Bounds base = null;
      if(firstIndex!=-1){
        base = labels.get(firstIndex).getNode().getBoundsInParent();
      }
      final int end = k==0?-1:labels.size();
      int i = k==0? firstIndex-1:firstIndex+1;
      final int add = k==0?-1:1;
      if(i < 0){
        i = -1;
      }

      for(;i!=end;i+=add){
        final AxisLabel a = labels.get(i);
        final Node n = a.getNode();
        final Bounds bounds = n.getBoundsInParent();
        if(base == null){
          n.setVisible(true);
          a.setBeforeVisible(true);
          base = bounds;
        }else{
          final boolean visible =!base.intersects(bounds);
          a.setBeforeVisible(visible);
          n.setVisible(visible);
          if(visible) {
            base = bounds;
          }
        }
      }
    }
  }


  private void layoutGroups(final double width,final double height){
    final boolean ish = isHorizontal();
    if(ish){
      if(getSide()!=Side.TOP){//BOTTOM
        double y = 0;
        if(scroll.isVisible()){
          y = scroll.prefHeight(-1);
          scroll.resizeRelocate(0, 0, width, y);
        }
        lineGroup.setLayoutX(0);
        lineGroup.setLayoutY(y);
        y+=lineGroup.prefHeight(-1)+getTickLabelGap();
        labelGroup.setLayoutX(0);
        labelGroup.setLayoutY(y);
      }else{

      }
    }else{
      if(getSide()!=Side.RIGHT){//LEFT
        double x = width;
        if(scroll.isVisible()){
          final double w = scroll.prefWidth(-1);
          x = x-w;
          scroll.resizeRelocate(x, 0, w, height);
        }

        lineGroup.setLayoutX(x);
        lineGroup.setLayoutY(0);
        x -= getTickLabelGap()+lineGroup.prefWidth(-1);
        labelGroup.setLayoutX(x);
        labelGroup.setLayoutY(0);
      }else{

      }
    }

  }

  private double linesPrefSize(){
    return max(getMajorTickLength(), getMinorTickLength());
  }
  private void layoutLines(final double width,final double height){

    final boolean ish = isHorizontal();
    final double l = getAxisLength(width, height);
    baseLine.setEndX(ish?l:0);
    baseLine.setEndY(ish?0:l);

    final double al=getMajorTickLength();
    final double il=getMinorTickLength();
    final boolean isIV=il >0 && isMinorTickVisible();
    final Side s = getSide();
    final int k =
        ish?s!=Side.TOP?1:-1:
            s!=Side.RIGHT?-1:1;

    if(isIV){
      final List<Double> list = getMinorTicks();
      final ObservableList<PathElement> elements = minorTickPath.getElements();
      if(elements.size() > list.size()*2){
        elements.remove(list.size()*2, elements.size());
      }

      final int eles = elements.size();
      final int ls = list.size();
      for(int i=0;i<ls;i++){
        final double d = list.get(i);
        MoveTo mt;LineTo lt;
        if(i*2 < eles){
          mt = (MoveTo)elements.get(i*2);
          lt = (LineTo)elements.get(i*2+1);
        }else{
          mt = new MoveTo();
          lt = new LineTo();
          elements.addAll(mt,lt);
        }
        double x1,x2,y1,y2;
        if(ish){
          x1 = x2 = d;
          y1 = 0;
          y2 = il*k;
        }else{
          x1 = 0;
          x2 = il*k;
          y1 = y2 = d;
        }
        mt.setX(x1);mt.setY(y1);
        lt.setX(x2);lt.setY(y2);
      }
    }else{
      minorTickPath.setVisible(false);
    }

    final List<Double> list=getMajorTicks();
    final ObservableList<PathElement> elements = majorTickPath.getElements();
    if(elements.size() > list.size()*2){
      elements.remove(list.size()*2, elements.size());
    }

    final int eles = elements.size();
    final int ls = list.size();
    for(int i=0;i<ls;i++){
      final double d = list.get(i);
      MoveTo mt;LineTo lt;
      if(i*2 < eles){
        mt = (MoveTo)elements.get(i*2);
        lt = (LineTo)elements.get(i*2+1);
      }else{
        mt = new MoveTo();
        lt = new LineTo();
        elements.addAll(mt,lt);
      }
      double x1,x2,y1,y2;
      if(ish){
        x1 = x2 = d;
        y1 = 0;
        y2 = al*k;
      }else{
        x1 = 0;
        x2 = al*k;
        y1 = y2 = d;
      }
      mt.setX(x1);mt.setY(y1);
      lt.setX(x2);lt.setY(y2);
    }
  }


  /**
   * major tickを表示する座標を返します
   * @return unmodifiable list
   */
  public abstract List<Double> getMajorTicks();
  /**
   * minor tickを表示する座標を返します
   * @return unmodifiable list
   */
  public abstract List<Double> getMinorTicks();

  /**
   * major tickから次のmajor tickまでの間をFillするかどうか。
   * getMajorTicksのsizeと同じ長さのリストであること。
   * @return
   */
  public abstract List<Boolean> getMajorTicksFill();

  /**
   * レイアウトを構成すべき情報に対して付加すべきリスナ
   * @return
   */
  protected InvalidationListener getLayoutValidateListener(){
    if (layoutValidateListener == null) {
      layoutValidateListener = new InvalidationListener(){
        @Override
        public void invalidated(final Observable observable){
          if (isLayoutValidate()) {
            setLayoutValidate(false);
            requestLayout();
          }
        }
      };
    }
    return layoutValidateListener;
  }

  protected boolean isLayoutValidate(){
    return layoutValidate;
  }

  protected void setLayoutValidate(final boolean bool){
    layoutValidate = bool;
  }

  /** 状態の正当性を示すプロパティ*/
  private boolean layoutValidate = false;

  /** 直接フィールドを利用せずに、 getValidateListener() を利用すること*/
  private InvalidationListener layoutValidateListener = null;



  /**
   * 数値をラベルに変換するformat
   * @return
   */
  public ObjectProperty<LabelFormat> formatProperty(){
    if (formatterProperty == null) {
      formatterProperty = new SimpleObjectProperty<>(this, "formatter", null);
      formatterProperty.addListener(getLayoutValidateListener());
    }
    return formatterProperty;
  }

  public LabelFormat getLabelFormat(){
    return formatterProperty == null ? null : formatterProperty.get();
  }

  public void setgetLabelFormat(final LabelFormat value){
    formatProperty().set(value);
  }

  private ObjectProperty<LabelFormat> formatterProperty;



  /**
   * 数値からラベル文字列に変換するインターフェース
   * @author nodamushi
   *
   */
  public static interface LabelFormat{
    public Node format(double value);
  }

  public static class SimpleLabelFormat implements LabelFormat{
    private DecimalFormat integerFormatter =
        new DecimalFormat("###,###,###,###,###,###");
    @Override
    public Node format(final double value){
      final double f = floor(value);
      if(f == value){
        final String str = integerFormatter.format(value);
        return new Text(str);
      }
      return new Text(String.valueOf(value));
    }
  }

  /**
   *
   * TICK_UNIT_DEFAULTSを利用することが前提のフォーマッタ
   * @author nodemushi
   *
   */
  protected static class DefaultUnitLabelFormat implements LabelFormat{

    private DecimalFormat formatter;
    private int index = -1;
    public void setUnitIndex(final int index){
      if(this.index !=index){
        formatter = getFormatter(index);
        this.index = index;
      }
    }

    @Override
    public Node format(final double value){
      final String str = formatter.format(value);
      return new Text(str);
    }
  }


  //------------------------------------------------------------

  // javafx.scene.chart.NumberAxisのコードから引用

  /** We use these for auto ranging to pick a user friendly tick unit. We handle tick units in the range of 1e-10 to 1e+12 */
  protected static final double[] TICK_UNIT_DEFAULTS = {
      1.0E-10d, 2.5E-10d, 5.0E-10d, 1.0E-9d, 2.5E-9d, 5.0E-9d, 1.0E-8d, 2.5E-8d, 5.0E-8d, 1.0E-7d, 2.5E-7d, 5.0E-7d,
      1.0E-6d, 2.5E-6d, 5.0E-6d, 1.0E-5d, 2.5E-5d, 5.0E-5d, 1.0E-4d, 2.5E-4d, 5.0E-4d, 0.0010d, 0.0025d, 0.0050d,
      0.01d, 0.025d, 0.05d, 0.1d, 0.25d, 0.5d, 1.0d, 2.5d, 5.0d, 10.0d, 25.0d, 50.0d, 100.0d, 250.0d, 500.0d,
      1000.0d, 2500.0d, 5000.0d, 10000.0d, 25000.0d, 50000.0d, 100000.0d, 250000.0d, 500000.0d, 1000000.0d,
      2500000.0d, 5000000.0d, 1.0E7d, 2.5E7d, 5.0E7d, 1.0E8d, 2.5E8d, 5.0E8d, 1.0E9d, 2.5E9d, 5.0E9d, 1.0E10d,
      2.5E10d, 5.0E10d, 1.0E11d, 2.5E11d, 5.0E11d, 1.0E12d, 2.5E12d, 5.0E12d
  };
  /** These are matching decimal formatter strings */
  private static final String[] TICK_UNIT_FORMATTER_DEFAULTS = {"0.0000000000", "0.00000000000", "0.0000000000",
                                                                "0.000000000", "0.0000000000", "0.000000000",
                                                                "0.00000000", "0.000000000", "0.00000000",
                                                                "0.0000000", "0.00000000", "0.0000000", "0.000000",
                                                                "0.0000000", "0.000000", "0.00000", "0.000000",
                                                                "0.00000", "0.0000", "0.00000", "0.0000", "0.000",
                                                                "0.0000", "0.000", "0.00", "0.000", "0.00", "0.0",
                                                                "0.00", "0.0", "0", "0.0", "0", "#,##0"};

  private static DecimalFormat getFormatter(final int rangeIndex) {
    if (rangeIndex < 0) {
      return new DecimalFormat();
    } else if(rangeIndex >= TICK_UNIT_FORMATTER_DEFAULTS.length) {
      return new DecimalFormat(TICK_UNIT_FORMATTER_DEFAULTS[TICK_UNIT_FORMATTER_DEFAULTS.length-1]);
    } else {
      return new DecimalFormat(TICK_UNIT_FORMATTER_DEFAULTS[rangeIndex]);
    }
  }

  //-------------------------引用終わり------------------------------








}
