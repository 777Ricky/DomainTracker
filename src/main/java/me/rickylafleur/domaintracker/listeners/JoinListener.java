package me.rickylafleur.domaintracker.listeners;

import lombok.RequiredArgsConstructor;
import me.lucko.helper.Events;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.rickylafleur.domaintracker.DomainTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * @author Ricky Lafleur
 */
@RequiredArgsConstructor
public class JoinListener implements TerminableModule {

    private final DomainTracker plugin;

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Events.subscribe(PlayerLoginEvent.class)
                .filter(e -> e.getResult() == PlayerLoginEvent.Result.ALLOWED)
                .handler(e -> {
                    Player player = e.getPlayer();
                    String hostname = e.getHostname();

                    if (plugin.getDatabase().playerExists(player.getUniqueId())) return;

                    if (plugin.getConfig().getStringList("domains").contains(hostname)) {
                        plugin.getDatabase().addData(
                                plugin.getFormat().format(new Date()),
                                player.getUniqueId().toString(),
                                hostname,
                                plugin.getDatabase().getCountryFromIp(e.getAddress())
                        );
                    }
                })
                .bindWith(consumer);
    }
}
