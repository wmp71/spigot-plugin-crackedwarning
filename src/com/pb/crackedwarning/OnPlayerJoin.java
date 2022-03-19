package com.pb.crackedwarning;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OnPlayerJoin implements Listener {
	CrackedWarning plugin;
	
	OnPlayerJoin(CrackedWarning plugin) {
		this.plugin = plugin;
	}
	
	/* https://www.spigotmc.org/threads/getting-uuid-from-player-name-using-mojang-api.492896/ */
	public static String getUUID(String name) {
	    String uuid = "";
	    try {
	    	BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()));
	        uuid = (((JsonObject)new JsonParser().parse(in)).get("id")).toString().replaceAll("\"", "");
	        uuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
	        in.close();
	    } catch (Exception e) {
	        uuid = "-";
	    }
	    return uuid;
	}
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String username = player.getName();
		if(player == null) return;
		String ip = player.getAddress().getHostString();
		String realuuid = getUUID(username);
		String oldip = this.plugin.getConfig().getString("cached-ips." + username);
		if(oldip == null) oldip = "";
		if(!realuuid.equals(player.getUniqueId() + "")) {
			if(!(this.plugin.getConfig().getStringList("excluded-usernames").contains(username) || (this.plugin.getConfig().getBoolean("exclude-local-ip") && (ip.equals("127.0.0.1") || ip.startsWith("192.168."))))) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a [{\"color\":\"yellow\",\"bold\":true,\"text\":\"[경고!]: \"},{\"bold\":false,\"color\":\"white\",\"text\":\"" + player.getName() + "이(가) 비정품 또는 크랙된 클라이언트로 접속했습니다. 닉네임을 변경하면 인벤토리가 초기화됩니다.\"}]");
				if(!realuuid.equals("-")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a [{\"color\":\"red\",\"bold\":true,\"text\":\"[경고!]: \"},{\"bold\":false,\"color\":\"white\",\"text\":\"정품 사용자 중 " + player.getName() + "이라는 닉네임이 존재합니다. 사칭 또는 닉네임 도용일 수 있습니다. 채팅 시 허위 정보에 유의해 주세요.\"}]");
				}
			}
			if(!oldip.equals(ip) && !oldip.equals("")) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a [{\"color\":\"red\",\"bold\":true,\"text\":\"[경고!]: \"},{\"bold\":false,\"color\":\"white\",\"text\":\"현재 " + player.getName() + "이(가) 사용중인 IP가 기존 접속 시 사용한 IP와 다릅니다. 닉네임 사칭의 가능성이 있습니다. 피싱 및 악성 링크에 주의해 주세요.\"}]");
				if(player.isOp()) {
					player.setOp(false);
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a [{\"color\":\"red\",\"bold\":true,\"text\":\"[경고!]: \"},{\"bold\":false,\"color\":\"white\",\"text\":\"보안을 위해 " + player.getName() + "의 오피가 박탈되었습니다. 기존에 접속하던 " + player.getName() + "이(가) 맞다면 운영자에게 문의해 주세요.\"}]");
				}
			}
			this.plugin.getConfig().set("cached-ips." + username, ip);
			this.plugin.saveConfig();
		}
	}
}