package nodamushi.jfx.chart.linechart;

import static java.lang.Math.*;

import java.util.List;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

@GraphContainer
public class VGraphs extends Region{


  public VGraphs(){
    getStyleClass().setAll("chart");
    graphs.addListener(new ListChangeListener<Graph>(){
      @Override
      public void onChanged(final Change<? extends Graph> c){
        while(c.next()){
          final List<? extends Graph> removed = c.getRemoved();
          final ObservableList<Node> ch = getChildren();
          if(!removed.isEmpty()) {
            ch.removeAll(removed);
            for(final Graph g:removed){
              final Label l = g.getTitleLabel();
              l.visibleProperty().unbind();
              final int i = ch.indexOf(l);
              if(i!=-1){
                ch.remove(i);
              }
            }
          }
          if(c.getAddedSize()!=0){
            final List<? extends Graph> sub = c.getAddedSubList();
            ch.addAll(sub);
            for(final Graph g:sub){
              final Label l = g.getTitleLabel();
              if(l.visibleProperty().isBound()) {
                l.visibleProperty().unbind();
              }
              l.visibleProperty().bind(new BooleanBinding(){
                {
                  bind(l.textProperty(),titleVisibleProperty());
                }
                @Override
                protected boolean computeValue(){
                  final String str = l.getText();
                  return isTitleVisible() && l.getText()!=null && !str.isEmpty();
                }
              });
              ch.add(l);
            }
          }
          requestLayout();
        }
      }
    });
  }


  @Override
  protected void layoutChildren(){
    final List<Graph> list = graphs;
    final Insets insets = getInsets();
    double x0=0,x1=getWidth(),y0=0,y1=getHeight();
    if(insets!=null){
      x0 = ceil(insets.getLeft());
      x1 -=insets.getRight();
      y0 = ceil(insets.getTop());
      y1 -=insets.getBottom();
    }
    double width = ceil(x1-x0);
    final double height = y1-y0;

    final int size = list.size();
    final double space = getSpace();
    double hh = height - space*(size-1);
    final boolean titlev = isTitleVisible();
    Side titles = getTitleSide();
    if(titles==null) {
      titles=Side.TOP;
    }
    if(titlev && titles.isHorizontal()){
      for(final Graph g:list){
        final Label l = g.getTitleLabel();
        if( l.isVisible()){
          hh-=l.prefHeight(-1);
        }
      }
    }

    if(titlev && titles.isVertical()){
      double maxv = 0;
      for(final Graph g:list){
        final Label l = g.getTitleLabel();
        if(l.isVisible()){
          maxv =max(maxv, l.prefWidth(-1));
        }
      }

      if(titles == Side.LEFT){
        x0 = ceil(x0+maxv);
      }else{
        x1 -= maxv;
      }
      width = ceil(x1-x0);
    }

    hh=Math.floor(hh/size);

    double y=y0;

    double maxx0=0;
    double minx1=Double.POSITIVE_INFINITY;
    for(final Graph g:list){
      final Label l = g.getTitleLabel();
      if(l.isVisible()){
        final double w = l.prefWidth(-1);
        final double h = l.prefHeight(-1);
        switch(titles){
          case TOP:
            l.resizeRelocate(floor(x0+(width-w)*0.5), y, w, h);
            y+=h;
            break;
          case RIGHT:
            l.resizeRelocate(x1, floor(y1+(hh-h)*0.5),w, h);
            break;
          case LEFT:
            l.resizeRelocate(x0, floor(y1+(hh-h)*0.5),w, h);
            break;
          default:
            break;
        }
      }
      g.setPlotAreaPrefferedBounds(null);
      g.preLayout(x0, floor(y), width, hh);
      maxx0 = max(maxx0, g.getPlotAreaBounds().getMinX());
      minx1 = min(minx1,g.getPlotAreaBounds().getMaxX());
      y += hh;
      if(l.isVisible()&& titles==Side.BOTTOM){
        final double w = l.prefWidth(-1);
        final double h = l.prefHeight(-1);
        l.resizeRelocate(floor(x0+(width-w)*0.5), y, w, h);
        y+=h;
      }
      y+=space;
    }

    for(final Graph g:list){
      final Rectangle2D r = g.getPlotAreaBounds(),
          r2 = new Rectangle2D(maxx0, r.getMinY(), minx1-maxx0, r.getHeight());
      g.setPlotAreaPrefferedBounds(r2);
      g.layout();
    }

  }


  private ObservableList<Graph> graphs =
      FXCollections.observableArrayList();
  public ObservableList<Graph> getGraphs(){
    return graphs;
  }



  /**
   * タイトルの可視性
   * @return
   */
  public BooleanProperty titleVisibleProperty(){
    if (titleVisibleProperty == null) {
      titleVisibleProperty = new SimpleBooleanProperty(this, "titleVisible", true);
    }
    return titleVisibleProperty;
  }

  public boolean isTitleVisible(){
    return titleVisibleProperty == null ? true : titleVisibleProperty.get();
  }

  public void setTitleVisible(final boolean value){
    titleVisibleProperty().set(value);
  }

  private BooleanProperty titleVisibleProperty;

  /**
   * タイトルの表示位置
   * @return
   */
  public ObjectProperty<Side> titleSideProperty(){
    if (titleSideProperty == null) {
      titleSideProperty = new SimpleObjectProperty<>(this, "titleSide", Side.TOP);
    }
    return titleSideProperty;
  }

  public Side getTitleSide(){
    return titleSideProperty == null ? Side.TOP : titleSideProperty.get();
  }

  public void setTitleSide(final Side value){
    titleSideProperty().set(value);
  }

  private ObjectProperty<Side> titleSideProperty;


  /**
   * グラフの間の空間の長さ
   * @return
   */
  public DoubleProperty spaceProperty(){
    if (spaceProperty == null) {
      spaceProperty = new SimpleDoubleProperty(this, "space", 20);
    }
    return spaceProperty;
  }

  public double getSpace(){
    return spaceProperty == null ? 20 : spaceProperty.get();
  }

  public void setSpace(final double value){
    spaceProperty().set(value);
  }

  private DoubleProperty spaceProperty;

}
