package matter_manipulator.core.util;

import java.util.ArrayList;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class Flag {

    private static final Object LOCK = new Object();

    private static final Object2ObjectOpenHashMap<String, Flag> FLAGS_BY_NAME = new Object2ObjectOpenHashMap<>();

    private static int idCounter;
    private static final ArrayList<Flag> FLAGS_BY_ID = new ArrayList<>();

    public final String name;
    public final int id;

    private Flag(String name) {
        this.name = name;
        synchronized (LOCK) {
            this.id = idCounter++;
            FLAGS_BY_ID.add(this);
        }
    }

    public static Flag get(String name) {
        synchronized (LOCK) {
            return FLAGS_BY_NAME.computeIfAbsent(name, Flag::new);
        }
    }

    public static Flag get(int id) {
        synchronized (LOCK) {
            return FLAGS_BY_ID.get(id);
        }
    }

    @Override
    public String toString() {
        return "Flag{" + "name='" + name + '\'' + '}';
    }
}
