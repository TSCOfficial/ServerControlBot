package ch.frily.scb.service;

import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

public record ChannelDTO (
        @Nullable String id,
        int position,
        String name,
        String type,
        @Nullable String topic,
        @Nullable String parentId) {

    public ChannelDTO(String id, int position, String name, ChannelType type, String topic, Category parent) {
        this(id, position, name, type.name(), topic, parent == null ? null : parent.getId());
    }

    public ChannelDTO(String id, int position, String name, ChannelType type, String topic) {
        this(id, position, name, type.name(), topic, null);
    }
}
