package me.tade.mccron.bungee.managers;

import me.tade.mccron.bungee.BungeeCron;
import me.tade.mccron.bungee.job.BungeeEventJob;
import me.tade.mccron.utils.EventType;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class EventManager implements Listener {

    private BungeeCron cron;

    public EventManager(BungeeCron cron) {
        this.cron = cron;

        ProxyServer.getInstance().getPluginManager().registerListener(this.cron, this);
    }

    @EventHandler
    public void onJoinEvent(net.md_5.bungee.api.event.ServerConnectEvent event){
        ProxiedPlayer player = event.getPlayer();

        HashMap<EventType, List<BungeeEventJob>> eventJobs = cron.getEventJobs();

        if (eventJobs.isEmpty())
            return;

        if(!cron.getEventJobs().containsKey(EventType.JOIN_EVENT))
            return;

        for(BungeeEventJob job : cron.getEventJobs().get(EventType.JOIN_EVENT))
            job.performJob(player);
    }

    @EventHandler
    public void onQuitEvent(net.md_5.bungee.api.event.ServerDisconnectEvent event){
        ProxiedPlayer player = event.getPlayer();

        if(!cron.getEventJobs().containsKey(EventType.QUIT_EVENT))
            return;

        for(BungeeEventJob job : cron.getEventJobs().get(EventType.QUIT_EVENT))
            job.performJob(player);
    }
}
