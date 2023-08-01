package ShopKeep;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import io.github.cdimascio.dotenv.Dotenv;
import reactor.core.publisher.Mono;

public class ShopKeep {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        DiscordClient client = DiscordClient.create(dotenv.get("BOT_TOKEN"));
        Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) ->
                gateway.on(MessageCreateEvent.class, event -> {
                    Message message = event.getMessage();
                    if (message.getContent().equalsIgnoreCase("!ping")) {
                        System.out.println("fuck");
                        return message.getChannel()
                                .flatMap(channel -> channel.createMessage("pong!"));
                    }

                    return Mono.empty();
                }));
        login.block();
    }
}