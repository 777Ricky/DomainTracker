package me.rickylafleur.domaintracker.storage.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * @author Ricky Lafleur
 */
@RequiredArgsConstructor
@Getter
public class JoinData {

    private final String date;
    private final UUID uuid;
    private final String domain;
    private final String country;
}
