package me.tade.mccron;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import java.util.*;

import me.tade.mccron.commands.CronCommand;
import me.tade.mccron.commands.TimerCommand;
import me.tade.mccron.job.CronJob;
import me.tade.mccron.job.EventJob;
import me.tade.mccron.managers.EventManager;
import me.tade.mccron.utils.EventType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author The_TadeSK
 */
@SuppressWarnings("WeakerAccess")
public class Cron extends JavaPlugin {

    private HashMap<String, CronJob> jobs = new HashMap<>();
    private HashMap<EventType, List<EventJob>> eventJobs = new HashMap<>();
    private List<String> startUpCommands = new ArrayList<>();
    private Calendar cal;
    @Override
    public void onEnable(){
        log("Loading plugin...");
        log("Loading config...");
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        
        log("Loading commands...");
        getCommand("timer").setExecutor(new TimerCommand(this));
        getCommand("mccron").setExecutor(new CronCommand(this));
        
        loadJobs();

        log("Loading managers...");
        new EventManager(this);
        
        log("Loading metrics...");
        Metrics metrics = new Metrics(this);
        
        log("Loading custom charts for metrics...");
        metrics.addCustomChart(new Metrics.SingleLineChart("running_jobs") {
            @Override
            public int getValue() {
                return jobs.size();
            }
        });

        cal = Calendar.getInstance();
        log("The current calendar time is " + getCurrentTime());
        metrics.addCustomChart(new Metrics.SingleLineChart("running_event_jobs") {
            @Override
            public int getValue() {
                int size = 0;
                for(EventType type : EventType.values())
                    if(getEventJobs().containsKey(type))
                        size += getEventJobs().get(type).size();
                return size;
            }
        });

        metrics.addCustomChart(new Metrics.SingleLineChart("running_startup_commands") {
            @Override
            public int getValue() {
                return getStartUpCommands().size();
            }
        });
        log("Everything loaded!");

        new BukkitRunnable(){
            public void run() {
                log("Dispatching startup commands..");
                for(String commands : getStartUpCommands()){
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commands);
                }
                log("Commands dispatched!");
            }
        }.runTaskLater(this, 20);
    }

    public String getCurrentTime() {
        if (cal != null)
        {
            String hour, minute, second = "";

            hour = cal.get(Calendar.HOUR_OF_DAY) + "";
            minute = cal.get(Calendar.MINUTE) + "";
            second = cal.get(Calendar.SECOND) + "";

            return hour + ":" + minute + ":" + second;
        }

        return "Unknown!";
    }

    public void loadJobs(){
        log("Loading cron jobs....");
        ConfigurationSection cronJobsSection = getConfig().getConfigurationSection("jobs");

        if (cronJobsSection != null)
        {
            Set<String> keys = cronJobsSection.getKeys(false);

            if (keys.isEmpty()) {
                log("No Cron Jobs found!");
            }
            else
            {
                for(String s : keys)
                {
                    List<String> cmds = cronJobsSection.getStringList(s + ".commands");
                    String time = getConfig().getString("jobs." + s + ".time");

                    jobs.put(s, new CronJob(this, cmds, time, s));
                    log("Created new job: " + s);
                }
                log("Total loaded cron jobs: " + jobs.size());

                log("Starting cron jobs...");
                for(CronJob j : new ArrayList<>(jobs.values())){
                    try{
                        log("Starting job: " + j.getName());
                        j.startJob();
                    }catch(IllegalArgumentException ex){
                        log("Can't start job " + j.getName() + "! " + ex.getMessage());
                    }
                }
                log("All jobs started!");
            }
        }

        ConfigurationSection eventJobsSection = getConfig().getConfigurationSection("event-jobs");

        if (eventJobsSection != null)
        {
            Set<String> eventNames = eventJobsSection.getKeys(false);

            if (eventNames.isEmpty())
            {
                log("No Event Jobs found!");
            }
            else
            {
                for (String eventName : eventNames)
                {
                    EventType type = EventType.isEventJob(eventName);
                    if(type != null)
                    {
                        ConfigurationSection eventSection = eventJobsSection.getConfigurationSection(eventName);

                        if (eventSection != null)
                        {
                            Set<String> jobNames = eventSection.getKeys(false);
                            List<EventJob> jobs = new ArrayList<>();

                            for(String jobName : jobNames)
                            {
                                int time = eventSection.getInt(jobName + ".time");
                                List<String> cmds = eventSection.getStringList(jobName + ".commands");
                                jobs.add(new EventJob(this, jobName, time, cmds, type));
                                log("Created new event job: " + jobName + " (" + type.getConfigName() + ")");
                            }

                            eventJobs.put(type, jobs);
                        }
                    }
                }
                log("All Event Jobs registered!");
            }
        }

        List<String> cmds = getConfig().getStringList("startup.commands");
        if(!cmds.isEmpty()) {
            for (String command : cmds) {
                startUpCommands.add(command);
                log("Created new startup command: " + command);
            }
        }
        log("All Startup Commands registered!");
    }

    @Override
    public void onDisable(){
    }
    
    public void log(String info){
        getLogger().info(info);
        logCustom(info);
    }
    
    private void logCustom(String info){
        try {
            File dataFolder = getDataFolder();
            if(!dataFolder.exists()){
                dataFolder.mkdir();
            }
            
            File saveTo = new File(getDataFolder(), "log.txt");
            if (!saveTo.exists()){
                saveTo.createNewFile();
            }
            
            FileWriter fw = new FileWriter(saveTo, true);
            PrintWriter pw = new PrintWriter(fw);
            
            pw.println("[" + new SimpleDateFormat("dd.MM.YYYY HH:mm:ss").format(new Date()) + "] "+ info);
            pw.flush();
            pw.close();
            
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public HashMap<String, CronJob> getJobs() {
        return jobs;
    }

    public HashMap<EventType, List<EventJob>> getEventJobs() {
        return eventJobs;
    }

    public List<String> getStartUpCommands() {
        return startUpCommands;
    }
}
