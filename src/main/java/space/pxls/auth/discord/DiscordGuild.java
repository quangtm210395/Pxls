package space.pxls.auth.discord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordGuild {

    private final String id;

    private final String name;

    private final List<DiscordRole> roles;

    public static final HashMap<String, DiscordGuild> canonicalGuilds = new HashMap<>();

    public DiscordGuild(String id, String name, List<DiscordRole> roles) {
        this.id = id;
        this.name = name;
        this.roles = roles;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<DiscordRole> getRoles() {
        return roles;
    }

    public List<String> getRoleIds() {
        return this.roles.stream().map(DiscordRole::getId).toList();
    }

    public static DiscordGuild fromId(String id) {
        return canonicalGuilds.get(id);
    }

    public boolean hasRole(String roleId) {
        return this.getRoleIds().contains(roleId);
    }

    public static void makeCanonical(DiscordGuild guild) {
        canonicalGuilds.put(guild.id, guild);
    }
}
