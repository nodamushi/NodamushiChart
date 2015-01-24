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
  public final ReadOnlyBooleanProperty validateProperty(){
    return validateWrapper().getReadOnlyProperty();
  }

  @Override
  public final boolean isValidate(){
    return validateWrapper == null ? false : validateWrapper.get();
  }

  protected final void setValidate(final boolean value){
    validateWrapper().set(value);
  }

  /**
   * validatePropertyのラッパー
   * @return
   */
  protected final ReadOnlyBooleanWrapper validateWrapper(){
    if (validateWrapper == null) {
      validateWrapper = new ReadOnlyBooleanWrapper(this, "validate", false);
    }
    return validateWrapper;
  }

  private ReadOnlyBooleanWrapper validateWrapper;

  /**
   * 変化を受け取ったときにvalidateをfalseに変更するリスナー
   * @return
   */
  protected final InvalidationListener getValidateListener(){
    if (nameValidateListener == null) {
      nameValidateListener = new InvalidationListener(){
        @Override
        public void invalidated(final Observable observable){
          if (isValidate()) {
            setValidate(false);
          }
        }
      };
    }
    return nameValidateListener;
  }


  private InvalidationListener nameValidateListener = null;



}
