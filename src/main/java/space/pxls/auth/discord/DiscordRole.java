package space.pxls.auth.discord;

public class DiscordRole {
    private final String id;

    private final String name;

    public DiscordRole(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
