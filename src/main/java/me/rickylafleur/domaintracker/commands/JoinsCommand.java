package me.rickylafleur.domaintracker.commands;

import lombok.RequiredArgsConstructor;
import me.lucko.helper.Commands;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.rickylafleur.domaintracker.DomainTracker;

import javax.annotation.Nonnull;

/**
 * @author Ricky Lafleur
 */
@RequiredArgsConstructor
public class JoinsCommand implements TerminableModule {

    private final DomainTracker plugin;

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {

    }
}
