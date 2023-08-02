package ShopKeep;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import io.github.cdimascio.dotenv.Dotenv;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

interface Command {
    void execute(MessageCreateEvent event);
}
public class ShopKeep {
    private static final Map<String, Command> commands = new HashMap<>();
    private static final SRDJSONHelper reader = new SRDJSONHelper();
    private static final String cmdPrefix = "!";
    static {
        commands.put("ping", event -> event.getMessage()
                .getChannel().block()
                .createMessage("no").block());
        commands.put("test", event -> {
            event.getMessage()
                    .getChannel().block()
                    .createMessage("heard").block();
            reader.test();
            String testing = event.getMessage().getContent();
            System.out.println(testing);
        });
    }
    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();
        final GatewayDiscordClient client = DiscordClientBuilder.create(dotenv.get("BOT_TOKEN")).build()
                .login()
                .block();

        client.getEventDispatcher().on(MessageCreateEvent.class)
                        .subscribe(event -> {
                            final String content = event.getMessage().getContent();
                            for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                                if (content.startsWith(cmdPrefix+entry.getKey())) {
                                    entry.getValue().execute(event);
                                    break;
                                }
                            }
                        });

        client.onDisconnect().block();
//        DiscordClient client = DiscordClient.create(dotenv.get("BOT_TOKEN"));
//        Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) ->
//                gateway.on(MessageCreateEvent.class, event -> {
//                    Message message = event.getMessage();
//                    if (message.getContent().equalsIgnoreCase("!ping")) {
//                        System.out.println("fuck");
//                        return message.getChannel()
//                                .flatMap(channel -> channel.createMessage("pong!"));
//                    }
//
//                    return Mono.empty();
//                }));
//        login.block();
    }
}