package io.opentracing.contrib.tracerprovider;

import java.util.Map;
import java.util.ServiceLoader;

import io.opentracing.Tracer;

public abstract class TracerProvider {
  public abstract String getConfigurationPrefix();
  protected abstract Tracer createTracerImpl(final Map<String, String> args);

  public Tracer createTracer(final Map<String, String> args) {
    Tracer tracer = null;
    try {
      tracer = createTracerImpl(args);
    } catch (NoClassDefFoundError e) {
    }

    return tracer;
  }

  public static Iterable<TracerProvider> loadCandidates() {
    return ServiceLoader.load(TracerProvider.class);
  }
}
