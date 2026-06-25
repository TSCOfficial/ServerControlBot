package ch.frily.scb.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.channel.concrete.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChannelService {

    /**
     * Get all channels (and categories), sorted by position
     * @param guild the guild to get the channels from
     * @return a {@link List} of {@link GuildChannel}
     */
    public List<ChannelDTO> getChannels(Guild guild) {
        List<GuildChannel> guildChannels = guild.getChannels();

        List<ChannelDTO> validChannels = new ArrayList<>();
        for (GuildChannel channel : guildChannels) {
            // Filter out channels that are dependent on other channels (such as Threads)
            ChannelDTO channelDto;
            switch (channel.getType()) {
                case CATEGORY -> {
                    Category category = (Category) channel;
                    ChannelDTO channelDTO = new ChannelDTO(category.getId(), category.getPosition(), category.getName(), category.getType());
                    validChannels.add(channelDTO);
                }

                // Text based channels
                case TEXT -> {
                    TextChannel textChannel = (TextChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(textChannel.getId(), textChannel.getPosition(), textChannel.getName(), textChannel.getType(), textChannel.getTopic());
                    validChannels.add(channelDTO);
                }
                case NEWS -> {
                    NewsChannel newsChannel = (NewsChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(newsChannel.getId(), newsChannel.getPosition(), newsChannel.getName(), newsChannel.getType().name(), newsChannel.getTopic());
                    validChannels.add(channelDTO);
                }
                case FORUM -> {
                    ForumChannel forumChannel = (ForumChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(forumChannel.getId(), forumChannel.getPosition(), forumChannel.getName(), forumChannel.getType().name(), forumChannel.getTopic());
                    validChannels.add(channelDTO);
                }

                // Voice based channels
                case VOICE -> {
                    VoiceChannel voiceChannel = (VoiceChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(voiceChannel.getId(), voiceChannel.getPosition(), voiceChannel.getName(), voiceChannel.getType());
                    validChannels.add(channelDTO);
                }
                case STAGE -> {
                    StageChannel stageChannel = (StageChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(stageChannel.getId(), stageChannel.getPosition(), stageChannel.getName(), stageChannel.getType());
                    validChannels.add(channelDTO);
                }
                default -> {}
            }

        }
        return validChannels;
    }

    public void createChannel(Guild guild, ChannelDTO channelDto) {
        ChannelType channelType = ChannelType.valueOf(channelDto.type());
        switch (channelType) {
            case CATEGORY -> guild.createCategory(channelDto.name()).queue();
            // Text based channels
            case TEXT -> guild.createTextChannel(channelDto.name()).queue();
            case NEWS -> guild.createNewsChannel(channelDto.name()).queue();
            case FORUM -> guild.createForumChannel(channelDto.name()).queue();
            // Voice based channels
            case VOICE -> guild.createVoiceChannel(channelDto.name()).queue();
            case STAGE -> guild.createStageChannel(channelDto.name()).queue();
        }
    }

    public void updateChannel(Guild guild, ChannelDTO channelDto) {
        ChannelType channelType = ChannelType.valueOf(channelDto.type());
        switch (channelType) {
            case CATEGORY -> {
                CategoryManager categoryManager = guild.getCategoryById(channelDto.id()).getManager();
                categoryManager.setName(channelDto.name());
                categoryManager.queue();
            }
            // Text based channels
            case TEXT -> {
                TextChannelManager textChannelManager = guild.getTextChannelById(channelDto.id()).getManager();
                textChannelManager.setName(channelDto.name());
                textChannelManager.setTopic(channelDto.topic());
                textChannelManager.queue();
            }
            case NEWS -> {
                NewsChannelManager newsChannelManager = guild.getNewsChannelById(channelDto.id()).getManager();
                newsChannelManager.setName(channelDto.name());
                newsChannelManager.setTopic(channelDto.topic());
                newsChannelManager.queue();
            }
            case FORUM -> {
                ForumChannelManager forumChannelManager = guild.getForumChannelById(channelDto.id()).getManager();
                forumChannelManager.setName(channelDto.name());
                forumChannelManager.setTopic(channelDto.topic());
                forumChannelManager.queue();
            }
            // Voice based channels
            case VOICE -> {
                VoiceChannelManager voiceChannelManager = guild.getVoiceChannelById(channelDto.id()).getManager();
                voiceChannelManager.setName(channelDto.name());
                voiceChannelManager.queue();
            }
            case STAGE -> {
                StageChannelManager stageChannelManager = guild.getStageChannelById(channelDto.id()).getManager();
                stageChannelManager.setName(channelDto.name());
                stageChannelManager.queue();
            }
        }
    }


}
