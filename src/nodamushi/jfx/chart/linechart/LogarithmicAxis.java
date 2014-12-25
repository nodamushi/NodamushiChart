package nodamushi.jfx.chart.linechart;

import static java.lang.Math.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

/**
 * 対数グラフ用の軸。利用可能範囲は0より大
 * @author nodamushi
 *
 */
public class LogarithmicAxis extends LineChartAxis{

  private List<Double>
  majours=new ArrayList<>(10),
  majoursU=unmodifiableList(majours),
  minors=new ArrayList<>(100),
  minorsU=unmodifiableList(minors);
  private List<Boolean>
  majoursFill = new ArrayList<>(10),
  majoursFillU = unmodifiableList(majoursFill);

  private double loglow,logup;
  private double length;

  private static final double[] LOG10 = {
    log10(2),log10(3),log10(4),log10(5),
    log10(6),log10(7),log10(8),log10(9)
  };

  private double computeLogUpperValue(final double logmax,final double logmin,final double loglow){
    final double a = getVisibleAmount();
    final double l = logmax-logmin;
    return min(logmax, loglow+l*a);
  }

  @Override
  protected void computeAxisProperties(final double width ,final double height){
    majours.clear();
    minors.clear();
    majoursFill.clear();
    double min = getMinValue(),max = getMaxValue();
    double low = computeLowerValue(Double.NaN);
    final double defaultMin = getDefaultMinValue();
    if(min <=0){
      min = defaultMin;
    }

    if(max <=0){
      max = defaultMin;
    }

    if(low <=0){
      low = defaultMin;
    }

    final double l = getAxisLength(width, height);


    if(min == max ||  l==0){
      length = 1;
      loglow = 1;
      logup = 2;
      setUpperValue(100);
      majours.add(0d);
      majours.add(l);
      majoursFill.add(true);
      majoursFill.add(false);
      final ObservableList<AxisLabel> labels = getLabels();
      labels.clear();
      AxisLabel al = new AxisLabel();
      al.setID(Double.NaN);
      al.setNode(new Text("0"));
      labels.add(al);
      al = new AxisLabel();
      al.setID(Double.NaN);
      al.setNode(new Text("1"));
      labels.add(al);
      return;
    }

    final double logmin = log10(min),logmax = log10(max);
    final double loglow = log10(low);
    final double logup = computeLogUpperValue(logmax, logmin, loglow);
    final double up = pow(10, logup);
    setUpperValue(up);

    //getDisplayPositionの為に先に設定
    length = l;
    this.loglow = loglow;
    this.logup = logup;

    final double ll = logmax-logmin;
    if(low == min && max == up){
      setScrollBarValue(-1);
      setScrollVisibleAmount(1);
    }else{
      final double lll = logup-loglow;
      setScrollVisibleAmount(lll/ll);
      setScrollBarValue((loglow-logmin)/(ll-lll));
    }

    final double bottomLabelV = floor(loglow);
    double powv = pow(10, bottomLabelV);
    boolean fill = ((int)bottomLabelV & 1) ==0;
    final double topLabelV = ceil(logup);
    final int size = (int)(topLabelV-bottomLabelV);

    LabelFormat format = getLabelFormat();
    if(format ==null){
      format = new LogarithmicLabelFormat();
      setgetLabelFormat(format);
    }

    final ObservableList<AxisLabel> labels = getLabels();
    final ArrayList<AxisLabel> notUse = new ArrayList<>(labels);
    final ArrayList<AxisLabel> labelList=new ArrayList<>(size);

    for(int i=0;i<size;i++){
      final double logv = bottomLabelV+i;

      if(logv >= loglow && logv <= logup){
        final double pos = getDisplayPosition_log(logv);
        majours.add(pos);
        majoursFill.add(fill);
        fill = !fill;
        boolean find = false;
        for(int t = 0,lsize=notUse.size();t<lsize;t++){
          final AxisLabel a = notUse.get(t);
          if(a.match(logv)){
            labelList.add(a);
            notUse.remove(t);
            find = true;
            break;
          }
        }
        if(!find){
          final AxisLabel a = new AxisLabel();
          a.setID(logv);
          final Node node = format.format(powv);
          a.setNode(node);
          labelList.add(a);
        }

      }
      for(int k=0;k<8;k++){
        final double mlogv = logv + LOG10[k];
        if(mlogv > logup) {
          break;
        }
        if(mlogv < loglow) {
          continue;
        }
        final double pos = getDisplayPosition_log(mlogv);
        minors.add(pos);
      }
      powv*=10;
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

  @Override
  protected double calcLowValue(final double scrollPosition,final double scrollSize){
    double max = getMaxValue();
    double min = getMinValue();
    final double defaultMin = getDefaultMinValue();
    if(min <=0){
      min = defaultMin;
    }

    if(max <=0){
      max = defaultMin;
    }
    max = log10(max);
    min = log10(min);

    final double l = max-min;
    final double bar = l*scrollSize;
    final double low = (l-bar)*scrollPosition + min;
    return pow(10,low);
  }


  @Override
  public double getDisplayPosition(final double v){
    if(v ==0) {
      return Double.NEGATIVE_INFINITY;
    }
    if(v<0) {
      return Double.NaN;
    }
    return getDisplayPosition_log(log10(v));
  }

  private double getDisplayPosition_log(final double logV){
    final double p= (length/(logup-loglow))*(logV-loglow);
    return isHorizontal()? p: length-p;
  }

  @Override
  public double getValueForDisplay(double position){
    if(isVertical()){
      position = length-position;
    }
    return pow(10, position*((logup-loglow)/length)+loglow);
  }


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



  /**
   * minやlowに設定されている値が0以下の時に、代わりに参照される値です。
   * 値が変更されても再描画は行われません
   * @return
   */
  public DoubleProperty dafaultMinValueProperty(){
    if (dafaultMinValueProperty == null) {
      dafaultMinValueProperty = new SimpleDoubleProperty(
          this, "dafaultMinValue", 1){
        @Override
        public void set(final double newValue){
          if(newValue <=0) {
            return;
          }
          super.set(newValue);
        }
      };
    }
    return dafaultMinValueProperty;
  }

  public double getDefaultMinValue(){
    return dafaultMinValueProperty == null ? 1 : dafaultMinValueProperty.get();
  }

  public void setDefaultMinValue(final double value){
    dafaultMinValueProperty().set(value);
  }

  private DoubleProperty dafaultMinValueProperty;



  private static class LogarithmicAxisLabel extends Group{
    private static String[] NUMBERS ={"0","1","2","3","4","5","6","7","8","9","10"};
    private Text up,ten,number;
    private static Text text(final String str){
      final Text t = new Text();
      t.setText(str);
      t.setBoundsType(TextBoundsType.VISUAL);
      return t;
    }
    public LogarithmicAxisLabel(final int up,final double number){
      final ObservableList<Node> c = getChildren();
      getStyleClass().add("logarithmic-axis-label");
      if(up!=0){
        ten = text(NUMBERS[10]);
        c.add(ten);
      }
      if(up!=0 && up!=1){
        final String str = up>0 && up <11?NUMBERS[up]:String.valueOf(up);
        this.up = text(str);
        this.up.setScaleX(0.8);
        this.up.setScaleY(0.8);
        c.add(this.up);
      }
      if(number != 1 || up == 0){
        String str;
        if(number == floor(number)){
          final int num = (int)number;
          str = num >=0 && num <11?NUMBERS[num]:String.valueOf(num);
        }else{
          str = String.valueOf(number);
        }
        if(ten != null) {
          str +="･";
        }
        this.number = text(str);
        c.add(this.number);
      }
      setAutoSizeChildren(false);
      double x=0;
      if(this.number!=null){
        final double w = this.number.prefWidth(-1);
        this.number.setLayoutX(0);
        this.number.setLayoutY(0);
        x +=w+3;
      }
      if(ten!=null){
        final double w = ten.prefWidth(-1);
        ten.setLayoutX(x);
        ten.setLayoutY(0);
        x+=w+3;
        if(this.up!=null){
          final double h = ten.prefHeight(-1)*0.66;
          this.up.setLayoutX(x);
          this.up.setLayoutY(-h);
        }
      }
    }


  }




  public static class LogarithmicLabelFormat implements LabelFormat{

    @Override
    public Node format(final double value){
      final double logv = log10(value);
      final int up = (int)floor(logv);
      final double num = logv-up;
      return new LogarithmicAxisLabel(up, pow(10,num));
    }

  }


}
