package ch.frily.scb.api;

import ch.frily.scb.exception.ExceptionHandler;
import ch.frily.scb.service.ChannelDTO;
import ch.frily.scb.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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
    public List<ChannelDTO> getChannels(@PathVariable("guild_id") String guildId) {
        try {
            Guild guild = jda.getGuildById(guildId);
            List<ChannelDTO> channels = channelService.getChannels(guild);
            return channels;
        } catch (Exception exception) {
            return ExceptionHandler.fail(exception);
        }
    }

    @PatchMapping(value = "{guild_id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter patchChannels(@PathVariable("guild_id") String guildId, @RequestBody List<ChannelDTO> channelDtos) {
        SseEmitter emitter = new SseEmitter(0L);

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            sendSafely(emitter, "error", "Guild nicht gefunden");
            emitter.complete();
            return emitter;
        }

        channelService.bulkPatchChannels(guild, channelDtos, progress -> sendSafely(emitter, "progress", progress))
                .whenComplete((ignored, exception) -> {
                    if (exception != null) {
                        ExceptionHandler.handle(exception);
                        sendSafely(emitter, "error", exception.getMessage());
                        emitter.completeWithError(exception);
                    } else {
                        sendSafely(emitter, "done", "OK");
                        emitter.complete();
                    }
                });

        return emitter;
    }

    public void sendSafely(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
