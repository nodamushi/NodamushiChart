package nodamushi.jfx.chart.linechart;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

/**
 * validateのプロパティを実装したクラス
 * @author nodamushi
 *
 */
public abstract class AbstractGraphShape implements GraphShape{


  @Override
  public final ReadOnlyBooleanProperty invalidateProperty(){
    return invalidateWrapper().getReadOnlyProperty();
  }

  @Override
  public final boolean isInvalidate(){
    return invalidateWrapper == null ? true : invalidateWrapper.get();
  }

  protected final void setInvalidate(final boolean value){
    invalidateWrapper().set(value);
  }

  /**
   * validatePropertyのラッパー
   * @return
   */
  protected final ReadOnlyBooleanWrapper invalidateWrapper(){
    if (invalidateWrapper == null) {
      invalidateWrapper = new ReadOnlyBooleanWrapper(this, "validate", true);
    }
    return invalidateWrapper;
  }

  private ReadOnlyBooleanWrapper invalidateWrapper;

  /**
   * 変化を受け取ったときにvalidateをfalseに変更するリスナー
   * @return
   */
  protected final InvalidationListener getInvalidateListener(){
    if (nameValidateListener == null) {
      nameValidateListener = new InvalidationListener(){
        @Override
        public void invalidated(final Observable observable){
          if (!isInvalidate()) {
            setInvalidate(true);
          }
        }
      };
    }
    return nameValidateListener;
  }


  private InvalidationListener nameValidateListener = null;



}
