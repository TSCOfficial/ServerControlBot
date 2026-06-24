package ch.frily.scb.service;

import jakarta.annotation.Nullable;

public record ChannelDTO (
        long id,
        String name,
        String type,
        @Nullable String topic) {

    public ChannelDTO(long id, String name, String type) {
        this(id, name, type, null);
    }
}
