package nodamushi.jfx.chart.linechart;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;

public class Legend extends Control{


  /**
   *
   * @return
   */
  public final ObjectProperty<Orientation> orientationProperty(){
    if (orientationProperty == null) {
      orientationProperty =
          new SimpleObjectProperty<Orientation>(this, "orientation", Orientation.HORIZONTAL){
        @Override
        public void set(final Orientation v){
          if(v==null){
            return;
          }
          super.set(v);
        }
        @Override
        protected void invalidated(){
          requestLayout();
          super.invalidated();
        }
      };
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

  /**
   *
   * @return
   */
  public final ObjectProperty<ObservableList<LineChartData>> dataListProperty(){
    if (dataListProperty == null) {
      dataListProperty = new SimpleObjectProperty<>(this, "dataList", null);
      dataListProperty.addListener(new ChangeListener<ObservableList<LineChartData>>(){
        InvalidationListener lis = new InvalidationListener(){
          @Override
          public void invalidated(final Observable arg0){
            requestLayout();
          }
        };
        @Override
        public void changed(
            final ObservableValue<? extends ObservableList<LineChartData>> c ,
                final ObservableList<LineChartData> old ,
                final ObservableList<LineChartData> newv){
          if(old!=null){
            old.removeListener(lis);
          }
          if(newv!=null){
            newv.addListener(lis);
          }
          requestLayout();
        }
      });
    }
    return dataListProperty;
  }

  public final ObservableList<LineChartData> getDataList(){
    return dataListProperty == null ? null : dataListProperty.get();
  }

  public final void setDataList(final ObservableList<LineChartData> value){
    dataListProperty().set(value);
  }

  private ObjectProperty<ObservableList<LineChartData>> dataListProperty;

}
