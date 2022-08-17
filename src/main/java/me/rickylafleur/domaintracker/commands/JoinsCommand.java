package me.rickylafleur.domaintracker.commands;

import lombok.RequiredArgsConstructor;
import me.lucko.helper.Commands;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.rickylafleur.domaintracker.DomainTracker;
import me.rickylafleur.domaintracker.storage.objects.JoinData;
import me.rickylafleur.domaintracker.utils.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Ricky Lafleur
 */
@RequiredArgsConstructor
public class JoinsCommand implements TerminableModule {

    private final DomainTracker plugin;

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Commands.create()
                .assertPlayer()
                .assertPermission("domaintracker.admin")
                .assertUsage("<MM-dd-yyyy>")
                .handler(c -> {
                    final Player player = c.sender();
                    String date = c.arg(0).parseOrFail(String.class);

                    if (date.equalsIgnoreCase("today")) date = plugin.getFormat().format(new Date());

                    Set<JoinData> joinDataSet;
                    if (date.equalsIgnoreCase("all")) {
                        joinDataSet = plugin.getDatabase().getJoinData();
                    } else {
                        joinDataSet = plugin.getDatabase().getJoinData(date);
                    }

                    if (joinDataSet.isEmpty()) {
                        player.sendMessage(Text.colorize("&cInvalid date or no joins for that date."));
                        return;
                    }

                    player.sendMessage(" ");

                    int i = 0;
                    for (String domain : plugin.getConfig().getStringList("domains")) {
                        final List<JoinData> joins = joinDataSet.stream().filter(joinData -> joinData.getDomain().equals(domain)).sorted(Comparator.comparing(JoinData::getCountry)).collect(Collectors.toList());
                        final List<String> countries = joins.stream().map(JoinData::getCountry).collect(Collectors.toList());

                        final Map<String, Integer> countryJoins = new HashMap<>();
                        plugin.getConfig().getStringList("countries").forEach(country -> {
                            int frequency = Collections.frequency(countries, country);

                            if (frequency <= 0) return;

                            countryJoins.put(country, frequency);
                        });

                        final TextComponent textComponent = Component.text()
                                .content(plugin.getConfig().getStringList("display").get(i) + " - " + joins.size() + " joins")
                                .color(NamedTextColor.GREEN)
                                .build().hoverEvent(Component.text().hoverEvent(HoverEvent.showText(Component.text(countryJoins.entrySet().stream()
                                                .map(join -> join.getKey() + " - " + join.getValue() + " joins")
                                                .collect(Collectors.joining("\n")))))
                                        .color(NamedTextColor.GREEN)
                                        .build());

                        plugin.getLogger().log(Level.INFO, String.valueOf(countryJoins.entrySet().size()));
                        player.sendMessage(textComponent);
                        i++;
                    }

                    player.sendMessage(" ");
                })
                .register("domaintracker", "joins");
    }
}
