package com.raymondweng.listeners;


import com.raymondweng.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class EventListener implements net.dv8tion.jda.api.hooks.EventListener {
    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if (genericEvent instanceof MessageReceivedEvent && !Objects.requireNonNull(((MessageReceivedEvent) genericEvent).getMember()).getId().equals(genericEvent.getJDA().getSelfUser().getId())) {
            Message message = ((MessageReceivedEvent) genericEvent).getMessage();
            switch (message.getContentRaw().split(" ")[0]) {
                case "%findUser":
                    if (message.getChannel().getId().equals("1272745478538264592")) {
                        message.reply(message.getJDA().getUserById(Long.valueOf(message.getContentRaw().split(" ")[1])).getName()).queue();
                    }
                    break;
                case "%leader-board":
                    try {
                        synchronized (Main.main.connection) {
                            Statement stmt = Main.main.connection.createStatement();
                            ResultSet rs = stmt.executeQuery("SELECT DISCORD_ID, POINT FROM PLAYER WHERE POINT >= 1200 ORDER BY POINT DESC, DATE_CREATED ACS LIMIT 10");
                            EmbedBuilder e = new EmbedBuilder();
                            e.setTitle("Leaderboard");
                            int cnt = 0;
                            while (rs.next()) {
                                cnt++;
                                e.addField("第" + cnt + "名", "<@" + rs.getString("DISCORD_ID") + "> (" + rs.getString("POINT") + "分)\n", false);
                            }
                            while (cnt < 10) {
                                cnt++;
                                e.addField("第" + cnt + "名", "空缺", false);
                            }
                            e.setFooter("\n註：只有分數大於1200的人可以進榜，如果還沒進榜請繼續努力");
                            message.replyEmbeds(e.build()).queue();
                            rs.close();
                            stmt.close();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "%help":
                    message.reply("請前往<#1272745478538264589>確認規則以及使用方式").queue();
                    break;
                case "%register":
                    synchronized (Main.main.connection) {
                        try {
                            Statement stmt = Main.main.connection.createStatement();
                            ResultSet rs = stmt.executeQuery("SELECT DISCORD_ID FROM PLAYER WHERE DISCORD_ID = " + message.getAuthor().getId());
                            if (rs.next()) {
                                message.reply("你已經註冊過了").queue();
                            } else {
                                stmt.executeUpdate("INSERT INTO PLAYER (DISCORD_ID, DATE_CREATED) " +
                                        "VALUES (" + message.getAuthor().getId() + ", DATE('now'))");
                                message.reply("註冊完成，祝你玩得愉快").queue();
                            }
                            rs.close();
                            stmt.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
            }

        }
    }
}
