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
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a [{\"color\":\"yellow\",\"bold\":true,\"text\":\"[���!]: \"},{\"bold\":false,\"color\":\"white\",\"text\":\"" + player.getName() + "��(��) ����ǰ �Ǵ� ũ���� Ŭ���̾�Ʈ�� �����߽��ϴ�. �г����� �����ϸ� �κ��丮�� �ʱ�ȭ�˴ϴ�.\"}]");
				if(!realuuid.equals("-")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a [{\"color\":\"red\",\"bold\":true,\"text\":\"[���!]: \"},{\"bold\":false,\"color\":\"white\",\"text\":\"��ǰ ����� �� " + player.getName() + "�̶�� �г����� �����մϴ�. ��Ī �Ǵ� �г��� ������ �� �ֽ��ϴ�. ä�� �� ���� ������ ������ �ּ���.\"}]");
				}
			}
			if(!oldip.equals(ip) && !oldip.equals("")) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a [{\"color\":\"red\",\"bold\":true,\"text\":\"[���!]: \"},{\"bold\":false,\"color\":\"white\",\"text\":\"���� " + player.getName() + "��(��) ������� IP�� ���� ���� �� ����� IP�� �ٸ��ϴ�. �г��� ��Ī�� ���ɼ��� �ֽ��ϴ�. �ǽ� �� �Ǽ� ��ũ�� ������ �ּ���.\"}]");
				if(player.isOp()) {
					player.setOp(false);
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a [{\"color\":\"red\",\"bold\":true,\"text\":\"[���!]: \"},{\"bold\":false,\"color\":\"white\",\"text\":\"������ ���� " + player.getName() + "�� ���ǰ� ��Ż�Ǿ����ϴ�. ������ �����ϴ� " + player.getName() + "��(��) �´ٸ� ��ڿ��� ������ �ּ���.\"}]");
				}
			}
			this.plugin.getConfig().set("cached-ips." + username, ip);
			this.plugin.saveConfig();
		}
	}
}