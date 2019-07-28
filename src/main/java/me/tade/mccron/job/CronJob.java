package me.tade.mccron.job;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.tade.mccron.Cron;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author The_TadeSK
 */
@SuppressWarnings("WeakerAccess")
public class CronJob {

    private Cron cron;
    private List<String> cmds;
    private String time, name;
    private Calendar cal;
    private int t = 0;
    private int calDayMonth = 0;
    private int calDayWeek = Calendar.SUNDAY;
    private boolean useDayWeek = false;
    private String clockTime = "";
    private BukkitTask task;
    
    public CronJob(Cron cron, List<String> cmds, String time, String name){
        this.cron = cron;
        this.cmds = cmds;
        this.time = time;
        this.name = name;
    }
    
    public void startJob() throws IllegalArgumentException {
        getTimer();
        task = new BukkitRunnable(){
            @Override
            public void run(){
                t--;
                if(cal != null){
                    if(t <= 0){
                        getTimer();
                        if(!clockTime.isEmpty() && !isTime()){
                            return;
                        }
                        if(calDayMonth != 0 && cal.get(Calendar.DAY_OF_MONTH) == calDayMonth){
                            runCommands();
                        }else if(useDayWeek && cal.get(Calendar.DAY_OF_WEEK) == calDayWeek){
                            runCommands();
                        }
                    }
                    return;
                }
                if(!clockTime.isEmpty() && !isTime()){
                    return;
                }
                if(t <= 0){
                    runCommands();
                    getTimer();
                }
            }
        }.runTaskTimer(cron, 0, 20);
    }
    
    public void stopJob(){
        if(task != null) task.cancel();
    }
    
    private void getTimer() throws IllegalArgumentException {
        String[] args = time.split(" ");
        if(args.length >= 5)
        {
            if(args.length == 7)
            {
                if(args[5].contains("at"))
                {
                    clockTime = args[6];
                }
            }
            cal = Calendar.getInstance();

            if(args[2].contains("day") && args[4].contains("month"))
            {
                calDayMonth = Integer.parseInt(args[1]);
                // Tick Countdown set to 1 hour
                t = (!clockTime.isEmpty()) ? ((20 * 60) * 60 ) : 61;
            }
            else if(args[2].contains("day") && args[4].contains("week"))
            {
                useDayWeek = true;
                calDayWeek = Integer.parseInt(args[1]) + 1;
                // Tick Countdown set to 1 hour
                t = (!clockTime.isEmpty()) ? ((20 * 60) * 60 ) : 61;
            }
            else
            {
                throw new IllegalArgumentException("Invalid Time format: '" + time + "'");
            }
        }
        else if(args.length == 3)
        {
            // every x {time_unit}
            if(args[2].contains("second"))
            {
                t = Integer.parseInt(args[1]);
            }
            else if(args[2].contains("minute"))
            {
                t = Integer.parseInt(args[1]) * 60;
            }
            else if(args[2].contains("hour"))
            {
                t = (Integer.parseInt(args[1]) * 60) * 60;
            }
            else if(args[2].contains("day"))
            {
                t = ((Integer.parseInt(args[1]) * 60) * 60) * 24;
            }
            else
            {
                throw new IllegalArgumentException("Invalid Time format: '" + time + "'");
            }
        }
        else if(args.length == 2)
        {
            if(args[0].contains("at"))
            {
                clockTime = args[1];
                t = 61;
                return;
            }
            if(args[1].contains("second"))
            {
                t = 1;
            }
            else if(args[1].contains("minute"))
            {
                t = 60;
            }
            else if(args[1].contains("hour"))
            {
                t = 60 * 60;
            }
            else if(args[1].contains("day") || args[1].contains("dayS"))
            {
                t = (60 * 60) * 24;
            }
            else
            {
                throw new IllegalArgumentException("Invalid Time format: '" + time + "'");
            }
        }else{
            throw new IllegalArgumentException("Invalid Time format: '" + time + "'");
        }
    }
    
    public void runCommands(){
        for(String s : new ArrayList<>(cmds)){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
        }
    }
    
    public boolean isTime(){
        return clockTime.equalsIgnoreCase(cron.getCurrentTime(false));
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public List<String> getCommands() {
        return cmds;
    }
}
