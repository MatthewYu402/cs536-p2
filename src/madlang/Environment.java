package madlang;

import java.util.HashMap;
import java.util.Map;

class Environment {

    Environment parent;
    private final Map<String, Object> values = new HashMap<>();

    // Constructue
    public Environment(Environment parent) {
        this.parent = parent;
    }

    // Declare a variable in the current scope
    void declare(String name, Object value) {
        // Create variable if it doesn't exist, or update it
        values.put(name, value);
    }

    // If assigning a variable outside the scope, walk the chain of environments
    void assign(String name, Object value) {
        if (values.containsKey(name)) {
            values.put(name, value);
            return;
        }

        if (parent == null) {
            throw new RuntimeException("Error: unbound reference");
        }

        parent.assign(name, value);
    }

    // Get name in current environment or search for parent declaration
    Object get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }

        if (parent == null) {
            throw new RuntimeException("Error: unbound reference");
        }

        return parent.get(name);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("{");
      boolean first = true;
      for (Map.Entry<String, Object> entry : values.entrySet()) {
          if (!first) {
              sb.append(", ");
          }
          String name = entry.getKey();
          Object value = entry.getValue();
          sb.append(name).append(" = ").append(value);
          first = false;
      }
      sb.append("}");
      return sb.toString();
    }
}
