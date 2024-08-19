package com.raymondweng.listeners;


import com.raymondweng.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Objects;

public class EventListener implements net.dv8tion.jda.api.hooks.EventListener {
    public boolean registered(String id) {
        boolean registered = false;
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/player.db");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISCORD_ID FROM PLAYER WHERE DISCORD_ID = " + id);
            if (rs.next()) {
                registered = true;
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return registered;
    }

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if (genericEvent instanceof GuildVoiceUpdateEvent) {
            if (((GuildVoiceUpdateEvent) genericEvent).getChannelLeft() != null) {
                if (!((GuildVoiceUpdateEvent) genericEvent).getChannelLeft().getId().equals("1270560414719279236") && ((GuildVoiceUpdateEvent) genericEvent).getChannelLeft().getMembers().isEmpty()) {
                    ((GuildVoiceUpdateEvent) genericEvent).getChannelLeft().delete().queue();
                }
            }
            if (((GuildVoiceUpdateEvent) genericEvent).getChannelJoined() != null) {
                if (((GuildVoiceUpdateEvent) genericEvent).getChannelJoined().getId().equals("1270560414719279236")) {
                    Objects.requireNonNull(genericEvent.getJDA()
                                    .getCategoryById("1270560414274687009"))
                            .createVoiceChannel(((GuildVoiceUpdateEvent) genericEvent).getMember().getUser().getEffectiveName() + "的語音頻道")
                            .queue(channel -> {
                                ((GuildVoiceUpdateEvent) genericEvent).getGuild().moveVoiceMember(((GuildVoiceUpdateEvent) genericEvent).getMember(), channel).queue();
                            });
                }
            }
        }
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
                        Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/player.db");
                        Statement stmt = connection.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT DISCORD_ID, POINT FROM PLAYER WHERE POINT >= 1200 ORDER BY POINT DESC, DATE_CREATED ASC LIMIT 10");
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
                        connection.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "%help":
                    message.reply("請前往<#1272745478538264589>確認規則以及使用方式").queue();
                    break;
                case "%register":
                    if (registered(Objects.requireNonNull(message.getMember()).getUser().getId())) {
                        message.reply("你已經註冊過了").queue();
                    } else {
                        try {
                            Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/player.db");
                            Statement stmt = connection.createStatement();
                            stmt.executeUpdate("INSERT INTO PLAYER (DISCORD_ID, DATE_CREATED) " +
                                    "VALUES (" + message.getAuthor().getId() + ", DATE('now'))");
                            message.reply("註冊完成，祝你玩得愉快").queue();
                            stmt.close();
                            connection.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
            }

        }
    }
}
