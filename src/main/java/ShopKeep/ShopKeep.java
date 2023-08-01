package ShopKeep;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;
import io.github.cdimascio.dotenv.Dotenv;
import reactor.core.publisher.Mono;

public class ShopKeep {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        DiscordClient client = DiscordClient.create(dotenv.get("BOT_TOKEN"));
        Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) ->
                gateway.on(ReadyEvent.class, event ->
                        Mono.fromRunnable(() -> {
                            final User self = event.getSelf();
                            System.out.printf("Logged in as %s#%s%n", self.getUsername(), self.getDiscriminator());
                        })));
        login.block();
    }
}