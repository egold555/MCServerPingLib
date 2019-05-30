package org.golde.bukkit.pingtest;

import org.bukkit.scheduler.BukkitRunnable;

import lombok.AllArgsConstructor;

public class PingServersBackground extends Thread {

	final ServerInput[] serversToPing;
	
	public PingServersBackground(ServerInput... serversToPing) {
		this.serversToPing = serversToPing;
	}
	
	@Override
	public void run() {
		while(true) {
			try {

				for(final ServerInput input : serversToPing) {
					// Don't overload the CPU or the API
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					final ServerPinger server = new ServerPinger(input.ip, input.port, 100);
					
					new BukkitRunnable() {

						@Override
						public void run() {
							Main.SERVER_PLACEHOLDERS.put(input.name, server);
						}
						
					}.runTask(Main.getInstance());
					
				}
				
				
			} 
			catch (Exception e) {
				// So the loop doesn't break if an error occurs
				// Print the error, sleep, try again.
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		}
	}
	
	@AllArgsConstructor
	public static class ServerInput {
		String name;
		String ip;
		int port;
	}
	
}