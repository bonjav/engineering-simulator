package com.datacenterflow.project.domain.model;

public enum ProjectRole {
    VIEWER, EDITOR, OWNER;

    public boolean canEdit() {
        return this.ordinal() >= EDITOR.ordinal();
    }

    public boolean isOwner() {
        return this == OWNER;
    }
}
