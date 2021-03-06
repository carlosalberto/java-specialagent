/* Copyright 2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, final Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, final either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentracing.contrib.specialagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Identified.Narrowable;
import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.utility.JavaModule;

public class ByteBuddyManager extends Manager {
  private static final Logger logger = Logger.getLogger(ByteBuddyManager.class.getName());

  private Instrumentation instrumentation;

  ByteBuddyManager() {
    super("otaplugins.txt");
  }

  @Override
  void premain(final String agentArgs, final Instrumentation instrumentation) throws Exception {
    this.instrumentation = instrumentation;
    SpecialAgent.initialize();
  }

  /**
   * This method loads any OpenTracing Agent rules (otarules.btm) found as
   * resources within the supplied classloader.
   */
  @Override
  void loadRules(final ClassLoader allPluginsClassLoader, final Map<String,Integer> pluginJarToIndex, final String agentArgs) throws IOException {
    // Prepare the ClassLoader rule
    ClassLoaderAgent.premain(agentArgs, instrumentation);

    // Prepare the Plugin rules
    final Enumeration<URL> enumeration = allPluginsClassLoader.getResources(file);
    while (enumeration.hasMoreElements()) {
      final URL scriptUrl = enumeration.nextElement();
      final String pluginJar = Util.getSourceLocation(scriptUrl, file);
      if (logger.isLoggable(Level.FINEST))
        logger.finest("Dereferencing index for " + pluginJar);

      final int index = pluginJarToIndex.get(pluginJar);

      final BufferedReader reader = new BufferedReader(new InputStreamReader(scriptUrl.openStream()));
      for (String line; (line = reader.readLine()) != null;) {
        line = line.trim();
        if (line.length() == 0 || line.charAt(0) == '#')
          continue;

        if (logger.isLoggable(Level.FINE))
          logger.fine("Installing plugin: " + line);

        try {
          final Class<?> agentClass = Class.forName(line, true, allPluginsClassLoader);
          if (!AgentPlugin.class.isAssignableFrom(agentClass)) {
            logger.severe("Class " + agentClass.getName() + " does not implement " + AgentPlugin.class);
            continue;
          }

          final AgentPlugin agentPlugin = (AgentPlugin)agentClass.getConstructor().newInstance();
          final AgentBuilder builder = agentPlugin.buildAgent(agentArgs);

          final TransformationListener listener = new TransformationListener(index);
//          if (agentPlugin.onEn().getOnEnter() != null)
//            installOn(builder, agentPlugin.onEn().getOnEnter(), agentPlugin, listener, instrumentation);

//          if (agentPlugin.onEn().getOnExit() != null)
//            installOn(builder, agentPlugin.onEn().getOnExit(), agentPlugin, listener, instrumentation);

          builder.with(listener).installOn(instrumentation);
        }
        catch (final InvocationTargetException e) {
          logger.log(Level.SEVERE, "Error initliaizing plugin", e);
        }
        catch (final InstantiationException e) {
          logger.log(Level.SEVERE, "Unable to instantiate: " + line, e);
        }
        catch (final ClassNotFoundException | IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
        catch (final Exception e) {
          logger.log(Level.SEVERE, "Error invoking " + line + "#buildAgent(String) was not found", e);
        }
      }
    }
  }

  private static void installOn(final Narrowable builder, final Class<?> advice, final AgentPlugin agentPlugin, final Listener listener, final Instrumentation instrumentation) {
    builder
      .transform(new Transformer() {
        @Override
        public Builder<?> transform(final Builder<?> builder, final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {
          return null;//builder.visit(Advice.to(advice).on(agentPlugin.onMethod()));
        }
      })
      .with(listener)
      .installOn(instrumentation);
  }

  @Override
  boolean disableTriggers() {
    return false;
  }

  @Override
  boolean enableTriggers() {
    return false;
  }

  class TransformationListener implements AgentBuilder.Listener {
    private final int index;

    TransformationListener(final int index) {
      this.index = index;
    }

    @Override
    public void onDiscovery(final String typeName, final ClassLoader classLoader, final JavaModule module, final boolean loaded) {
    }

    @Override
    public void onTransformation(final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module, final boolean loaded, final DynamicType dynamicType) {
      if (!SpecialAgent.linkPlugin(index, classLoader))
        throw new IllegalStateException("Disallowing transformation due to incompatibility");
    }

    @Override
    public void onIgnored(final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module, final boolean loaded) {
    }

    @Override
    public void onError(final String typeName, final ClassLoader classLoader, final JavaModule module, final boolean loaded, final Throwable throwable) {
      logger.log(Level.SEVERE, "Error transforming " + typeName, throwable);
    }

    @Override
    public void onComplete(final String typeName, final ClassLoader classLoader, final JavaModule module, final boolean loaded) {
    }
  }
}