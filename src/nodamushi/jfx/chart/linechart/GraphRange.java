package nodamushi.jfx.chart.linechart;

import static java.lang.Math.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.shape.Rectangle;

public class GraphRange implements GraphShape{

  private Rectangle rect = new Rectangle();

  @Override
  public Rectangle getNode(){
    return rect;
  }

  public ObservableList<String> getStyleClass(){
    return rect.getStyleClass();
  }

  /**
   * 方向
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


  @Override
  public Orientation getAxisOrientation(){
    return getOrientation()==Orientation.VERTICAL?Orientation.HORIZONTAL:Orientation.VERTICAL;
  }



  /**
   * 範囲の片方の値
   * @return
   */
  public DoubleProperty value1Property(){
    if (value1Property == null) {
      value1Property = new SimpleDoubleProperty(this, "value1", 0);
    }
    return value1Property;
  }

  public double getValue1(){
    return value1Property == null ? 0 : value1Property.get();
  }

  public void setValue1(final double value){
    value1Property().set(value);
  }

  private DoubleProperty value1Property;



  /**
   * 範囲の片方の値
   * @return
   */
  public DoubleProperty value2Property(){
    if (value2Property == null) {
      value2Property = new SimpleDoubleProperty(this, "value2", 1);
    }
    return value2Property;
  }

  public double getValue2(){
    return value2Property == null ? 1 : value2Property.get();
  }

  public void setValue2(final double value){
    value2Property().set(value);
  }

  private DoubleProperty value2Property;



  /**
   * 可視性
   * @return
   */
  public BooleanProperty visibleProperty(){
    if (visibleProperty == null) {
      visibleProperty = new SimpleBooleanProperty(this, "visible", true);
    }
    return visibleProperty;
  }

  public boolean isVisible(){
    return visibleProperty == null ? true : visibleProperty.get();
  }

  public void setVisible(final boolean value){
    visibleProperty().set(value);
  }

  private BooleanProperty visibleProperty;

  @Override
  public void setNodeProperty(final Axis axis ,final double w ,final double h){
    final double v1 =getValue1();
    final double v2 = getValue2();
    final Orientation orientation = getOrientation();
    double d1 = axis.getDisplayPosition(v1);

    if(Double.isInfinite(d1)){
      if(d1 < 0){
        d1 = 0;
      }else{
        d1 = orientation==Orientation.HORIZONTAL?w:h;
      }
    }


    double d2 = axis.getDisplayPosition(v2);
    if(Double.isInfinite(d2)){
      if(d2 < 0){
        d2 = 0;
      }else{
        d2 = orientation==Orientation.HORIZONTAL?w:h;
      }
    }

    final Rectangle rect = getNode();
    if(d1!=d1 || d2!=d2 || !isVisible()){
      rect.setVisible(false);
      return;
    }

    rect.setVisible(true);
    final double i = min(d1, d2),a=max(d1, d2);

    if(orientation==Orientation.HORIZONTAL){
      rect.setWidth(ceil(a-i));
      rect.setHeight(h);
      rect.setLayoutX(floor(i));
      rect.setLayoutY(0);
    }else{
      rect.setWidth(w);
      rect.setHeight(ceil(a-i));
      rect.setLayoutX(0);
      rect.setLayoutY(floor(i));
    }
  }
}

