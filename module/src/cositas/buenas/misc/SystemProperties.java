package cositas.buenas.misc;

import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class SystemProperties {
    public static void main(String[] args) {
        Properties properties = System.getProperties();
        Set<String> propertyNames = new TreeSet<>(properties.stringPropertyNames());
        for (String propertyName : propertyNames) {
            System.out.println(propertyName + " -> " + System.getProperty(propertyName));
        }
    }
}
