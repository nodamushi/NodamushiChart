package nodamushi.jfx.chart.linechart;

import javafx.event.Event;
import javafx.event.EventHandler;

/**
 * GraphPlotAreaやGraphにEventHandlerをaddしたりするのは、install,uninstallメソッドを利用してください。
 * @author nodamushi
 *
 * @param <T>
 */
public abstract class GraphEventHandler<T extends Event> implements EventHandler<T>{

  protected final static GraphPlotArea getPlotArea(final LineChart l){
    return l.graph;
  }

  public final void install(final LineChart g){
    install(g.graph);
  }

  public abstract void install(GraphPlotArea g);

  public final void uninstall(final LineChart g){
    uninstall(g.graph);
  }

  public abstract void uninstall(GraphPlotArea g);
}
