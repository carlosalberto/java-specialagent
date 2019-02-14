package io.opentracing.contrib.specialagent.lightstep;

import java.net.MalformedURLException;
import java.util.logging.Logger;
import java.util.logging.Level;

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerFactory;

import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;

// FIXME: normalize the log level
public class LightStepTracerFactory implements TracerFactory {
  private static final Logger logger = Logger.getLogger(LightStepTracerFactory.class.getName());

  @Override
  public Tracer getTracer()
  {
    String accessToken = System.getProperty("ls.accessToken");
    if (accessToken == null) {
      logger.log(Level.INFO, "No ls.accessToken was provided, not trying to initialize the LightStep Tracer");
      return null;
    }

    String componentName = System.getProperty("ls.componentName");
    String collectorHost = System.getProperty("ls.collectorHost");
    String collectorPort = System.getProperty("ls.collectorPort");
    String collectorProtocol = System.getProperty("ls.collectorProtocol");

    logger.log(Level.INFO, "Trying to create the LightStep Tracer with the following parameters:");
    logger.log(Level.INFO, " componentName = " + componentName);
    logger.log(Level.INFO, " collectorHost = " + collectorHost);
    logger.log(Level.INFO, " collectorPort = " + collectorPort);
    logger.log(Level.INFO, " collectorProtocol = " + collectorProtocol);

    Options.OptionsBuilder opts = new Options.OptionsBuilder().withAccessToken(accessToken);
    if (componentName != null)
      opts = opts.withComponentName(componentName);

    if (collectorHost != null)
      opts = opts.withCollectorHost(collectorHost);

    if (collectorPort != null)
      opts = opts.withCollectorPort(Integer.valueOf(collectorPort));

    if (collectorProtocol != null)
      opts = opts.withCollectorProtocol(collectorProtocol);

    JRETracer tracer = null;
    try {
      tracer = new JRETracer(opts.build());
    } catch (MalformedURLException e) {
      logger.log(Level.WARNING, "Got an exception initializing the LightStep Tracer = " + e);
      return null;
    }

    logger.log(Level.INFO, "Created LightStep Tracer = " + tracer);
    return tracer;
  }
}
