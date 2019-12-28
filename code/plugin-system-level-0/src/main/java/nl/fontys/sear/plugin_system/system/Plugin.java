package nl.fontys.sear.plugin_system.system;

import nl.fontys.sear.plugin_system.api.PluginInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Plugin {

    @NotNull
    private final PluginInfo pluginInfo;
    @NotNull
    private final Class<?> pluginClass;
    @Nullable
    private final Method enableMethod;
    @Nullable
    private final Method disableMethod;
    @Nullable
    private final Method taskMethod;
    @Nullable
    private Object instance;

    public Plugin(@NotNull PluginInfo pluginInfo, @NotNull Class<?> pluginClass, @Nullable Method enableMethod, @Nullable Method disableMethod, @Nullable Method taskMethod) {
        this.pluginInfo = pluginInfo;
        this.pluginClass = pluginClass;
        this.enableMethod = enableMethod;
        this.disableMethod = disableMethod;
        this.taskMethod = taskMethod;
    }

    @NotNull
    public String getPluginName() {
        return pluginInfo.name();
    }

    @NotNull
    public String getPluginVersion() {
        return pluginInfo.version();
    }

    @NotNull
    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    @NotNull
    public Class<?> getPluginClass() {
        return pluginClass;
    }

    @Nullable
    public Method getEnableMethod() {
        return enableMethod;
    }

    @Nullable
    public Method getDisableMethod() {
        return disableMethod;
    }

    @Nullable
    public Method getTaskMethod() {
        return taskMethod;
    }

    /**
     * Tries to find a valid constructor.
     * Used to instantiate the plugin class.
     *
     * @return found constructor
     */
    @NotNull
    private Constructor<?> findFittingConstructor() {
        Constructor<?>[] constructors = this.pluginClass.getConstructors();
        if (constructors.length == 0)
            throw new RuntimeException("No constructors defined in plugin '" + toString() + "'!");

        List<Constructor<?>> validConstructorList = Arrays.stream(constructors)
                .filter(it -> it.getParameterCount() == 0)
                .collect(Collectors.toList());
        if (validConstructorList.isEmpty())
            throw new RuntimeException("No valid constructor found in plugin '" + toString() + "'!");

        Constructor<?> constructor = validConstructorList.get(0);
        if (constructor == null)
            throw new RuntimeException("Chosen constructor is null for plugin '" + toString() + "'!");

        return constructor;
    }

    private void verifyInstance() {
        if (this.instance != null) return;

        Constructor<?> constructor = findFittingConstructor();
        try {
            this.instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.err.println("Failed to instantiate a new instance of plugin class!");
            throw new RuntimeException("Failed to instantiate a new instance of plugin class!");
        }
    }

    public void enable() {
        if (this.enableMethod == null) return;
        verifyInstance();

        try {
            this.enableMethod.invoke(this.instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.err.println("Failed to enable plugin '" + toString() + "'!");
        }
    }

    public void disable() {
        if (this.disableMethod == null) return;
        verifyInstance();

        try {
            this.disableMethod.invoke(this.instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.err.println("Failed to disable plugin '" + toString() + "'!");
        }
    }

    public void task() {
        if (this.taskMethod == null) return;
        verifyInstance();

        try {
            this.taskMethod.invoke(this.instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.err.println("Failed to execute task on plugin '" + toString() + "'!");
        }
    }

    @Override
    public String toString() {
        return "Plugin{" +
                "pluginInfo=" + pluginInfo +
                ", pluginClass=" + pluginClass +
                ", instance=" + instance +
                ", enableMethod=" + enableMethod +
                ", disableMethod=" + disableMethod +
                ", taskMethod=" + taskMethod +
                '}';
    }
}
