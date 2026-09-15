package ch.frily.scb.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.channel.concrete.*;
import net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
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
                    log.info(category.getName());
                    log.info(category.getPermissionContainer().getPermissionOverrides().toString());
                    category.getPermissionContainer().getPermissionOverrides().forEach((permission) -> {
                        log.info("Allowed: {}", permission.getAllowed().stream().map(perm -> {
                            return String.format("%s (%d)", perm.getName(), perm.getOffset());
                        }).toList());
                        log.info("Denied: {}", permission.getDenied().stream().map(perm -> {
                            return String.format("%s (%d)", perm.getName(), perm.getOffset());
                        }).toList());
                        log.info("Inherit: {}", permission.getInherit().stream().map(perm -> {
                            return String.format("%s (%d)", perm.getName(), perm.getOffset());
                        }).toList());
                    });
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
                CategoryManager manager = category.getManager();
                boolean changed = false;
                if (!category.getName().equals(dto.name())) {
                    manager.setName(dto.name());
                    changed = true;
                }
                yield changed ? manager.submit() : CompletableFuture.completedFuture(null);
            }
            case TEXT -> {
                TextChannel channel = guild.getTextChannelById(dto.id());
                if (channel == null) throw new IllegalArgumentException("Unknown text channel: " + dto.id());
                TextChannelManager manager = channel.getManager();
                boolean changed = false;
                if (!channel.getName().equals(dto.name())) {
                    manager.setName(dto.name());
                    changed = true;
                }
                if (!java.util.Objects.equals(channel.getTopic(), dto.topic())) {
                    manager.setTopic(dto.topic());
                    changed = true;
                }
                yield changed ? manager.submit() : CompletableFuture.completedFuture(null);
            }
            case NEWS -> {
                NewsChannel channel = guild.getNewsChannelById(dto.id());
                if (channel == null) throw new IllegalArgumentException("Unknown news channel: " + dto.id());
                NewsChannelManager manager = channel.getManager();
                boolean changed = false;
                if (!channel.getName().equals(dto.name())) {
                    manager.setName(dto.name());
                    changed = true;
                }
                if (!java.util.Objects.equals(channel.getTopic(), dto.topic())) {
                    manager.setTopic(dto.topic());
                    changed = true;
                }
                yield changed ? manager.submit() : CompletableFuture.completedFuture(null);
            }
            case FORUM -> {
                ForumChannel channel = guild.getForumChannelById(dto.id());
                if (channel == null) throw new IllegalArgumentException("Unknown forum channel: " + dto.id());
                ForumChannelManager manager = channel.getManager();
                boolean changed = false;
                if (!channel.getName().equals(dto.name())) {
                    manager.setName(dto.name());
                    changed = true;
                }
                if (!java.util.Objects.equals(channel.getTopic(), dto.topic())) {
                    manager.setTopic(dto.topic());
                    changed = true;
                }
                yield changed ? manager.submit() : CompletableFuture.completedFuture(null);
            }
            // Voice based channels
            case VOICE -> {
                VoiceChannel channel = guild.getVoiceChannelById(dto.id());
                if (channel == null) throw new IllegalArgumentException("Unknown voice channel: " + dto.id());
                VoiceChannelManager manager = channel.getManager();
                boolean changed = false;
                if (!channel.getName().equals(dto.name())) {
                    manager.setName(dto.name());
                    changed = true;
                }
                yield changed ? manager.submit() : CompletableFuture.completedFuture(null);
            }
            case STAGE -> {
                StageChannel channel = guild.getStageChannelById(dto.id());
                if (channel == null) throw new IllegalArgumentException("Unknown voice channel: " + dto.id());
                StageChannelManager manager = channel.getManager();
                boolean changed = false;
                if (!channel.getName().equals(dto.name())) {
                    manager.setName(dto.name());
                    changed = true;
                }
                yield changed ? manager.submit() : CompletableFuture.completedFuture(null);
            }
            // NEWS, FORUM analog zu TEXT (Name + Topic vergleichen)
            // VOICE, STAGE analog, nur Name vergleichen
            default -> throw new IllegalArgumentException("Unsupported channel type: " + type);
        };
    }

    /**
     * Update channel positions
     * <p>
     *     For each {@link ChannelGroup}, the position-changes are collected and executed together
     * </p>
     */
    private CompletableFuture<Void> applyPositions(Guild guild, List<ChannelDTO> allChannels, Consumer<ProgressEvent> onProgress) {
        Map<ChannelGroup, List<ChannelDTO>> byGroup = allChannels.stream()
                .collect(Collectors.groupingBy(dto -> toGroup(ChannelType.valueOf(dto.type()))));

        CompletableFuture<Void> categoryFuture = applyOrderAction(guild, ChannelGroup.CATEGORY, byGroup.get(ChannelGroup.CATEGORY), onProgress);
        CompletableFuture<Void> textFuture = applyOrderAction(guild, ChannelGroup.TEXT_BASED, byGroup.get(ChannelGroup.TEXT_BASED), onProgress);
        CompletableFuture<Void> voiceFuture = applyOrderAction(guild, ChannelGroup.VOICE_BASED, byGroup.get(ChannelGroup.VOICE_BASED), onProgress);

        return CompletableFuture.allOf(categoryFuture, textFuture, voiceFuture);
    }

    /**
     * Execute the reposition-action ({@link ChannelOrderAction}
     * @param guild
     * @param group group of channels to reposition
     * @param dtos all channels of the group
     * @return submitted future
     */
    private CompletableFuture<Void> applyOrderAction(Guild guild, ChannelGroup group, List<ChannelDTO> dtos, Consumer<ProgressEvent> onProgress) {
        if (dtos == null || dtos.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        List<ChannelDTO> sorted = dtos.stream()
                .sorted(Comparator.comparingInt(ChannelDTO::position))
                .toList();

        // Kanäle trennen: echte parent_id-Änderung vs. reine Positions-Änderung
        List<ChannelDTO> parentChanges = new ArrayList<>();
        for (ChannelDTO dto : sorted) {
            GuildChannel channel = guild.getGuildChannelById(dto.id());
            if (channel == null) continue;
            if (!Objects.equals(currentParentId(channel), dto.parentId())) {
                parentChanges.add(dto);
            }
        }

        int totalSteps = 1 + parentChanges.size();
        AtomicInteger step = new AtomicInteger(0);

        // 1) Ein Sammel-Request: Positionen für ALLE Kanäle der Gruppe, ohne parent_id zu berühren
        CompletableFuture<Void> chained = sendOrderAction(guild, group, sorted, null, onProgress, step.incrementAndGet(), totalSteps);

        // 2) Für jeden Kanal mit echter Parent-Änderung: eigener, sequenzieller Request
        for (ChannelDTO target : parentChanges) {
            chained = chained.thenCompose(ignored ->
                    sendOrderAction(guild, group, sorted, target, onProgress, step.incrementAndGet(), totalSteps));
        }

        return chained;
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
     * Sendet einen einzelnen Bulk-Positions-Request.
     * Ist {@code parentChangeTarget} gesetzt, bekommt NUR dieser eine Kanal
     * zusätzlich eine neue Kategorie zugewiesen (Discord erlaubt max. 1 pro Request).
     */
    private CompletableFuture<Void> sendOrderAction(Guild guild, ChannelGroup group, List<ChannelDTO> sorted,
                                                    ChannelDTO parentChangeTarget, Consumer<ProgressEvent> onProgress,
                                                    int step, int totalSteps) {
        ChannelOrderAction orderAction = switch (group) {
            case CATEGORY -> guild.modifyCategoryPositions();
            case TEXT_BASED -> guild.modifyTextChannelPositions();
            case VOICE_BASED -> guild.modifyVoiceChannelPositions();
        };

        for (ChannelDTO dto : sorted) {
            GuildChannel channel = guild.getGuildChannelById(dto.id());
            if (channel == null) continue;

            ChannelOrderAction selected = orderAction.selectPosition(channel).moveTo(dto.position());

            if (parentChangeTarget != null && dto.id().equals(parentChangeTarget.id())) {
                Category newParent = dto.parentId() == null ? null : guild.getCategoryById(dto.parentId());
                selected.setCategory(newParent);
            }
        }

        return orderAction.submit().thenAccept(ignored -> {
            String message = parentChangeTarget != null
                    ? "Kanal verschoben: " + parentChangeTarget.name()
                    : "Positionen aktualisiert (" + group + ")";
            onProgress.accept(new ProgressEvent(group.name(), step, totalSteps, message));
        });
    }

    /**
     * Executes the changes given by the frontend in bulk, to prevent to many discord-API requests and conflicts while updating everything
     */
    public CompletableFuture<Void> bulkPatchChannels(Guild guild, List<ChannelDTO> channelDtos, Consumer<ProgressEvent> onProgress) {
        List<ChannelDTO> channelsToCreate = channelDtos.stream().filter(c -> c.id() == null).toList();
        List<ChannelDTO> channelsToUpdate = channelDtos.stream().filter(c -> c.id() != null).toList();

        int totalCreate = channelsToCreate.size();
        AtomicInteger createdCount = new AtomicInteger(0);

        List<CompletableFuture<ChannelDTO>> createFutures = channelsToCreate.stream()
                .map(dto -> {
                    log.info("Erstelle Channel: {}", dto);
                    return createChannelAsync(guild, dto)
                            .thenApply(createdChannel -> withId(dto, createdChannel.getId()))
                            .whenComplete((c, ex) -> {
                                if (ex == null) {
                                    onProgress.accept(new ProgressEvent("CREATE",
                                            createdCount.incrementAndGet(), totalCreate,
                                            "Kanal erstellt: " + dto.name()));
                                }
                            });
                })
                .toList();

        CompletableFuture<List<ChannelDTO>> createdAll = CompletableFuture
                .allOf(createFutures.toArray(new CompletableFuture[0]))
                .thenApply(_ -> createFutures.stream().map(CompletableFuture::join).toList());

        int totalMeta = channelsToUpdate.size();
        AtomicInteger metaCount = new AtomicInteger(0);

        List<CompletableFuture<Void>> metaFutures = channelsToUpdate.stream()
                .map(dto -> updateChannelMetaAsync(guild, dto)
                        .whenComplete((v, ex) -> {
                            if (ex == null) {
                                onProgress.accept(new ProgressEvent("META",
                                        metaCount.incrementAndGet(), totalMeta,
                                        "Metadaten geprüft: " + dto.name()));
                            }
                        }))
                .toList();

        CompletableFuture<Void> metaAll = CompletableFuture.allOf(metaFutures.toArray(new CompletableFuture[0]));

        CompletableFuture<Void> positionFuture = createdAll.thenCompose(created -> {
            List<ChannelDTO> allPositioned = new ArrayList<>(channelsToUpdate);
            allPositioned.addAll(created);
            return applyPositions(guild, allPositioned, onProgress);
        });

        return CompletableFuture.allOf(positionFuture, metaAll);
    }

    private String currentParentId(GuildChannel channel) {
        return switch (channel) {
            case TextChannel c -> c.getParentCategory() == null ? null : c.getParentCategory().getId();
            case NewsChannel c -> c.getParentCategory() == null ? null : c.getParentCategory().getId();
            case ForumChannel c -> c.getParentCategory() == null ? null : c.getParentCategory().getId();
            case VoiceChannel c -> c.getParentCategory() == null ? null : c.getParentCategory().getId();
            case StageChannel c -> c.getParentCategory() == null ? null : c.getParentCategory().getId();
            default -> null;
        };
    }
}
