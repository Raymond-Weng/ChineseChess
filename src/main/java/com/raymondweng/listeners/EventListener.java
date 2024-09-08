package com.raymondweng.listeners;


import com.raymondweng.Main;
import com.raymondweng.core.Invite;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EventListener implements net.dv8tion.jda.api.hooks.EventListener {
    public boolean registered(String id) {
        boolean registered = false;
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS CNT FROM PLAYER WHERE DISCORD_ID = " + id);
            rs.next();
            if (rs.getInt("CNT") > 0) {
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
                        Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
                        Statement stmt = connection.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT DISCORD_ID, POINT FROM PLAYER WHERE POINT >= 1200 ORDER BY POINT DESC, DATE_CREATED ASC LIMIT 10");
                        EmbedBuilder e = new EmbedBuilder();
                        e.setTitle("Leaderboard");
                        int cnt = 0;
                        while (rs.next()) {
                            cnt++;
                            e.addField("第" + cnt + "名", "<@" + rs.getString("DISCORD_ID") + "> (" + rs.getString("POINT") + ")\n", false);
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
                            Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
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
                case "%invite":
                    if (!registered(message.getMember().getUser().getId())) {
                        message.reply("請先註冊後再進行遊戲，如需更多資訊請使用`%help`指令").queue();
                    } else {
                        if (message.getContentRaw().split(" ").length >= 2 && !message.getContentRaw().split(" ")[1].matches("<\\d*>")) {
                            if (!registered(message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1))) {
                                message.reply("你邀請的使用者還沒註冊，請先請他註冊後再邀請他").queue();
                            } else {
                                Invite inv = Invite.createInvite(message.getMember().getId(), message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1));
                                if (inv == null) {
                                    message.reply("你已經發送邀請了，請等待回復或是刪除上一個邀請")
                                            .addEmbeds(new EmbedBuilder()
                                                    .addField("目前正在邀請...", "<@" + Invite.getInviteByInviter(message.getMember().getId()).invitee + ">", true)
                                                    .build())
                                            .queue();
                                } else {
                                    message.reply("邀請成功").queue();
                                    message.getChannel().sendMessage("<@" + inv.invitee + "> 您已收到來自 <@" + message.getMember().getId() + "> 的邀請，請使用`%accept <邀請者>`接受挑戰").queue();
                                }
                            }
                        } else {
                            message.reply("用法：`%invite <對手>`，在<對手>那邊請標註一個人").queue();
                        }
                    }
                    break;
            }

        }

        if (genericEvent instanceof GuildVoiceUpdateEvent) {
            if (((GuildVoiceUpdateEvent) genericEvent).getChannelLeft() != null) {
                List<String> deleteProtect = Arrays.asList("1270560414719279236", "1279362956848529442", "1279333695265833001");
                if (!deleteProtect.contains(((GuildVoiceUpdateEvent) genericEvent).getChannelLeft().getId()) && ((GuildVoiceUpdateEvent) genericEvent).getChannelLeft().getMembers().isEmpty()) {
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
    }
}
