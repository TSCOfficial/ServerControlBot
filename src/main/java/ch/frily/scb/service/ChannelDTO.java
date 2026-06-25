package ch.frily.scb.service;

import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.entities.channel.ChannelType;

public record ChannelDTO (
        @Nullable String id,
        int position,
        String name,
        String type,
        @Nullable String topic) {

    public ChannelDTO(String id, int position, String name, ChannelType type) {
        this(id, position, name, type, null);
    }

    public ChannelDTO(String id, int position, String name, ChannelType type, String topic) {
        this(id, position, name, type.name(), topic);
    }
}
