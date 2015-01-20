package nodamushi.jfx.chart.linechart;

import javafx.geometry.Orientation;
import javafx.scene.Node;

public interface GraphShape{
  /**
   * 表示するノード
   * @return
   */
  public Node getNode();
  /**
   * 値を変換するのに必要なaxisの方向。
   * HORIZONTALならばx軸を、そうでなければy軸を渡す。
   * @return
   */
  public Orientation getAxisOrientation();

  /**
   * 必要な値をaxisから座標に変換し、設定する。
   * @param axis
   * @param w 表示領域の幅
   * @param h 表示領域の高さ
   */
  public void setNodeProperty(Axis axis,double w,double h);

}
