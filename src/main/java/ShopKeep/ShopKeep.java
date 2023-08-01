package ShopKeep;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Mono;

public class ShopKeep {
    public static void main(String[] args) {
        System.out.println(System.getenv("BOT_TOKEN"));
        DiscordClient client = DiscordClient.create(System.getenv("BOT_TOKEN"));
        Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> Mono.empty());
        login.block();
    }
}