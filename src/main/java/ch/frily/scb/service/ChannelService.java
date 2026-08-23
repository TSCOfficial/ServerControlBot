package ch.frily.scb.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.channel.concrete.*;
import net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChannelService {

    /**
     * Discord positions channels by sort-bucket (text-based, voice-based, category)
     */
    private enum ChannelGroup {
        CATEGORY,
        TEXT_BASED,
        VOICE_BASED
    }

    /**
     * Get the channel group for a given channel type
     * @param type the channel type
     * @return the channel group
     */
    private ChannelGroup toGroup(ChannelType type) {
        return switch (type) {
            case CATEGORY -> ChannelGroup.CATEGORY;
            case TEXT, NEWS, FORUM -> ChannelGroup.TEXT_BASED;
            case VOICE, STAGE -> ChannelGroup.VOICE_BASED;
            default -> throw new IllegalArgumentException("Unsupported channel type: " + type);
        };
    }


    /**
     * Get all channels (and categories)
     * @param guild the guild to get the channels from
     * @return a {@link List} of {@link GuildChannel}
     */
    public List<ChannelDTO> getChannels(Guild guild) {
        List<GuildChannel> guildChannels = guild.getChannels();
        List<ChannelDTO> validChannels = new ArrayList<>();

        for (GuildChannel channel : guildChannels) {
            // Filter out channels that are dependent on other channels (such as Threads)
            switch (channel.getType()) {
                case CATEGORY -> {
                    Category category = (Category) channel;
                    ChannelDTO channelDTO = new ChannelDTO(category.getId(), category.getPositionRaw(), category.getName(), category.getType(), null);
                    validChannels.add(channelDTO);
                }
                // Text-based channels
                case TEXT -> {
                    TextChannel textChannel = (TextChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(textChannel.getId(), textChannel.getPositionRaw(), textChannel.getName(), textChannel.getType(), textChannel.getTopic(), textChannel.getParentCategory());
                    validChannels.add(channelDTO);
                }
                case NEWS -> {
                    NewsChannel newsChannel = (NewsChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(newsChannel.getId(), newsChannel.getPositionRaw(), newsChannel.getName(), newsChannel.getType(), newsChannel.getTopic(), newsChannel.getParentCategory());
                    validChannels.add(channelDTO);
                }
                case FORUM -> {
                    ForumChannel forumChannel = (ForumChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(forumChannel.getId(), forumChannel.getPositionRaw(), forumChannel.getName(), forumChannel.getType(), forumChannel.getTopic(), forumChannel.getParentCategory());
                    validChannels.add(channelDTO);
                }

                // Voice based channels
                case VOICE -> {
                    VoiceChannel voiceChannel = (VoiceChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(voiceChannel.getId(), voiceChannel.getPositionRaw(), voiceChannel.getName(), voiceChannel.getType(), null, voiceChannel.getParentCategory());
                    validChannels.add(channelDTO);
                }
                case STAGE -> {
                    StageChannel stageChannel = (StageChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(stageChannel.getId(), stageChannel.getPositionRaw(), stageChannel.getName(), stageChannel.getType(), null, stageChannel.getParentCategory());
                    validChannels.add(channelDTO);
                }
                default -> {}
            }

        }
        return validChannels;
    }

    /**
     * Create a new channel
     * @param guild the guild to create the channel in
     * @param dto channel data
     * @return a {@link CompletableFuture} with the created channel
     */
    private CompletableFuture<GuildChannel> createChannelAsync(Guild guild, ChannelDTO dto) {
        ChannelType type = ChannelType.valueOf(dto.type());
        CompletableFuture<? extends GuildChannel> future = switch (type) {
            case CATEGORY -> guild.createCategory(dto.name()).submit();
            case TEXT -> guild.createTextChannel(dto.name()).setTopic(dto.topic()).submit();
            case NEWS -> guild.createNewsChannel(dto.name()).setTopic(dto.topic()).submit();
            case FORUM -> guild.createForumChannel(dto.name()).setTopic(dto.topic()).submit();
            case VOICE -> guild.createVoiceChannel(dto.name()).submit();
            case STAGE -> guild.createStageChannel(dto.name()).submit();
            default -> throw new IllegalArgumentException("Unsupported channel type: " + type);
        };
        return future.thenApply(channel -> (GuildChannel) channel); // add channel as return value if completed successfully
    }

    /**
     * Update channel name and topic
     * @param guild the guild to update the channel in
     * @param dto channel data
     * @return a {@link CompletableFuture}
     */
    private CompletableFuture<Void> updateChannelMetaAsync(Guild guild, ChannelDTO dto) {
        ChannelType type = ChannelType.valueOf(dto.type());
        return switch (type) {
            case CATEGORY -> {
                Category category = guild.getCategoryById(dto.id());
                if (category == null) throw new IllegalArgumentException("Unknown category: " + dto.id());
                yield category.getManager().setName(dto.name()).submit();
            }
            // Text-based channels
            case TEXT -> {
                TextChannel channel = guild.getTextChannelById(dto.id());
                if (channel == null) throw new IllegalArgumentException("Unknown text channel: " + dto.id());
                TextChannelManager manager = channel.getManager();
                manager.setName(dto.name());
                manager.setTopic(dto.topic());
                yield manager.submit();
            }
            case NEWS -> {
                NewsChannel channel = guild.getNewsChannelById(dto.id());
                if (channel == null) throw new IllegalArgumentException("Unknown news channel: " + dto.id());
                NewsChannelManager manager = channel.getManager();
                manager.setName(dto.name());
                manager.setTopic(dto.topic());
                yield manager.submit();
            }
            case FORUM -> {
                ForumChannel channel = guild.getForumChannelById(dto.id());
                if (channel == null) throw new IllegalArgumentException("Unknown forum channel: " + dto.id());
                ForumChannelManager manager = channel.getManager();
                manager.setName(dto.name());
                manager.setTopic(dto.topic());
                yield manager.submit();
            }
            // Voice based channels
            case VOICE -> {
                VoiceChannel channel = guild.getVoiceChannelById(dto.id());
                if (channel == null) throw new IllegalArgumentException("Unknown voice channel: " + dto.id());
                VoiceChannelManager manager = channel.getManager();
                manager.setName(dto.name());
                yield manager.submit();
            }
            case STAGE -> {
                StageChannel channel = guild.getStageChannelById(dto.id());
                if (channel == null) throw new IllegalArgumentException("Unknown stage channel: " + dto.id());
                StageChannelManager manager = channel.getManager();
                manager.setName(dto.name());
                yield manager.submit();
            }
            default -> throw new IllegalArgumentException("Unsupported channel type: " + type);
        };
    }

    /**
     * Update channel positions
     * <p>
     *     For each {@link ChannelGroup}, the position-changes are collected and executed together
     * </p>
     */
    private void applyPositions(Guild guild, List<ChannelDTO> allChannels) {
        Map<ChannelGroup, List<ChannelDTO>> byGroup = allChannels.stream()
                .collect(Collectors.groupingBy(dto -> toGroup(ChannelType.valueOf(dto.type()))));

        applyOrderAction(guild, ChannelGroup.CATEGORY, byGroup.get(ChannelGroup.CATEGORY));
        applyOrderAction(guild, ChannelGroup.TEXT_BASED, byGroup.get(ChannelGroup.TEXT_BASED));
        applyOrderAction(guild, ChannelGroup.VOICE_BASED, byGroup.get(ChannelGroup.VOICE_BASED));
    }

    /**
     * Execute the reposition-action ({@link ChannelOrderAction}
     * @param guild
     * @param group group of channels to reposition
     * @param dtos all channels of the group
     * @return submitted future
     */
    private void applyOrderAction(Guild guild, ChannelGroup group, List<ChannelDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) { // if no channels are provided, return as completed
            return;
        }

        List<ChannelDTO> sorted = dtos.stream()
                .sorted(Comparator.comparingInt(ChannelDTO::position))
                .toList();

        List<ChannelDTO> uncategorized = sorted.stream()
                .filter(dto -> dto.parentId() == null)
                .toList();

        Map<Category, List<ChannelDTO>> groupedByCategory = sorted.stream()
                .filter(dto -> dto.parentId() != null)
                .collect(Collectors.groupingBy(category -> guild.getCategoryById(category.parentId())));

        ChannelOrderAction orderAction = null;
        switch (group) {
            case CATEGORY:
                orderAction = guild.modifyCategoryPositions();
                break;
            case TEXT_BASED:
                orderAction = guild.modifyTextChannelPositions();
                break;
            case VOICE_BASED:
                orderAction = guild.modifyVoiceChannelPositions();
                break;
        }

        // execute orderaction per category so that no conflicts occur???
        ChannelOrderAction finalOrderAction = orderAction;
        uncategorized.forEach(dto -> {
            GuildChannel channel = guild.getGuildChannelById(dto.id());

            finalOrderAction.selectPosition(channel).moveTo(dto.position()).queue();
        });
    }

    /**
     * Add the channel id to the dto
     * @param dto
     * @param id
     * @return
     */
    private ChannelDTO withId(ChannelDTO dto, String id) {
        return new ChannelDTO(id, dto.position(), dto.name(), dto.type(), dto.topic(), dto.parentId());
    }

    /**
     * Executes the changes given by the frontend in bulk, to prevent to many discord-API requests and conflicts while updating everything
     */
    public CompletableFuture<Void> bulkPatchChannels(Guild guild, List<ChannelDTO> channelDtos) {
        List<ChannelDTO> channelsToCreate = channelDtos.stream().filter(c -> c.id() == null).toList();
        List<ChannelDTO> channelsToUpdate = channelDtos.stream().filter(c -> c.id() != null).toList();


        // Create channels
        List<CompletableFuture<ChannelDTO>> createFutures = channelsToCreate.stream()
                .map(dto -> {
                    log.info("Erstelle Channel: {}", dto);
                    return createChannelAsync(guild, dto).thenApply(createdChannel -> withId(dto, createdChannel.getId()));
                })
                .toList();

        // create one future that contains all completed created channels
        CompletableFuture<List<ChannelDTO>> createdAll = CompletableFuture
                .allOf(createFutures.toArray(new CompletableFuture[0])) // turns a List<CompletableFuture<> to an array (CompletableFuture, CompletableFuture, ...)
                .thenApply(_ -> createFutures.stream().map(CompletableFuture::join).toList());

        // Update channel name & topic
        List<CompletableFuture<Void>> metaFutures = channelsToUpdate.stream()
                .map(dto -> {
                    log.info("Aktualisiere Channel: {}", dto);
                    return updateChannelMetaAsync(guild, dto);
                })
                .toList();

        // create one future that contains all completed meta-edits
        CompletableFuture<Void> metaAll = CompletableFuture
                .allOf(metaFutures.toArray(new CompletableFuture[0]));

        // redistribute the new positions
        return createdAll.thenCombine(metaAll, (created, ignored) -> created)
                .thenCompose(created -> {
                    List<ChannelDTO> allPositioned = new ArrayList<>(channelsToUpdate);
                    allPositioned.addAll(created);
                    applyPositions(guild, allPositioned);
                    return CompletableFuture.completedFuture(null);
                });
    }
}
