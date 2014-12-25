package nodamushi.jfx.chart.linechart;

import static java.lang.Math.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.text.Text;

public class LinerAxis extends LineChartAxis{



  private double lowVal=0;
  private double m;
  @Override
  public double getDisplayPosition(final double v){
    final double d= m * (v-lowVal);
    return isHorizontal()? d:getHeight()-d;
  }

  @Override
  public double getValueForDisplay(double position){
    if(!isHorizontal()){
      position = getHeight()-position;
    }
    return position/m + lowVal;
  }


  private int unitIndex=-1;
  private double lastPUnitSize = Double.NaN;
  private List<Double>
  majours=new ArrayList<>(10),
  majoursU=unmodifiableList(majours),
  minors=new ArrayList<>(100),
  minorsU=unmodifiableList(minors);

  private List<Boolean>
  majoursFill = new ArrayList<>(10),
  majoursFillU = unmodifiableList(majoursFill);


  @Override
  public List<Double> getMajorTicks(){
    return majoursU;
  }

  @Override
  public List<Double> getMinorTicks(){
    return minorsU;
  }

  @Override
  public List<Boolean> getMajorTicksFill(){
    return majoursFillU;
  }

  private double computeUpperValue(final double low){
    final double max = getMaxValue();
    final double a = getVisibleAmount();
    final double min = getMinValue();
    final double ll = max-min;
    return min(low + ll*a,max);
  }

  @Override
  protected void computeAxisProperties(final double width ,final double height){
    majours.clear();
    minors.clear();
    majoursFill.clear();

    final double low = computeLowerValue(getMaxValue());
    final double up = computeUpperValue(low);
    if(low == up || low != low || up!=up){
      noData(width,height);
      return;
    }

    final double len = getAxisLength(width, height);
    lowVal = low;
    setUpperValue(up);
    {//scroll bar
      final double max = getMaxValue();
      final double min = getMinValue();
      if(low == min && max == up){
        setScrollBarValue(-1);
        setScrollVisibleAmount(1);
      }else{
        final double ll = max-min;
        final double l = up-low;
        setScrollBarValue((low-min)/(ll-l));
        setScrollVisibleAmount(l/ll);
      }
    }

    //適当な単位を見つける
    final int mtn = getPrefferedMajorTickNumber();
    final double minu = getMinUnitLength();
    double pUnitLength = len/mtn;
    if(minu >0 && pUnitLength < minu){
      pUnitLength = minu;
    }
    final double pUnitSize = (up-low)/(len/pUnitLength);
    int uindex=unitIndex;//前回の探索結果の再利用
    boolean useBefore=true;

    if(lastPUnitSize != pUnitSize){
      final double[] UNITS = TICK_UNIT_DEFAULTS;
      if(pUnitSize <= UNITS[0]){
        uindex = 0;
      }else if(pUnitSize >= UNITS[UNITS.length-1]){
        uindex = UNITS.length-1;
      } else {
        BLOCK:{
          int l=1,r=UNITS.length-2;
          int m = (l+r>>1);

          while(r-l>1){
            final double d = UNITS[m];
            if(d == pUnitSize){
              uindex = m;
              break BLOCK;
            }
            if(d < pUnitSize){
              l=m;
            }else{
              r=m;
            }
            m =(l+r>>1);
          }

          if(UNITS[r] < pUnitSize){
            uindex = r+1;
          }else if(UNITS[l] > pUnitSize){
            uindex = l;
          }else{
            uindex = r;
          }
        }
      }
      lastPUnitSize = pUnitSize;
      useBefore = uindex == unitIndex;
      unitIndex=uindex;
    }
    final double usize = TICK_UNIT_DEFAULTS[uindex];
    LabelFormat format = getLabelFormat();
    if(format == null){
      format = new DefaultUnitLabelFormat();
      setgetLabelFormat(format);
    }
    if (format instanceof DefaultUnitLabelFormat) {
      final DefaultUnitLabelFormat df = (DefaultUnitLabelFormat) format;
      df.setUnitIndex(uindex);
    }
    final double l = up-low;
    boolean fill = ((int)floor(low/usize) & 1) !=0;
    final double basel = floor(low/usize)*usize;
    final double m = len/l;

    final double majorLength = m*usize;
    final int k = (int)(ceil(up-basel)/usize);

    this.m = m;


    double minorLength;
    int mcount = getPrefferedMinorCount();
    if(!isMinorTickVisible() || mcount <=1) {
      minorLength = -1;
    } else{
      minorLength = majorLength/mcount;
      final double mins = getMinorUnitMinLength();
      if(mins > 0 && mins >= majorLength){
        minorLength = -1;
      }else if(mins > 0 && minorLength < mins){
        mcount = (int)floor(majorLength/mins);
        minorLength = majorLength/mcount;
      }
    }
    final boolean visibleMinor = minorLength!=-1;
    //単位の検索終わり
    final boolean isH = isHorizontal();

    final ObservableList<AxisLabel> labels = getLabels();
    if(!useBefore) {
      labels.clear();
    }

    final ArrayList<AxisLabel> notUse = new ArrayList<>(labels);
    final ArrayList<AxisLabel> labelList=new ArrayList<>(k+1);
    for(int i=0;i<=k+1;i++){
      final double value = basel + usize*i;
      fill =!fill;
      if(value > up){
        break;//i==k
      }
      final double majorpos = m*(value-low);
      if(value >= low){
        majours.add(floor(isH?majorpos:height-majorpos));
        majoursFill.add(fill);
        boolean find =false;
        for(int t = 0,lsize=notUse.size();t<lsize;t++){
          final AxisLabel a = notUse.get(t);
          if(a.match(value)){
            labelList.add(a);
            notUse.remove(t);
            find = true;
            break;
          }
        }
        if(!find){
          final AxisLabel a = new AxisLabel();
          a.setID(value);
          final Node node = format.format(value);
          a.setNode(node);
          labelList.add(a);
        }
      }
      if(visibleMinor){
        for(int count=1;count<mcount;count++){
          final double minorpos = majorpos + count*minorLength;
          if(minorpos < 0) {
            continue;
          }
          if(minorpos >=len) {
            break;
          }
          minors.add(floor(isH?minorpos:height-minorpos));
        }
      }
    }//end for

    //これで大丈夫か？
    labels.removeAll(notUse);
    for(int i=0,e=labelList.size();i<e;i++){
      final AxisLabel axisLabel = labelList.get(i);
      if(!labels.contains(axisLabel)){
        labels.add(i, axisLabel);
      }
    }
  }


  protected void noData(final double width,final double height){
    lastPUnitSize = Double.NaN;
    unitIndex = -1;
    lowVal = 0;
    setUpperValue(1);
    final double len = getAxisLength(width, height);
    m = len;
    majours.add(0d);
    majours.add(getAxisLength(width, height));
    majoursFill.add(true);
    majoursFill.add(false);
    final ObservableList<AxisLabel> labels = getLabels();
    labels.clear();
    AxisLabel l = new AxisLabel();
    l.setID(Double.NaN);
    l.setNode(new Text("0"));
    labels.add(l);
    l = new AxisLabel();
    l.setID(Double.NaN);
    l.setNode(new Text("1"));
    labels.add(l);
  }

  //----------------------------------------------------------------------
  //                       data
  //----------------------------------------------------------------------



  /**
   * major tickが画面に表示される理想個数
   * @return
   */
  public IntegerProperty prefferedMajorTickNumberProperty(){
    if (prefferedMajorTickNumberProperty == null) {
      prefferedMajorTickNumberProperty = new SimpleIntegerProperty(this, "prefferedMajorTickNumber", 10);
      prefferedMajorTickNumberProperty.addListener(getDataValidateListener());
    }
    return prefferedMajorTickNumberProperty;
  }

  public int getPrefferedMajorTickNumber(){
    return prefferedMajorTickNumberProperty == null ? 10 : prefferedMajorTickNumberProperty.get();
  }

  public void setPrefferedMajorTickNumber(final int value){
    prefferedMajorTickNumberProperty().set(value);
  }

  private IntegerProperty prefferedMajorTickNumberProperty;




  /**
   * major tick間の画面距離（ピクセルの事）の最小値の理想。
   * @return
   */
  public DoubleProperty minUnitLengthProperty(){
    if (minUnitLengthProperty == null) {
      minUnitLengthProperty = new SimpleDoubleProperty(this, "minUnitLength", 40);
      minUnitLengthProperty.addListener(getDataValidateListener());
    }
    return minUnitLengthProperty;
  }

  public double getMinUnitLength(){
    return minUnitLengthProperty == null ? 50 : minUnitLengthProperty.get();
  }

  public void setMinUnitLength(final double value){
    minUnitLengthProperty().set(value);
  }

  private DoubleProperty minUnitLengthProperty;





  /**
   * major tick間を何分割するか。表示されるMinor tickはこの数より1少ない。
   * これより大きくなることはない。
   * @return
   */
  public IntegerProperty prefferedMinorCountProperty(){
    if (prefferedMinorCountProperty == null) {
      prefferedMinorCountProperty = new SimpleIntegerProperty(this, "prefferedMinorCount", 10);
    }
    return prefferedMinorCountProperty;
  }

  public int getPrefferedMinorCount(){
    return prefferedMinorCountProperty == null ? 10 : prefferedMinorCountProperty.get();
  }

  public void setPrefferedMinorCount(final int value){
    prefferedMinorCountProperty().set(value);
  }
  private IntegerProperty prefferedMinorCountProperty;


  /**
   * minor tick間の最小画面距離
   * @return
   */
  public DoubleProperty minorUnitMinLengthProperty(){
    if (minorUnitMinLengthProperty == null) {
      minorUnitMinLengthProperty = new SimpleDoubleProperty(this, "minorUnitMinLength", 4);
    }
    return minorUnitMinLengthProperty;
  }

  public double getMinorUnitMinLength(){
    return minorUnitMinLengthProperty == null ? 4 : minorUnitMinLengthProperty.get();
  }

  public void setMinorUnitMinLength(final double value){
    minorUnitMinLengthProperty().set(value);
  }

  private DoubleProperty minorUnitMinLengthProperty;


}
