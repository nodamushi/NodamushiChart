package nodamushi.jfx.chart.linechart;

import javafx.scene.shape.Circle;

public class GraphCircle extends GraphPointShape{
  private Circle circle;

  @Override
  public Circle getNode(){
    if(circle == null){
      circle = new Circle(5);
    }
    return circle;
  }

  public void setRadius(final double r){
    getNode().setRadius(r);
  }

  public double getRadius(){
    return getNode().getRadius();
  }

}
