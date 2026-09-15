package ch.frily.scb.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    public List<RoleDTO> getRoles(Guild guild) {
        return guild.getRoles().stream().map(role -> {
            Color color = null;
            if (role.getColors().getPrimary() != null) {
                color =  new Color(role.getColors().getPrimary());
            }
            return new RoleDTO(role.getId(), role.getName(), color);
        }).toList();
    }
}
