package nodamushi.jfx.chart.linechart;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.shape.Line;

/**
 * グラフ上に表示する線
 * @author nodamushi
 *
 */
public class GraphLine implements GraphShape{
  private Line line =new Line();

  @Override
  public Line getNode(){
    return line;
  }

  @Override
  public void setNodeProperty(final Axis axis ,final double w ,final double h){
    final double v = getValue();
    final Line l = getNode();
    if(Double.isInfinite(v)|| v!=v || !isVisible()){
      l.setVisible(false);
      return;
    }
    if(getOrientation()!=Orientation.VERTICAL){
      //横方向
      final double y=axis.getDisplayPosition(v);
      if(Double.isInfinite(y)|| y!=y|| y<0 || y>h){
        l.setVisible(false);
        return;
      }
      l.setStartX(0);
      l.setEndX(w);
      l.setStartY(y);
      l.setEndY(y);
    }else{
      final double x=axis.getDisplayPosition(v);
      if(Double.isInfinite(x) || x!=x || x <0 || x >w){
        l.setVisible(false);
        return;
      }
      l.setStartY(0);
      l.setEndY(h);
      l.setStartX(x);
      l.setEndX(x);
    }
    l.setVisible(true);

  }

  /**
   * グラフ上の値
   * @return
   */
  public DoubleProperty valueProperty(){
    if (valueProperty == null) {
      valueProperty = new SimpleDoubleProperty(this, "value", 0){
        @Override
        public double get(){
          setValidate(true);
          return super.get();
        }
      };
      valueProperty.addListener(getValidateListener());
    }
    return valueProperty;
  }

  public double getValue(){
    return valueProperty == null ? 0 : valueProperty.get();
  }

  public void setValue(final double value){
    valueProperty().set(value);
  }

  private DoubleProperty valueProperty;




  /**
   * 可視性
   * @return
   */
  public BooleanProperty visibleProperty(){
    if (visibleProperty == null) {
      visibleProperty = new SimpleBooleanProperty(this, "visible", true);
      visibleProperty.addListener(getValidateListener());
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



  public ObservableList<String> getStyleClass(){
    return line.getStyleClass();
  }



  /**
   * 縦方向の線か、横方向の線か
   * @return
   */
  public ObjectProperty<Orientation> orientationProperty(){
    if (orientationProperty == null) {
      orientationProperty =
          new SimpleObjectProperty<Orientation>(this, "orientation", null){
        @Override
        public Orientation get(){
          setValidate(true);
          return super.get();
        }
      };
      orientationProperty.addListener(getValidateListener());
    }
    return orientationProperty;
  }

  public Orientation getOrientation(){
    return orientationProperty == null ? null : orientationProperty.get();
  }

  @Override
  public Orientation getAxisOrientation(){
    return getOrientation()==Orientation.VERTICAL?Orientation.HORIZONTAL:Orientation.VERTICAL;
  }

  public void setOrientation(final Orientation value){
    orientationProperty().set(value);
  }

  private ObjectProperty<Orientation> orientationProperty;




  protected InvalidationListener getValidateListener(){
    if (validateListener == null) {
      validateListener = new InvalidationListener(){
        @Override
        public void invalidated(final Observable observable){
          if (isValidate()) {
            setValidate(false);
          }
        }
      };
    }
    return validateListener;
  }

  /** 直接フィールドを利用せずに、 getValidateListener() を利用すること*/
  private InvalidationListener validateListener = null;



  /**
   * 正当性。
   * @return
   */
  public BooleanProperty validateProperty(){
    if (validateProperty == null) {
      validateProperty = new SimpleBooleanProperty(this, "validate", false);
    }
    return validateProperty;
  }

  public boolean isValidate(){
    return validateProperty == null ? false : validateProperty.get();
  }

  public void setValidate(final boolean value){
    validateProperty().set(value);
  }

  private BooleanProperty validateProperty;
}
