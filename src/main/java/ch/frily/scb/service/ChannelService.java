package ch.frily.scb.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
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


}
