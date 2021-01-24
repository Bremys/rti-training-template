package iface.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

public class Utils {

    private static final Logger logger = LogManager.getLogger(Utils.class);
    public static final String GET_TYPE_METHOD = "get_type_name";
    public static final String PACKAGE_PATH = "iface.objects.";

    public static String getTypeName(String topicName) {
        try {
            Class<?> typeSupport = Class.forName(String.format("%s%sTypeSupport", PACKAGE_PATH, topicName));
            Method method = typeSupport.getMethod(GET_TYPE_METHOD);
            return (String) method.invoke(null);
        } catch (Exception e) {
            logger.error("Could not get type name", e);
            throw new RuntimeException("Could not get type name");
        }
    }

    public static <T> T newInstance(String object) {
        try {
            Class<?> objectClass = getObjectClass(object);
            return (T) objectClass.getConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Could not get type name", e);
            throw new RuntimeException("Could not get type name");
        }
    }

    public static Class getObjectClass(String topicName) {
        try {
            return Class.forName(String.format("%s%s", PACKAGE_PATH, topicName));
        } catch (Exception e) {
            logger.error("Could not get type name", e);
            throw new RuntimeException("Could not get type name");
        }
    }
}
