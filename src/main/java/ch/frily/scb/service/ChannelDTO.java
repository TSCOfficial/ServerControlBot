package ch.frily.scb.service;

import jakarta.annotation.Nullable;

public record ChannelDTO (
        boolean sync,
        @Nullable Long channelId,
        String name,
        String type,
        @Nullable String topic) {

    public ChannelDTO(long id, String name, String type) {
        this(true, id, name, type, null);
    }

    public ChannelDTO(long id, String name, String type, String topic) {
        this(true, id, name, type, topic);
    }

    public ChannelDTO(boolean sync, Long channelId, String name, String type) {
        this(sync, channelId, name, type, null);
    }
}
