package dev.codex.classsidebar;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public final class ClassSidebarPlugin extends JavaPlugin implements Listener {
    private BukkitTask updateTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().severe("PlaceholderAPI не найден. Плагин будет отключён.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            applySidebar(player);
        }

        long period = Math.max(10L, getConfig().getLong("update-ticks", 20L));
        updateTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                applySidebar(player);
            }
        }, period, period);

        getLogger().info("ClassSidebar включён.");
    }

    @Override
    public void onDisable() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        applySidebar(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private void applySidebar(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("class_sidebar", Criteria.DUMMY, colorize(getConfig().getString("title", "&b✦ Профиль ✦")));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> configuredLines = getConfig().getStringList("lines");
        List<String> lines = configuredLines.isEmpty() ? defaultLines() : configuredLines;

        int score = lines.size();
        for (String line : lines) {
            String parsed = PlaceholderAPI.setPlaceholders(player, line);
            String colored = trim(colorize(parsed), 40);
            objective.getScore(colored + ChatColor.values()[score % ChatColor.values().length]).setScore(score);
            score--;
        }

        player.setScoreboard(scoreboard);
    }

    private List<String> defaultLines() {
        List<String> lines = new ArrayList<>();
        lines.add("&8&l• &7Ник: &f%player_name%");
        lines.add("&8&l• &7Осн. класс: &f%classlevel_main_class%");
        lines.add("&8  &7Ключ: &f%classlevel_main_class_key%");
        lines.add("&8  &7Уровень: &f%classlevel_main_level%");
        lines.add("&8&l• &7Боев. класс: &f%classlevel_combat_class%");
        lines.add("&8  &7Ключ: &f%classlevel_combat_class_key%");
        lines.add("&8  &7Уровень: &f%classlevel_combat_level%");
        return lines;
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String trim(String text, int max) {
        return text.length() > max ? text.substring(0, max) : text;
    }
}
