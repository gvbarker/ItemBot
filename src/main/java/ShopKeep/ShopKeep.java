package ShopKeep;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.rest.util.Color;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.cli.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import reactor.core.publisher.Mono;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeoutException;

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
    private static ArrayList<ArrayList<EmbedCreateSpec>> allItems = new ArrayList<>();

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

            if (!allItems.isEmpty()) {
                allItems.clear();
            }
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
            for (int i=0; i<reqItems.length; i++) {
                allItems.add(generateDayEmbed(reqItems[i], i, mundane, magical, filters.toArray(new String[0])));
            }
            for (ArrayList<EmbedCreateSpec> allItem : allItems) {
                event.getMessage()
                        .getChannel().block()
                        .createMessage(allItem.get(0))
                        .withComponents(
                                ActionRow.of(
                                        Button.primary("pageLeft", "<"),
                                        Button.primary("pageRight", ">")))
                        .block();
            }
        });
        commands.put("help", event -> {
            event.getMessage()
                    .getChannel().block()
                    .createMessage(MessageCreateSpec.builder()
                            .addEmbed(generateHelpMenu())
                            .build())
                    .block();
        });
    }

    private static ArrayList<EmbedCreateSpec> generateDayEmbed(JSONArray day, int iteration, int numMun, int numMag, String[] filters) {
        ArrayList<EmbedCreateSpec> dayItems = new ArrayList<>();
        for (Object i : day) {
            EmbedCreateSpec.Builder iterBuilder = EmbedCreateSpec.builder();
            iterBuilder.color(Color.RED)
                    .title("Inventory #"+(iteration+1) + " " + "Item #" + (day.indexOf(i) + 1))
                    .description(String.format("#of Mundane items: %d\n#of Magical items: %d\nFilters: %s", numMun, numMag, Arrays.toString(filters)));

            JSONObject item = (JSONObject) i;
            JSONObject equipmentCat = (JSONObject) item.get("equipment_category");

            iterBuilder.addField("Name", item.get("name").toString(), true);
            iterBuilder.addField("Equipment Type", equipmentCat.get("name").toString(), true);
            if (item.get("cost") != null) {
                JSONObject pricing = (JSONObject) item.get("cost");
                iterBuilder.addField("Rec. Price", pricing.get("quantity") + " " + pricing.get("unit"), true);
            }
            else {
                JSONObject rarity = (JSONObject) item.get("rarity");
                iterBuilder.addField("Rarity", rarity.get("name").toString(), true);
            }
            if (item.get("desc") != null) {
                iterBuilder.addField("Description", item.get("desc").toString(), false);
            }
            iterBuilder.addField("","\u200B", false);
            dayItems.add(iterBuilder.build());
        }
        return dayItems;
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


    private static EmbedCreateSpec getItemEmbedEdit(ButtonInteractionEvent event, int change) {
        String embedItemInfo = (event.getMessage().get()
                .getEmbeds().get(0)
                .getTitle().get());
        int itemNum = Character.getNumericValue(embedItemInfo.charAt(embedItemInfo.length()-1))-1;
        int iterationNum = Character.getNumericValue(embedItemInfo.charAt(11))-1;
        EmbedCreateSpec newItem = allItems.get(iterationNum).get(itemNum);
        try {
            newItem = allItems.get(iterationNum).get(itemNum + change);
        }
        catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        System.out.println(newItem);
        return newItem;
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
        client.getEventDispatcher().on(ButtonInteractionEvent.class, event -> {
//            if (event.getCustomId().equals("pageLeft")) {
//                event.getMessage().get().edit().withEmbeds(allItems.get())
//            }
            switch (event.getCustomId()) {
                case "pageLeft":
                    System.out.println("wrong");
                    event.getMessage().get().edit().withEmbeds(getItemEmbedEdit(event, -1)).subscribe();
                case "pageRight":
                    System.out.println("right");
                    event.getMessage().get().edit().withEmbeds(getItemEmbedEdit(event, 1)).subscribe();
            }


            if (event.getCustomId().equals("testb")) {
                event.getMessage().get().edit().withContent("s").subscribe();
                return event.reply("You clicked me!").withEphemeral(true);
            } else {
                // Ignore it
                return Mono.empty();
            }
        }).subscribe();
        client.onDisconnect().block();
    }
}