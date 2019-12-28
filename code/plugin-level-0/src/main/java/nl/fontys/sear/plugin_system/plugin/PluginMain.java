package nl.fontys.sear.plugin_system.plugin;

import nl.fontys.sear.plugin_system.api.Disable;
import nl.fontys.sear.plugin_system.api.Enable;
import nl.fontys.sear.plugin_system.api.PluginInfo;
import nl.fontys.sear.plugin_system.api.Task;

@PluginInfo(name = "Example", version = "Level-0")
public class PluginMain {
    @Enable
    public void enable() {
        System.out.println("Enable!");
    }

    @Disable
    public void disable() {
        System.out.println("Disable!");
    }

    @Task
    public void task() {
        System.out.println("TASK!");
    }
}
