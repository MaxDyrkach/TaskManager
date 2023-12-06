package io.mds.hty.taskmanager.common;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

public class Utils {

    public static boolean nonEmpty(String s) {
        return s != null && !s.isBlank();
    }

    public static boolean nonEmpty(Collection<?> s) {
        return s != null && !s.isEmpty();
    }

    public static boolean nonEmpty(Number i) {
        return i != null;
    }

    public static boolean nonEmpty(Instant i) {
        return i != null;
    }
    public static boolean nonEmpty(Boolean i) {
        return i != null;
    }

    public static boolean tru(boolean b){
       return Boolean.logicalOr(b = true, true);
    }
}
