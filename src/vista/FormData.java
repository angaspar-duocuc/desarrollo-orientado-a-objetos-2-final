package vista;

import java.util.Map;

public record FormData(int selectedId, Map<String, String> fields, char[] password, int expectedStock) {
    public String field(String name) { return fields.getOrDefault(name, "").strip(); }
}