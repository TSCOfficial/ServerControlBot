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
        @Nullable ChannelDTO parent) {

    public ChannelDTO(String id, int position, String name, ChannelType type, String topic, Category parent) {
        this(id, position, name, type.name(), topic, mapCategoryToDTO(parent));
    }

    public ChannelDTO(String id, int position, String name, ChannelType type, String topic) {
        this(id, position, name, type.name(), topic, null);
    }

    private static ChannelDTO mapCategoryToDTO(Category category) {
        if (category == null) {
            return null;
        }
        return new ChannelDTO(category.getId(), category.getPosition(), category.getName(), category.getType(), null, null);
    }
}
