package com.codacy.client.stash.client.auth;

/**
 * Helper class for sorting query and form parameters that we need
 */
public class Parameter implements Comparable<Parameter> {
    private final String key, value;

    public Parameter(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    @Override
    public int compareTo(Parameter other) {
        int diff = key.compareTo(other.key);
        if (diff == 0) {
            diff = value.compareTo(other.value);
        }
        return diff;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameter parameter = (Parameter) o;

        if (!key.equals(parameter.key)) return false;
        if (!value.equals(parameter.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}