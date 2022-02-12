package me.rickylafleur.domaintracker.commands;

import lombok.RequiredArgsConstructor;
import me.lucko.helper.Commands;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.rayzr522.jsonmessage.JSONMessage;
import me.rickylafleur.domaintracker.DomainTracker;
import me.rickylafleur.domaintracker.storage.objects.JoinData;
import me.rickylafleur.domaintracker.utils.Text;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                    Player player = c.sender();
                    String date = c.arg(0).parseOrFail(String.class);

                    Promise<Set<JoinData>> promise = Promise.start().thenApplyAsync(map -> plugin.getDatabase().getJoinData(date));

                    try {
                        Set<JoinData> joinDataSet = promise.get();

                        if (joinDataSet.isEmpty()) {
                            player.sendMessage(Text.colorize("&cInvalid date or no joins for that date."));
                            return;
                        }

                        int i = 0;
                        for (String domain : plugin.getConfig().getStringList("domains")) {
                            List<JoinData> joins = joinDataSet.stream().filter(joinData -> joinData.getDomain().equals(domain)).sorted(Comparator.comparing(JoinData::getCountry)).collect(Collectors.toList());
                            List<String> countries = joins.stream().map(JoinData::getCountry).collect(Collectors.toList());

                            Map<String, Integer> countryJoins = new HashMap<>();
                            plugin.getConfig().getStringList("countries").forEach(country -> {
                                int frequency = Collections.frequency(countries, country);

                                if (frequency <= 0) return;

                                countryJoins.put(country, frequency);
                            });

                            JSONMessage display = JSONMessage.create(Text.colorize("&a" + plugin.getConfig().getStringList("display").get(i) + " &8- &7" + joins.size() + " joins"))
                                    .tooltip(countryJoins.entrySet().stream().map(join -> Text.colorize("&a&l" + join.getKey() + " &8- &7" + join.getValue() + " joins")).collect(Collectors.joining("\n")));

                            display.send(player);
                            i++;
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                })
                .register("domaintracker");
    }
}
