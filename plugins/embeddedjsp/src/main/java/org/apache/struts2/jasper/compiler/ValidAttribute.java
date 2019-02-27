package org.apache.struts2.jasper.compiler;

public class ValidAttribute {
    String name;
    boolean mandatory;

    public ValidAttribute(String name, boolean mandatory) {
        this.name = name;
        this.mandatory = mandatory;
    }

    public ValidAttribute(String name) {
        this(name, false);
    }
}
