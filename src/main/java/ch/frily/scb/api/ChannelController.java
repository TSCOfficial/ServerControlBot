package ch.frily.scb.api;

import ch.frily.scb.exception.ExceptionHandler;
import ch.frily.scb.service.ChannelDTO;
import ch.frily.scb.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    private final JDA jda;

    private final ChannelService channelService;

    public ChannelController(JDA jda, ChannelService channelService) {
        this.jda = jda;
        this.channelService = channelService;
    }

    @GetMapping("{guild_id}")
    public List<ChannelDTO> getChannel(@PathVariable("guild_id") String guildId) {
        try {
            Guild guild = jda.getGuildById(guildId);
            List<ChannelDTO> channels = channelService.getChannels(guild);
            log.info("Channels ({}): {}", channels.size(), channels);
            return channels;
        } catch (Exception exception) {
            return ExceptionHandler.fail(exception);
        }
    }

    @PatchMapping("{guild_id}")
    public void patchChannels(@PathVariable("guild_id") String guildId, @RequestBody List<ChannelDTO> channelDtos) {
        try {
            Guild guild = jda.getGuildById(guildId);
            channelDtos.forEach(channelDto -> {
                if (channelDto.channelId() != null) {
                    channelService.updateChannel(guild, channelDto);
                } else {
                    channelService.createChannel(guild, channelDto);
                }
            });
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }

    }
}
