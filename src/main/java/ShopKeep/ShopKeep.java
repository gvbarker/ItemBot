package ShopKeep;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.cli.*;
import org.json.simple.JSONArray;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

interface Command {
    void execute(MessageCreateEvent event);
}
public class ShopKeep {
    private static final Map<String, String> filterMap = new HashMap<>(Map.of(
            "no_vehicles", "mounts-and-vehicles",
            "no_weapons", "weapon",
            "no_armors", "armor"
    ));
    private static final Map<String, Command> commands = new HashMap<>();
    private static final String cmdPrefix = "!";
    private static final Options commandOptions = new Options();
    static {
        commandOptions.addOption("no_weapons", false, "\tRemove weapons from shop lists.");
        commandOptions.addOption("no_vehicles", false, "\tRemove vehicles and mounts from shop lists.");
        commandOptions.addOption("no_armors", false, "\tRemove armor pieces from shop lists.");

        commands.put("ping", event -> event.getMessage()
                .getChannel().block()
                .createMessage("no").block());

        commands.put("gen", event -> {
            CommandLineParser clp = new DefaultParser();
            ArrayList<String> filters = new ArrayList<>();
            int mundane = 0, magical = 0, iterations = 0;
            try {
                CommandLine cl = clp.parse(commandOptions, event.getMessage()
                        .getContent()
                        .split(" "));
                filterMap.forEach((filter, srdindex) -> {
                        if (cl.hasOption(filter)) {
                            filters.add(srdindex);
                        }});
                mundane = Integer.parseInt(cl.getArgList().get(1));
                magical = Integer.parseInt(cl.getArgList().get(2));
                iterations = Integer.parseInt(cl.getArgList().get(3));

            } catch (IndexOutOfBoundsException | ParseException e) {
                event.getMessage()
                        .getChannel().block()
                        .createMessage(generateHelpMenu()).block();
            }
            SRDHelper itemGenerator = new SRDHelper(mundane, magical, filters.toArray(new String[0]));
            JSONArray[] reqItems = itemGenerator.generateRequestedItems(iterations);
        });
        commands.put("help", event -> {
            event.getMessage()
                    .getChannel().block()
                    .createMessage(generateHelpMenu()).block();
        });
    }

    private static EmbedCreateSpec generateHelpMenu() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 100, "!gen <# of mundane items> <# of magic items> <# of shop inventories to generate> [options]");
        formatter.printOptions(pw, 80, commandOptions, 0, 6);
        pw.flush();
        String[] helpLines = out.toString().split("\n");
        EmbedCreateSpec helpEmbed = EmbedCreateSpec.builder()
                .color(Color.RED)
                .title("Help Command")
                .description("Displays !gen syntax")
                .addField("\u200B", "\u200B", false)
                .addField("Syntax", helpLines[0], false)
                .addField("\u200B", "\u200B", false)
                .addField("Options", "", false)
                .addField(helpLines[1], helpLines[2], false)
                .addField(helpLines[3], helpLines[4], false)
                .addField(helpLines[5], helpLines[6], false)
                .timestamp(Instant.now())
                .build();
        return helpEmbed;
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