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
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ClassSidebarPlugin extends JavaPlugin implements Listener {
    private static final String[] ANIM_COLORS = {"&b", "&3", "&9", "&d", "&5"};

    private BukkitTask updateTask;
    private int animationTick;

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
            animationTick++;
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
        hideNumbers(objective);

        int mainLevel = parseLevel(placeholder(player, "%classlevel_main_level%"));
        int combatLevel = parseLevel(placeholder(player, "%classlevel_combat_level%"));

        List<String> lines = new ArrayList<>();
        lines.add("&8&m--------------");
        lines.add("&7Ник: &f" + player.getName());
        lines.add("&7Осн: &b" + cleanPlaceholder(placeholder(player, "%classlevel_main_class%")));
        lines.add("&7Ур: " + styleLevel(mainLevel, animationTick));
        lines.add("&7Бой: &d" + cleanPlaceholder(placeholder(player, "%classlevel_combat_class%")));
        lines.add("&7Ур: " + styleLevel(combatLevel, animationTick + 2));
        lines.add("&8&m--------------");

        int score = lines.size();
        for (String line : lines) {
            String unique = colorize(trim(line, 32)) + ChatColor.values()[score % ChatColor.values().length];
            Score boardScore = objective.getScore(unique);
            boardScore.setScore(score);
            hideNumbers(boardScore);
            score--;
        }

        player.setScoreboard(scoreboard);
    }

    private void hideNumbers(Objective objective) {
        try {
            Object blankFormat = getBlankNumberFormat();
            if (blankFormat == null) {
                return;
            }

            for (Method method : objective.getClass().getMethods()) {
                String name = method.getName().toLowerCase(Locale.ROOT);
                if ((name.equals("numberformat") || name.equals("setnumberformat"))
                        && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isInstance(blankFormat)) {
                    method.invoke(objective, blankFormat);
                    return;
                }
            }
        } catch (Throwable ignored) {
            // Старые версии API могут не поддерживать скрытие чисел.
        }
    }

    private void hideNumbers(Score score) {
        try {
            Object blankFormat = getBlankNumberFormat();
            if (blankFormat == null) {
                return;
            }

            for (Method method : score.getClass().getMethods()) {
                String name = method.getName().toLowerCase(Locale.ROOT);
                if ((name.equals("numberformat") || name.equals("setnumberformat"))
                        && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isInstance(blankFormat)) {
                    method.invoke(score, blankFormat);
                    return;
                }
            }
        } catch (Throwable ignored) {
            // В API без score-level number format просто продолжаем без падения.
        }
    }

    private Object getBlankNumberFormat() {
        try {
            Class<?> formatClass = Class.forName("io.papermc.paper.scoreboard.numbers.NumberFormat");
            Method blank = formatClass.getMethod("blank");
            return blank.invoke(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private String placeholder(Player player, String placeholder) {
        return PlaceholderAPI.setPlaceholders(player, placeholder);
    }

    private int parseLevel(String raw) {
        try {
            return Integer.parseInt(cleanPlaceholder(raw).replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String styleLevel(int level, int phase) {
        if (level >= 10) {
            return animatedGradient(String.valueOf(level), phase);
        }
        return "&f" + level;
    }

    private String animatedGradient(String text, int phase) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            int colorIndex = Math.floorMod(phase + i, ANIM_COLORS.length);
            out.append(ANIM_COLORS[colorIndex]).append(text.charAt(i));
        }
        return out.toString();
    }

    private String cleanPlaceholder(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        if (value.startsWith("%") && value.endsWith("%")) {
            return "-";
        }
        return value;
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String trim(String text, int max) {
        return text.length() > max ? text.substring(0, max) : text;
    }
}
