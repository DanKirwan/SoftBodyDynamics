package util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionUtil {

    private static final Field MODIFIERS_FIELD;
    static {
        try {
            MODIFIERS_FIELD = Field.class.getDeclaredField("modifiers");
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        MODIFIERS_FIELD.setAccessible(true);
    }

    public static void setFinalField(Class<?> owner, String name, Object instance, Object value) {
        try {
            setFinalField(owner.getDeclaredField(name), instance, value);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void setFinalField(Field field, Object instance, Object value) {
        try {
            field.setAccessible(true);
            MODIFIERS_FIELD.set(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(instance, value);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

}
