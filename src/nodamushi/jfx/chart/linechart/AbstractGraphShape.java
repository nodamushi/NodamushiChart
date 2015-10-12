package nodamushi.jfx.chart.linechart;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

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

  protected final ReadOnlyBooleanWrapper validateWrapper(){
    if (validateWrapper == null) {
      validateWrapper = new ReadOnlyBooleanWrapper(this, "validate", false);
    }
    return validateWrapper;
  }

  private ReadOnlyBooleanWrapper validateWrapper;

  protected final InvalidationListener getInvalidateListener(){
    if (invalidateSetter == null) {
      invalidateSetter =o->{
        if (isValidate()) {
          setValidate(false);
        }
      };

    }
    return invalidateSetter;
  }


  private InvalidationListener invalidateSetter = null;



}
