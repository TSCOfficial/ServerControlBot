package ch.frily.scb;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI serverControlBotOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServerControlBot API")
                        .description("REST API für Discord-Guilds, Channels und Bot-Verwaltung.")
                        .version("1.0.0"));
    }
}
