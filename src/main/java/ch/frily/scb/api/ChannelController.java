package ch.frily.scb.api;

import ch.frily.scb.exception.ExceptionHandler;
import ch.frily.scb.service.ChannelDTO;
import ch.frily.scb.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
            return channels;
        } catch (Exception exception) {
            return ExceptionHandler.fail(exception);
        }
    }

    @PatchMapping("{guild_id}")
    public CompletableFuture<ResponseEntity<Void>> patchChannels(@PathVariable("guild_id") String guildId, @RequestBody List<ChannelDTO> channelDtos) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
        }

        return channelService.bulkPatchChannels(guild, channelDtos)
                .thenApply(_ -> ResponseEntity.noContent().<Void>build()) // sets type to Void explicitly bc it can happen (through chaining futures), that the type is lost and fallback is Object
                .exceptionally(exception -> {
                    ExceptionHandler.handle(exception);
                    return ResponseEntity.badRequest().build();
                });
    }
}
