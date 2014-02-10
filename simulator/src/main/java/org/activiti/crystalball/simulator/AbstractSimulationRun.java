package org.activiti.crystalball.simulator;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.util.ClockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * This class...
 */
public abstract class AbstractSimulationRun implements SimulationRun, SimulationDebugger {

  private static Logger log = LoggerFactory.getLogger(AbstractSimulationRun.class);

  /**
   * Map for eventType -> event handlers to execute events on simulation engine
   */
  protected Map<String, SimulationEventHandler> eventHandlerMap;
  protected ProcessEngine processEngine;

  public AbstractSimulationRun(Map<String, SimulationEventHandler> eventHandlers) {
    this.eventHandlerMap = eventHandlers;
  }

  @Override
  public void execute() throws Exception {
    init();

    runContinue();

    close();
  }

  protected SimulationEvent removeSimulationEvent() {
    SimulationEvent event = SimulationRunContext.getEventCalendar().removeFirstEvent();
    if (event != null && event.hasSimulationTime())
      ClockUtil.setCurrentTime(new Date(event.getSimulationTime()));
    return event;
  }

  @Override
  public void init() {
    initSimulationRunContext();
    initHandlers();
  }

  @Override
  public void step() {
    SimulationEvent event = removeSimulationEvent();
    if (!simulationEnd( event)) {
      log.debug("executing simulation event {}", event );
      executeEvent(event);
      log.debug("simulation event {event} execution done", event);
    } else {
      log.info("Simulation run has ended.");
    }
  }

  @Override
  public void runContinue() {
    SimulationEvent event = removeSimulationEvent();

    while (!simulationEnd(event)) {
      executeEvent(event);
      event = removeSimulationEvent();
    }
  }

  @Override
  public void runTo(long simulationTime) {
    SimulationEvent breakEvent = new SimulationEvent.Builder(SimulationEvent.TYPE_BREAK_SIMULATION).
                                     simulationTime(simulationTime).
                                     priority(SimulationEvent.PRIORITY_SYSTEM).build();
    EventCalendar calendar = SimulationRunContext.getEventCalendar();
    calendar.addEvent(breakEvent);
    runContinue();
  }

  @Override
  public void runTo(String simulationEventType) {
    EventCalendar eventCalendar = SimulationRunContext.getEventCalendar();
    SimulationEvent event = eventCalendar.peekFirstEvent();

    while (!simulationEventType.equals(event.getType()) && !simulationEnd(event)) {
      step();
      event = eventCalendar.peekFirstEvent();
    }
  }

  /**
   * close simulation run
   */
  @Override
  public abstract void close();

  protected abstract void initSimulationRunContext();

  protected void initHandlers() {
		for( SimulationEventHandler handler : eventHandlerMap.values()) {
			handler.init();
		}
	}

  protected abstract boolean simulationEnd(SimulationEvent event);

  protected void executeEvent(SimulationEvent event) {
    // set simulation time to the next event for process engine too
    log.info( "Simulation time:" + ClockUtil.getCurrentTime());

    SimulationEventHandler handler = eventHandlerMap.get( event.getType() );
    if ( handler != null) {
      log.debug("Handling event of type[{}]", event.getType());
      handler.handle( event);
    } else {
        log.warn("Event type[{}] does not have any handler assigned.", event.getType());
    }
  }

}