package com.company.demo.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum Gender implements EnumClass<String> {

    MALE("M"),
    FEMALE("F");

    private String id;

    Gender(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static Gender fromId(String id) {
        for (Gender at : Gender.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}