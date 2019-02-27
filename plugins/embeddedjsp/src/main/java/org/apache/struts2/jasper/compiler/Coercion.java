package org.apache.struts2.jasper.compiler;

public class Coercion {

    public static String coerceToPrimitiveBoolean(String s,
                                                  boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerceToBoolean(" + s + ")";
        } else {
            if (s == null || s.length() == 0)
                return "false";
            else
                return Boolean.valueOf(s).toString();
        }
    }

    public static String coerceToBoolean(String s, boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "(Boolean) org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Boolean.class)";
        } else {
            if (s == null || s.length() == 0) {
                return "new Boolean(false)";
            } else {
                // Detect format error at translation time
                return "new Boolean(" + Boolean.valueOf(s).toString() + ")";
            }
        }
    }

    public static String coerceToPrimitiveByte(String s,
                                               boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerceToByte(" + s + ")";
        } else {
            if (s == null || s.length() == 0)
                return "(byte) 0";
            else
                return "((byte)" + Byte.valueOf(s).toString() + ")";
        }
    }

    public static String coerceToByte(String s, boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "(Byte) org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Byte.class)";
        } else {
            if (s == null || s.length() == 0) {
                return "new Byte((byte) 0)";
            } else {
                // Detect format error at translation time
                return "new Byte((byte)" + Byte.valueOf(s).toString() + ")";
            }
        }
    }

    public static String coerceToChar(String s, boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerceToChar(" + s + ")";
        } else {
            if (s == null || s.length() == 0) {
                return "(char) 0";
            } else {
                char ch = s.charAt(0);
                // this trick avoids escaping issues
                return "((char) " + (int) ch + ")";
            }
        }
    }

    public static String coerceToCharacter(String s, boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "(Character) org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Character.class)";
        } else {
            if (s == null || s.length() == 0) {
                return "new Character((char) 0)";
            } else {
                char ch = s.charAt(0);
                // this trick avoids escaping issues
                return "new Character((char) " + (int) ch + ")";
            }
        }
    }

    public static String coerceToPrimitiveDouble(String s,
                                                 boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerceToDouble(" + s + ")";
        } else {
            if (s == null || s.length() == 0)
                return "(double) 0";
            else
                return Double.valueOf(s).toString();
        }
    }

    public static String coerceToDouble(String s, boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "(Double) org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Double.class)";
        } else {
            if (s == null || s.length() == 0) {
                return "new Double(0)";
            } else {
                // Detect format error at translation time
                return "new Double(" + Double.valueOf(s).toString() + ")";
            }
        }
    }

    public static String coerceToPrimitiveFloat(String s,
                                                boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerceToFloat(" + s + ")";
        } else {
            if (s == null || s.length() == 0)
                return "(float) 0";
            else
                return Float.valueOf(s).toString() + "f";
        }
    }

    public static String coerceToFloat(String s, boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "(Float) org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Float.class)";
        } else {
            if (s == null || s.length() == 0) {
                return "new Float(0)";
            } else {
                // Detect format error at translation time
                return "new Float(" + Float.valueOf(s).toString() + "f)";
            }
        }
    }

    public static String coerceToInt(String s, boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerceToInt(" + s + ")";
        } else {
            if (s == null || s.length() == 0)
                return "0";
            else
                return Integer.valueOf(s).toString();
        }
    }

    public static String coerceToInteger(String s, boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "(Integer) org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Integer.class)";
        } else {
            if (s == null || s.length() == 0) {
                return "new Integer(0)";
            } else {
                // Detect format error at translation time
                return "new Integer(" + Integer.valueOf(s).toString() + ")";
            }
        }
    }

    public static String coerceToPrimitiveShort(String s,
                                                boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerceToShort(" + s + ")";
        } else {
            if (s == null || s.length() == 0)
                return "(short) 0";
            else
                return "((short) " + Short.valueOf(s).toString() + ")";
        }
    }

    public static String coerceToShort(String s, boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "(Short) org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Short.class)";
        } else {
            if (s == null || s.length() == 0) {
                return "new Short((short) 0)";
            } else {
                // Detect format error at translation time
                return "new Short(\"" + Short.valueOf(s).toString() + "\")";
            }
        }
    }

    public static String coerceToPrimitiveLong(String s,
                                               boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerceToLong(" + s + ")";
        } else {
            if (s == null || s.length() == 0)
                return "(long) 0";
            else
                return Long.valueOf(s).toString() + "l";
        }
    }

    public static String coerceToLong(String s, boolean isNamedAttribute) {
        if (isNamedAttribute) {
            return "(Long) org.apache.struts2.jasper.runtime.JspRuntimeLibrary.coerce(" + s + ", Long.class)";
        } else {
            if (s == null || s.length() == 0) {
                return "new Long(0)";
            } else {
                // Detect format error at translation time
                return "new Long(" + Long.valueOf(s).toString() + "l)";
            }
        }
    }
}
