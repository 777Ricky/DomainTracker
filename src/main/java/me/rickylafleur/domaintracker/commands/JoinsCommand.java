package me.rickylafleur.domaintracker.commands;

import lombok.RequiredArgsConstructor;
import me.lucko.helper.Commands;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.rickylafleur.domaintracker.DomainTracker;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

/**
 * @author Ricky Lafleur
 */
@RequiredArgsConstructor
public class JoinsCommand implements TerminableModule {

    private final DomainTracker plugin;

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Commands.create()
                .assertPermission("domaintracker.admin")
                .assertUsage("<MM/dd/yyyy>")
                .handler(c -> {
                    CommandSender sender = c.sender();
                    String date = c.arg(0).parseOrFail(String.class);


                })
                .register("domaintracker");
    }
}
