package org.golde.bukkit.pingtest;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.golde.bukkit.pingtest.PingServersBackground.ServerInput;

public class Main extends JavaPlugin implements Listener{

	private static Main instance;
	
	public static final HashMap<String, ServerPinger> SERVER_PLACEHOLDERS = new HashMap<String, ServerPinger>();

	public void onEnable() {
		instance = this;
		Bukkit.getPluginManager().registerEvents(this, this);

		new PingServersBackground(new ServerInput("server1", "localhost", 25566), new ServerInput("server2", "localhost", 25567)).start();

		new BukkitRunnable() {

			@Override
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers()) {
					updateScoreboard(p);
				}
			}

		}.runTaskTimer(this, 0, 20);
	}
	
	 public static Main getInstance() {
		return instance;
	}


	private void updateScoreboard(Player p) {
		final SimpleScoreboard b = new SimpleScoreboard("Example Scoreboard");

		final ServerPinger server1 = SERVER_PLACEHOLDERS.get("server1");
		final ServerPinger server2 = SERVER_PLACEHOLDERS.get("server2");


		if(server1 != null) {
			b.add("Server 1: ");
			b.add("  Online: " + server1.isOnline());
			if(server1.isOnline() && server1.getResponse() != null) {
				b.add("  MOTD: " + server1.getResponse().getDescription());
				b.add("  Players: " + server1.getResponse().getPlayers().getOnline() + "/" + server1.getResponse().getPlayers().getMax());
				b.add("  Ver: " + server1.getResponse().getVersion().getName() + " | " + server1.getResponse().getVersion().getProtocol());
				b.add("  WL: " + server1.getResponse().isWhitelisted());
			}
		}

		
		b.blankLine();
		if(server2 != null) {
			b.add("Server 2: ");
			b.add("  Online: " + server2.isOnline());
			if(server2.isOnline() && server1.getResponse() != null) {
				b.add("  MOTD: " + server2.getResponse().getDescription());
				b.add("  Players: " + server2.getResponse().getPlayers().getOnline() + "/" + server2.getResponse().getPlayers().getMax());
				b.add("  Ver: " + server2.getResponse().getVersion().getName() + " | " + server2.getResponse().getVersion().getProtocol());
				b.add("  WL: " + server2.getResponse().isWhitelisted());
			}
		}
		b.build();
		b.send(p);
	}


}