package me.tade.mccron.managers;

import me.tade.mccron.Cron;
import me.tade.mccron.job.EventJob;
import me.tade.mccron.utils.EventType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class EventManager implements Listener {

    private Cron cron;

    public EventManager(Cron cron) {
        this.cron = cron;

        Bukkit.getPluginManager().registerEvents(this, cron);
    }

    @EventHandler
    public void onJoinEvent(PlayerJoinEvent event){
        Player player = event.getPlayer();

        HashMap<EventType, List<EventJob>> eventJobs = cron.getEventJobs();

        if (eventJobs.isEmpty())
            return;

        Date now = new Date();
        // Number of seconds since the player first joined.
        long diff = (now.getTime() - player.getFirstPlayed()) / 1000;

        if (diff < 120 && eventJobs.containsKey(EventType.FIRST_JOIN_EVENT)) {
            for (EventJob job : eventJobs.get(EventType.FIRST_JOIN_EVENT)) {
                job.performJob(player);
            }
        }

        if(eventJobs.containsKey(EventType.JOIN_EVENT)) {
            for(EventJob job : eventJobs.get(EventType.JOIN_EVENT)) {
                job.performJob(player);
            }
        }
    }

    @EventHandler
    public void onQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (!cron.getEventJobs().containsKey(EventType.QUIT_EVENT))
            return;

        for (EventJob job : cron.getEventJobs().get(EventType.QUIT_EVENT))
            job.performJob(player);
    }
}
