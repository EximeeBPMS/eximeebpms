package org.eximeebpms.bpm.engine.impl.scripting.engine;

import java.util.List;

import org.codehaus.groovy.control.customizers.SecureASTCustomizer;

/**
 * Creates the SecureASTCustomizer used by the secured Groovy ScriptEngine.
 * <p>
 * This is intentionally Groovy-specific and separated from DefaultScriptEngineResolver
 * so the resolver only decides which engine to use, while this class defines the Groovy sandbox rules.
 * </p>
 */
public class GroovySecureAstCustomizerFactory {

  public SecureASTCustomizer createSecureAstCustomizer() {
    SecureASTCustomizer customizer = new SecureASTCustomizer();

    customizer.setClosuresAllowed(true);
    customizer.setMethodDefinitionAllowed(true);
    customizer.setPackageAllowed(false);

    /*
     * This is not the final sandbox by itself. It is the AST-level restriction layer.
     * It prevents common direct access to dangerous Java/Groovy escape hatches before execution.
     */
    customizer.setImportsBlacklist(List.of(
        "java.io.File",
        "java.lang.Runtime",
        "java.lang.System",
        "java.lang.ProcessBuilder"
    ));

    customizer.setStaticImportsBlacklist(List.of(
        "java.lang.System.exit",
        "java.lang.Runtime.getRuntime"
    ));

    customizer.setStarImportsBlacklist(List.of(
        "java.io",
        "java.nio",
        "java.nio.file",
        "java.net",
        "java.lang.reflect"
    ));

    customizer.setReceiversClassesBlackList(List.of(
        System.class,
        Runtime.class,
        ProcessBuilder.class,
        java.io.File.class,
        java.nio.file.Files.class,
        java.lang.reflect.Method.class,
        java.lang.reflect.Field.class,
        Class.class,
        ClassLoader.class
    ));

    return customizer;
  }
}
