package de.thegame4craft.core;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PermissionManager {
    public static PermissionManager INSTANCE;

    private final HashMap<String, List<String>> permissions = new HashMap<>();
    private final Path permissionFile = Path.of("permissions.json");

    public PermissionManager() throws IOException {
        JSONObject obj = new JSONObject(Files.readString(permissionFile));
        for (String key : obj.keySet()) {
            ArrayList<String> p = new ArrayList<>();
            for (Object o : obj.getJSONArray(key)) {
                p.add((String) o);
            }
            permissions.put(key, p);
        }
    }

    public static PermissionManager init() {
        if(INSTANCE != null) {
            return INSTANCE;
        }
        try {
            INSTANCE = new PermissionManager();
            return INSTANCE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void addPermission(UUID uuid, String permission) {
        if (permissions.containsKey(uuid.toString())) {
            permissions.get(uuid.toString()).add(permission);
        } else {
            permissions.put(uuid.toString(), List.of(permission));
        }
    }

    public void removePermission(UUID uuid, String permission) {
        if (permissions.containsKey(uuid.toString())) {
            permissions.get(uuid.toString()).remove(permission);
        }
    }

    public List<String> getPermissions(UUID uuid) {
        return permissions.get(uuid.toString());
    }

    public boolean hasPermission(UUID uuid, String permission) {
        return permissions.get(uuid.toString()).contains(permission);
    }


    public void save() throws IOException {
        JSONObject obj = new JSONObject();
        for (Map.Entry<String, List<String>> entry : permissions.entrySet()) {
            obj.put(entry.getKey(), entry.getValue());
        }
        Files.writeString(permissionFile, obj.toString());
    }

    public boolean isOP(UUID uuid) {
        return hasPermission(uuid, "server.op");
    }

    public void addOp(UUID uuid) {
        addPermission(uuid, "server.op");
    }

    public void removeOp(UUID uuid) {
        removePermission(uuid, "server.op");
    }
}
