/* Copyright 2019 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentracing.contrib.specialagent;

import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;

public class LightStepTracerInitializer {
  private static final Logger logger = Logger.getLogger(LightStepTracerInitializer.class.getName());

  public static void initializeTracer() {
      String accessToken = System.getProperty("ls.accessToken");
      if (accessToken == null) {
        logger.log(Level.INFO, "No ls.accessToken was provided, not trying to initialize the LightStep Tracer");
        return;
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
        return;
      }

      logger.log(Level.INFO, "Created LightStep Tracer = " + tracer);

      GlobalTracer.register(tracer);
  }
}
