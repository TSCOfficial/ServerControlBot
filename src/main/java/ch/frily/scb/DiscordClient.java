package ch.frily.scb;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordClient {

    @Bean
    public JDA jda(@Value("${credentials.discord.token}") String token) throws InterruptedException {

        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.setStatus(OnlineStatus.ONLINE);
        jdaBuilder.setAutoReconnect(true);
        return jdaBuilder.build().awaitReady();
    }
}
