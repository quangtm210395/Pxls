package space.pxls.auth;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import space.pxls.App;
import space.pxls.auth.discord.DiscordGuild;
import space.pxls.auth.discord.DiscordRole;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DiscordAuthService extends AuthService {
    public DiscordAuthService(String id) {
        super(id, App.getConfig().getBoolean("oauth.discord.enabled"), App.getConfig().getBoolean("oauth.discord.registrationEnabled"));
    }

    public String getRedirectUrl(String state) {
        return "https://discord.com/api/oauth2/authorize?client_id=" + App.getConfig().getString("oauth.discord.key") + "&response_type=code&redirect_uri=" + getCallbackUrl() + "&duration=temporary&scope=identify%20guilds%20guilds.members.read&state=" + state;
    }

    public String getToken(String code) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.post("https://discord.com/api/oauth2/token")
                .header("User-Agent", "pxls.space")
                .field("grant_type", "authorization_code")
                .field("code", code)
                .field("redirect_uri", getCallbackUrl())
                .field("client_id", App.getConfig().getString("oauth.discord.key"))
                .field("client_secret", App.getConfig().getString("oauth.discord.secret"))
//                .basicAuth(App.getConfig().getString("oauth.discord.key"), App.getConfig().getString("oauth.discord.secret"))
                .asJson();

        JSONObject json = response.getBody().getObject();

        if (json.has("error")) {
            return null;
        } else {
            return json.getString("access_token");
        }
    }

    public String getIdentifier(String token) throws UnirestException, InvalidAccountException {
        HttpResponse<JsonNode> me = Unirest.get("https://discord.com/api/users/@me")
                .header("Authorization", "Bearer " + token)
                .header("User-Agent", "pxls.space")
                .asJson();
        JSONObject json = me.getBody().getObject();
        if (json.has("error")) {
            return null;
        } else {
            long id = json.getLong("id");
            long signupTimeMillis = (id >> 22) + 1420070400000L;
            long ageMillis = System.currentTimeMillis() - signupTimeMillis;

            long minAgeMillis = App.getConfig().getDuration("oauth.discord.minAge", TimeUnit.MILLISECONDS);
            if (ageMillis < minAgeMillis){
                long days = minAgeMillis / 86400 / 1000;
                throw new InvalidAccountException("Account too young");
            }
            return json.getString("id");
        }
    }

    public static List<String> getGuildMember(String token, String guildId) {
        HttpResponse<JsonNode> me = Unirest.get("https://discord.com/api/users/@me/guilds/" + guildId + "/member")
                .header("Authorization", "Bearer " + token)
                .header("User-Agent", "pxls.space")
                .asJson();

        JSONObject json = me.getBody().getObject();
        if (json.has("error")) {
            return null;
        } else {
            List<String> roles = json.getJSONArray("roles").toList();
            return roles;
        }
    }

    public static boolean getGuilds(String token) {
        HttpResponse<JsonNode> me = Unirest.get("https://discord.com/api/users/@me/guilds")
                .header("Authorization", "Bearer " + token)
                .header("User-Agent", "pxls.space")
                .asJson();

        JSONArray json = me.getBody().getArray();
        for (int i = 0; i < json.length(); i+=1) {
            var guildJson = json.getJSONObject(i);
            var guildId = guildJson.getString("id");
            var guild = DiscordGuild.fromId(guildId);
            if (guild != null) {
                var roles = getGuildMember(token, guildId);
                if (roles != null && checkRoles(roles, guild)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkRoles(List<String> roles, DiscordGuild guild) {
        for (var role: roles) {
            if(guild != null && guild.hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return "Discord";
    }

    @Override
    public void reloadEnabledState() {
        this.enabled = App.getConfig().getBoolean("oauth.discord.enabled");
        this.registrationEnabled = App.getConfig().getBoolean("oauth.discord.registrationEnabled");
    }
}
