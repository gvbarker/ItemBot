package ShopKeep;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.json.simple.JSONArray;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

interface Command {
    void execute(MessageCreateEvent event);
}
public class ShopKeep {
    private static final Map<String, Command> commands = new HashMap<>();
    private static final String cmdPrefix = "!";
    private static final Options commandOptions = new Options();
    static {
        commandOptions.addOption("no_weapons", false, "Remove weapons from shop lists.");
        commandOptions.addOption("no_vehicles", false, "Remove vehicles and mounts from shop lists.");
        commandOptions.addOption("no_armors", false, "Remove armor pieces from shop lists.");
        commands.put("ping", event -> event.getMessage()
                .getChannel().block()
                .createMessage("no").block());
        commands.put("gen", event -> {
            event.getMessage()
                    .getChannel().block()
                    .createMessage("heard").block();
            SRDHelper testhelper = new SRDHelper(3,0, new String[]{"-no-vehicles"});
            JSONArray[] testarray = testhelper.generateRequestedItems(3);
        });
        commands.put("help", event -> {
            event.getMessage()
                    .getChannel().block()
                    .createMessage(getHelpMenu().toString()).block();
        });
    }

    private static StringWriter getHelpMenu() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "!gen <# of mundane items> <# of magic items> [options]");
        formatter.printOptions(pw, 80, commandOptions, 0, 6);
        pw.flush();
        return out;
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