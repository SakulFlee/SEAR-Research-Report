package nl.fontys.sear.plugin_system.system;

import nl.fontys.sear.plugin_system.api.Disable;
import nl.fontys.sear.plugin_system.api.Enable;
import nl.fontys.sear.plugin_system.api.PluginInfo;
import nl.fontys.sear.plugin_system.api.Task;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.jar.JarFile;

/**
 * Plugin Loader.
 * Loads plugins and adds their classes dynamically to the JRE classpath.
 * Requires instrumentation/Agent API; Check premain and agentmain for more information.
 */
public class PluginLoader {

    /**
     * Plugin folder plugins are load from
     */
    @NotNull
    private final File pluginFolder = new File("./plugins");
    /**
     * List of loaded/found plugins
     */
    @NotNull
    private static LinkedList<Plugin> plugins = new LinkedList<>();
    /**
     * Instrumentation used to access and modify the JRE
     */
    @NotNull
    private static Instrumentation instrumentation;

    /**
     * Default empty constructor.
     */
    public PluginLoader() { }

    /**
     * Loads all plugins from the set {@code PluginLoader::pluginFolder}.
     * @throws IOException in case something goes wrong. (Mainly: Permission errors when loading/reading a plugin)
     */
    public void loadPlugins() throws IOException {
        if (!pluginFolder.exists()) throw new FileNotFoundException(pluginFolder.getAbsolutePath());
        if (pluginFolder.isFile())
            throw new IOException("Expected directory, found file: " + pluginFolder.getAbsolutePath());

        System.out.println("Loading plugins ...");
        File[] pluginFiles = pluginFolder.listFiles((File, String) -> String.endsWith(".jar"));
        if (pluginFiles == null) {
            System.out.println("No plugins in plugin folder ('" + pluginFolder.getAbsolutePath() + "') found!");
            return;
        }
        Arrays.stream(pluginFiles).forEach(it -> {
            System.out.println("Found plugin: " + it.getAbsolutePath());
            loadPlugin(it);
        });
    }

    /**
     * Loads a plugin from a file.
     * Internally calls {@code PluginLoader::AddJARToClassPath}.
     * @param file to add.
     */
    public void loadPlugin(@NotNull File file) {
        try {
            PluginLoader.AddJARToClassPath(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a JAR to the JRE classpath from a file.
     * Makes use of the instrumentation/agent Java API to dynamically extend the classpath.
     *
     * @param file to add. Must be a compiled valid JAR, otherwise will fail.
     * @throws IOException in case something goes wrong. (Mainly: Permission errors when loading/reading a plugin)
     */
    public synchronized static void AddJARToClassPath(@NotNull File file) throws IOException {
        // Validate file constraints
        if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());
        if (!file.canRead()) throw new IOException("Cannot read file: " + file.getAbsolutePath());
        if (file.isDirectory()) throw new IOException("Expected file, found Directory: " + file.getAbsolutePath());

        // Add JAR to classpath
        instrumentation.appendToSystemClassLoaderSearch(new JarFile(file));
        // Find and register plugins from classpath
        findAndRegisterPluginsFromClasspath();
    }

    /**
     * Finds and registers plugins from classpath.
     * This is done by querying through all currently registered classes and finding valid plugin classes.
     * A valid plugin class has a {@code PluginInfo} annotation with a valide name and version set.
     * Setting a valid name and version meaning having at least one character.
     * Furthermore, enable, disable and task methods can be annotated with {@code Enable}, {@code Disable} and {@code Task}.
     * Only one for each type per plugin will be set, which is chosen can be random / depends on the classpath.
     * Each of these methods must have ZERO parameters and MUST return void.
     */
    private synchronized static void findAndRegisterPluginsFromClasspath() {
        System.out.println("Finding plugins:");
        for(Class<?> clazz : instrumentation.getAllLoadedClasses()) {
            System.out.println(clazz);
            Annotation[] clazzAnnotations = clazz.getAnnotations();
            if(clazzAnnotations == null || clazzAnnotations.length <= 0) continue;

            PluginInfo pluginInfo = null;

            for(Annotation clazzAnnotation : clazzAnnotations) {
                if(clazzAnnotation instanceof PluginInfo) {
                    System.out.println("Found annotated plugin class!");
                    System.out.println("\tClass: " + clazz);
                    System.out.println("\tAnnotation: " + clazzAnnotation);

                    PluginInfo pluginAnnotation = (PluginInfo) clazzAnnotation;
                    // Validate that plugin info has a valid name
                    String name = pluginAnnotation.name();
                    if(name.length() == 0) {
                        System.err.println("Found a plugin info annotation without a name! Skipping ...");
                        continue;
                    }
                    // Validate that plugin info has a valid version
                    String version = pluginAnnotation.version();
                    if(version.length() == 0) {
                        System.err.println("Found a plugin info annotation without a version! Skipping ...");
                        continue;
                    }

                    pluginInfo = pluginAnnotation;
                }
            }

            // Classes that are not annotated with @Plugin will be skipped
            if(pluginInfo == null) continue;

            Method enableMethod = null;
            Method disableMethod = null;
            Method taskMethod = null;

            for(Method method : clazz.getMethods()) {
                Annotation[] methodAnnotations = method.getAnnotations();
                // Skip all methods without annotations
                if(methodAnnotations == null || methodAnnotations.length <= 0) continue;

                for(Annotation methodAnnotation : methodAnnotations) {
                    if(methodAnnotation instanceof Enable) {
                        System.out.println("\tFound enable!");
                        System.out.println("\t\tMethod: " + method);
                        System.out.println("\t\tAnnotation: " + methodAnnotation);

                        // Check if annotated method has zero parameters
                        if(method.getParameterCount() != 0) {
                            System.err.println("\tEnable method must not have any parameters!");
                            continue;
                        }

                        enableMethod = method;
                    } else if(methodAnnotation instanceof  Disable) {
                        System.out.println("\tFound disable!");
                        System.out.println("\t\tMethod: " + method);
                        System.out.println("\t\tAnnotation: " + methodAnnotation);

                        // Check if annotated method has zero parameters
                        if(method.getParameterCount() != 0) {
                            System.err.println("\tDisable method must not have any parameters!");
                            continue;
                        }

                        disableMethod = method;
                    } else if(methodAnnotation instanceof  Task) {
                        System.out.println("\tFound task!");
                        System.out.println("\t\tMethod: " + method);
                        System.out.println("\t\tAnnotation: " + methodAnnotation);

                        // Check if annotated method has zero parameters
                        if(method.getParameterCount() != 0) {
                            System.err.println("\tTask method must not have any parameters!");
                            continue;
                        }

                        taskMethod = method;
                    }
                }
            }

            // Create plugin data class instance
            Plugin plugin = new Plugin(pluginInfo, clazz, enableMethod, disableMethod, taskMethod);
            // Register plugin data class
            registerPlugin(plugin);
        }
    }

    // --- Methods for all plugins

    /**
     * Registers a plugin to the plugin registry.
     * @param plugin the plugin to register.
     */
    private synchronized static void registerPlugin(@NotNull Plugin plugin) {
        plugins.add(plugin);
    }

    /**
     * Enables all plugins.
     */
    public synchronized void enableAll() {
        plugins.forEach(Plugin::enable);
    }

    /**
     * Disables all plugins.
     */
    public synchronized void disableAll() {
        plugins.forEach(Plugin::disable);
    }

    /**
     * Invokes the task method on all plugins.
     */
    public synchronized void taskAll() {
        plugins.forEach(Plugin::task);
    }

    // --- Main, Pre-Main and Agent-Main

    /**
     * Main method; is called by JRE.
     * Entry point of application.
     *
     * Initializes plugin loader, requests to loads all plugins and invokes enable -> task -> disable on all.
     * @param args application arguments.
     */
    public static void main(@NotNull String[] args) {
        System.out.println("CWD: " + new File(".").getAbsolutePath());
        if(PluginLoader.instrumentation == null) {
            System.err.println("No instrumentation reference set!");
            System.err.println("You may want to add '-javaagent:EXACT_PATH_TO_THIS_JAR.jar' to your VM arguments!");
            System.err.println("[E.g.: '-javaagent:out\\artifacts\\plugin_system_level_0_jar\\plugin-system-level-0.jar']");
            System.exit(-1);
        }

        PluginLoader loader = new PluginLoader();
        try {
            loader.loadPlugins();
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("Something did go wrong");
        }

        loader.enableAll();
        loader.taskAll();
        loader.disableAll();
    }

    /**
     * CALLED BY THE JRE!
     * Invoked when the JRE loads this class as an agent using {@code -javaagent:JAR-PATH-TO-THIS-CLASS}.
     * <p>
     * To work, within the {@code MANIFEST.MF} needs to be a line stating {@code Agent-Class: Fully-Qualified-Path-To-This-Class}.
     *
     * @param agentArgs       agent arguments.
     * @param instrumentation provided by the JRE
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        agentmain(agentArgs, instrumentation);
    }

    /**
     * CALLED BY THE JRE!
     * Invoked when agent attaches to the current process.
     * <p>
     * To work, within the {@code MANIFEST.MF} needs to be a line stating {@code Agent-Class: Fully-Qualified-Path-To-This-Class}.
     *
     * @param agentArgs       agent arguments.
     * @param instrumentation provided by the JRE
     */
    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        PluginLoader.instrumentation = instrumentation;
    }
}
