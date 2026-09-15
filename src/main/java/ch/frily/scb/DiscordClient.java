package ch.frily.scb;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DiscordClient extends ListenerAdapter {

    @Bean
    public JDA jda(@Value("${credentials.discord.token}") String token) throws InterruptedException {
        JDA jda = createClient(token);

        return jda.awaitReady();
    }

    private JDA createClient(String token) {
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.setStatus(OnlineStatus.ONLINE);
        jdaBuilder.setAutoReconnect(true);

        jdaBuilder.addEventListeners(this);
        return jdaBuilder.build();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        log.info("Discord Application Ready");
    }
}
