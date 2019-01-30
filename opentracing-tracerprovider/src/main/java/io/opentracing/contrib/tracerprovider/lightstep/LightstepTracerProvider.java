package io.opentracing.contrib.tracerprovider.lightstep;

import java.net.MalformedURLException;
import java.util.Map;

import io.opentracing.Tracer;

import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options.OptionsBuilder;

import io.opentracing.contrib.tracerprovider.TracerProvider;

public class LightstepTracerProvider extends TracerProvider {

  @Override
  public String getConfigurationPrefix() {
    return "ls";
  }

  @Override
  protected Tracer createTracerImpl(final Map<String, String> args)
  {
    if (args.containsKey("accessToken")) {
      return null;
    }

    String accessToken = args.get("accessToken");
    OptionsBuilder optsBuilder = new OptionsBuilder().withAccessToken(accessToken);

    if (args.containsKey("componentName"))
      optsBuilder = optsBuilder.withComponentName(args.get("componentName"));

    JRETracer tracer = null;

    try {
      tracer = new JRETracer(optsBuilder.build());
    } catch (MalformedURLException e) {
    }

    return tracer;
  }
}
