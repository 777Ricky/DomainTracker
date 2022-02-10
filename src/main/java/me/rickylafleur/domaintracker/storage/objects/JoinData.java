package me.rickylafleur.domaintracker.storage.objects;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * @author Ricky Lafleur
 */
@RequiredArgsConstructor
public class JoinData {

    private final String date;
    private final UUID uuid;
    private final String domain;
    private final String country;

    public String getDate() {
        return date;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDomain() {
        return domain;
    }

    public String getCountry() {
        return country;
    }
}
