package org.eximeebpms.bpm.engine.impl.scripting.engine;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;

/**
 * Creates Groovy ScriptEngines backed by a GroovyClassLoader configured with SecureASTCustomizer.
 * <p>
 * Important:
 * - The standard ScriptEngineManager-created Groovy engine does not know about our CompilerConfiguration.
 * - Therefore, for secured Groovy execution, we create GroovyScriptEngineImpl directly with a configured classloader.
 * </p>
 */
public class SecureGroovyScriptEngineFactory {

  protected final GroovySecureAstCustomizerFactory secureAstCustomizerFactory;

  public SecureGroovyScriptEngineFactory(GroovySecureAstCustomizerFactory secureAstCustomizerFactory) {
    this.secureAstCustomizerFactory = secureAstCustomizerFactory;
  }

  public ScriptEngine createScriptEngine() {
    final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
    compilerConfiguration.addCompilationCustomizers(secureAstCustomizerFactory.createSecureAstCustomizer());

    final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(getClass().getClassLoader(), compilerConfiguration);
    final GroovyScriptEngineImpl scriptEngine = new GroovyScriptEngineImpl(groovyClassLoader);

    /*
     * Keep the old Groovy setting:
     * Groovy compiled scripts should only hold weak references to Java methods.
     */
    scriptEngine.getContext().setAttribute(
        "#jsr223.groovy.engine.keep.globals",
        "weak",
        ScriptContext.ENGINE_SCOPE
    );

    return scriptEngine;
  }

}
