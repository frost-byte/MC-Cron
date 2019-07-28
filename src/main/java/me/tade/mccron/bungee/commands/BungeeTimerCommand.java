package me.tade.mccron.bungee.commands;

import me.tade.mccron.bungee.BungeeCron;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.concurrent.TimeUnit;

import static net.md_5.bungee.api.chat.TextComponent.fromLegacyText;

/**
 *
 * @author The_TadeSK
 */
public class BungeeTimerCommand extends Command {

    private BungeeCron cron;

    public BungeeTimerCommand(BungeeCron cron) {
        super("timer");
        this.cron = cron;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if(sender instanceof ProxiedPlayer)
        {
            sender.sendMessage(fromLegacyText("§cOnly console can perform this command!"));
            return;
        }
        if(args.length == 0)
        {
            sender.sendMessage(fromLegacyText("§aUse /timer <time> <command>"));
        }
        else if(args.length >= 2)
        {
            StringBuilder c = new StringBuilder();
            for(int i = 1; i < args.length; i++)
            {
                c.append(" ").append(args[i]);
            }

            c = new StringBuilder(c.substring(1));

            int time = Integer.valueOf(args[0]);
            if(time > 300)
            {
                sender.sendMessage(fromLegacyText("§cMaximum is 300 seconds (5 minutes)!"));
                return;
            }
            runCmd(c.toString(), time);
        }
    }
    
    private void runCmd(String cmd, int seconds)
    {
        ProxyServer proxy = ProxyServer.getInstance();
        proxy.getScheduler().schedule(
            cron,
            () -> proxy.getPluginManager().dispatchCommand(
                proxy.getConsole(), cmd
            ), seconds, TimeUnit.SECONDS
        );
    }
}
