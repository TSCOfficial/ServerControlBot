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
                    ChannelDTO channelDTO = new ChannelDTO(category.getIdLong(), category.getName(), category.getType().name());
                    validChannels.add(channelDTO);
                }

                // Text based channels
                case TEXT -> {
                    TextChannel textChannel = (TextChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(textChannel.getIdLong(), textChannel.getName(), textChannel.getType().name(), textChannel.getTopic());
                    validChannels.add(channelDTO);
                }
                case NEWS -> {
                    NewsChannel newsChannel = (NewsChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(newsChannel.getIdLong(), newsChannel.getName(), newsChannel.getType().name(), newsChannel.getTopic());
                    validChannels.add(channelDTO);
                }
                case FORUM -> {
                    ForumChannel forumChannel = (ForumChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(forumChannel.getIdLong(), forumChannel.getName(), forumChannel.getType().name(), forumChannel.getTopic());
                    validChannels.add(channelDTO);
                }

                // Voice based channels
                case VOICE -> {
                    VoiceChannel voiceChannel = (VoiceChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(voiceChannel.getIdLong(), voiceChannel.getName(), voiceChannel.getType().name());
                    validChannels.add(channelDTO);
                }
                case STAGE -> {
                    StageChannel stageChannel = (StageChannel) channel;
                    ChannelDTO channelDTO = new ChannelDTO(stageChannel.getIdLong(), stageChannel.getName(), stageChannel.getType().name());
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
                CategoryManager categoryManager = guild.getCategoryById(channelDto.channelId()).getManager();
                categoryManager.setName(channelDto.name());
                categoryManager.queue();
            }
            // Text based channels
            case TEXT -> {
                TextChannelManager textChannelManager = guild.getTextChannelById(channelDto.channelId()).getManager();
                textChannelManager.setName(channelDto.name());
                textChannelManager.setTopic(channelDto.topic());
                textChannelManager.queue();
            }
            case NEWS -> {
                NewsChannelManager newsChannelManager = guild.getNewsChannelById(channelDto.channelId()).getManager();
                newsChannelManager.setName(channelDto.name());
                newsChannelManager.setTopic(channelDto.topic());
                newsChannelManager.queue();
            }
            case FORUM -> {
                ForumChannelManager forumChannelManager = guild.getForumChannelById(channelDto.channelId()).getManager();
                forumChannelManager.setName(channelDto.name());
                forumChannelManager.setTopic(channelDto.topic());
                forumChannelManager.queue();
            }
            // Voice based channels
            case VOICE -> {
                VoiceChannelManager voiceChannelManager = guild.getVoiceChannelById(channelDto.channelId()).getManager();
                voiceChannelManager.setName(channelDto.name());
                voiceChannelManager.queue();
            }
            case STAGE -> {
                StageChannelManager stageChannelManager = guild.getStageChannelById(channelDto.channelId()).getManager();
                stageChannelManager.setName(channelDto.name());
                stageChannelManager.queue();
            }
        }
    }


}
