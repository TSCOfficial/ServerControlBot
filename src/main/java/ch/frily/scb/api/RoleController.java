package ch.frily.scb.api;

import ch.frily.scb.exception.ExceptionHandler;
import ch.frily.scb.service.ChannelDTO;
import ch.frily.scb.service.ChannelService;
import ch.frily.scb.service.RoleDTO;
import ch.frily.scb.service.RoleService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final JDA jda;

    private final RoleService roleService;

    public RoleController(JDA jda, RoleService roleService) {
        this.jda = jda;
        this.roleService = roleService;
    }

    @GetMapping("{guild_id}")
    public List<RoleDTO> getRoles(@PathVariable("guild_id") String guildId) {
        try {
            Guild guild = jda.getGuildById(guildId);
            List<RoleDTO> roles = roleService.getRoles(guild);
            return roles;
        } catch (Exception exception) {
            return ExceptionHandler.fail(exception);
        }
    }
}
