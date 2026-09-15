package ch.frily.scb.service;

public record RoleDTO (
        String id,
        String name,
        Color color
){
    public RoleDTO (String id, String name, Color color){
        this.id = id;
        this.name = name;
        this.color = color;
    }
}
