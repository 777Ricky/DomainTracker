package me.rickylafleur.domaintracker.commands;

import lombok.RequiredArgsConstructor;
import me.lucko.helper.Commands;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.rickylafleur.domaintracker.DomainTracker;
import me.rickylafleur.domaintracker.utils.Text;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ReloadCommand implements TerminableModule {

    private final DomainTracker plugin;

    @Override
    public void setup(@NotNull TerminableConsumer consumer) {
        Commands.create()
                .assertPermission("domaintracker.admin")
                .handler(c -> {
                    plugin.reloadConfig();
                    c.sender().sendMessage(Text.colorize("&aConfig reloaded."));
                })
                .register("domaintrackerreload", "joinsreload");
    }
}
