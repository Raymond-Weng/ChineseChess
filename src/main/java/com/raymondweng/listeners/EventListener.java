package com.raymondweng.listeners;


import com.raymondweng.Main;
import com.raymondweng.core.Game;
import com.raymondweng.core.Invite;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class EventListener implements net.dv8tion.jda.api.hooks.EventListener {
    public boolean registered(String id) throws SQLException {
        boolean registered = false;
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
        return registered;
    }

    public boolean playing(String id) throws SQLException {
        boolean playing = true;
        Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT GAME_PLAYING FROM PLAYER WHERE DISCORD_ID = " + id);
        resultSet.next();
        playing = resultSet.getString("GAME_PLAYING") != null;
        resultSet.close();
        statement.close();
        connection.close();
        return playing;
    }

    public int point(String id) throws SQLException {
        int point = 0;
        Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT POINT FROM PLAYER WHERE DISCORD_ID = " + id);
        resultSet.next();
        point = resultSet.getInt("POINT");
        resultSet.close();
        statement.close();
        connection.close();
        return point;
    }

    public boolean inServer(String id) throws SQLException {
        boolean inServer = false;
        Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT IN_SERVER FROM PLAYER WHERE DISCORD_ID = " + id);
        resultSet.next();
        inServer = resultSet.getBoolean("IN_SERVER");
        resultSet.close();
        statement.close();
        connection.close();
        return inServer;
    }

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        try {
            if (genericEvent instanceof MessageReceivedEvent && ((MessageReceivedEvent) genericEvent).isFromGuild() && !((MessageReceivedEvent) genericEvent).getMember().getId().equals(genericEvent.getJDA().getSelfUser().getId())) {
                Message message = ((MessageReceivedEvent) genericEvent).getMessage();
                switch (message.getContentRaw().split(" ")[0]) {
                    case "%shutdown":
                        if (message.getChannel().getId().equals("1272745478538264592")) {
                            message.reply("Bot is stopping").queue();
                            Main.main.stop();
                        }
                        break;
                    case "%leader-board":
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
                        break;
                    case "%help":
                        message.reply("請前往<#1272745478538264589>確認規則以及使用方式").queue();
                        break;
                    case "%register":
                        if (registered(message.getMember().getUser().getId())) {
                            message.reply("你已經註冊過了").queue();
                        } else {
                            Connection c = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
                            Statement statement = c.createStatement();
                            statement.executeUpdate("INSERT INTO PLAYER (DISCORD_ID, DATE_CREATED) " +
                                    "VALUES (" + message.getAuthor().getId() + ", DATE('now'))");
                            message.reply("註冊完成，祝你玩得愉快").queue();
                            statement.close();
                            c.close();
                        }
                        break;
                    case "%invite":
                        if (!registered(message.getMember().getId())) {
                            message.reply("請先註冊後再進行遊戲，如需更多資訊請使用`%help`指令").queue();
                        } else {
                            if (message.getContentRaw().split(" ").length >= 2 && message.getContentRaw().split(" ")[1].matches("<@\\d+>")) {
                                if (!registered(message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1))) {
                                    message.reply("你邀請的使用者還沒註冊，請先請他註冊後再邀請他").queue();
                                } else if (message.getMember().getId().equals(message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1))) {
                                    message.reply("不能邀請自己喔").queue();
                                } else {
                                    if (playing(message.getMember().getId())) {
                                        message.reply("你還在遊戲中，不能邀請人喔").queue();
                                    } else if (playing(message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1))) {
                                        message.reply("被邀請的人正在另一個對局，請等待對局結束後再邀請").queue();
                                    } else {
                                        Invite inv = Invite.createInvite(message.getMember().getId(), message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1));
                                        if (inv == null) {
                                            message.reply("你已經發送邀請了，請等待回復或是刪除上一個邀請")
                                                    .addEmbeds(new EmbedBuilder()
                                                            .addField("目前正在邀請...", "<@" + Invite.getInviteByInviter(message.getMember().getId()).invitee + ">", false)
                                                            .build())
                                                    .queue();
                                        } else {
                                            message.reply("邀請成功").queue();
                                            message.getChannel().sendMessage("<@" + inv.invitee + "> 您已收到來自 <@" + message.getMember().getId() + "> (" + point(message.getMember().getId()) + ")的邀請，請使用`%accept <邀請者>`接受挑戰，或是使用`%reject <邀請者>`來拒絕").queue();
                                        }
                                    }
                                }
                            } else {
                                message.reply("用法：`%invite <對手>`，在<對手>那邊請標註一個人").queue();
                            }
                        }
                        break;
                    case "%cancel":
                        if (Invite.getInviteByInviter(message.getMember().getId()) == null) {
                            message.reply("你目前沒有正在邀請的人").queue();
                        } else {
                            Invite.getInviteByInviter(message.getMember().getId()).remove();
                            message.reply("邀請已取消").queue();
                        }
                        break;
                    case "%invites":
                        if (Invite.getInviteByInvitee(message.getMember().getId()) == null) {
                            message.reply("目前你還沒有收到邀請").queue();
                            break;
                        }
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("以下是所有傳送給你的邀請");
                        Invite.getInviteByInvitee(message.getMember().getId()).keySet().forEach(key -> {
                            Main.main.jda.retrieveUserById(key).queue(member -> {
                                try {
                                    embedBuilder.addField(member.getName(), "(" + point(key) + ")", false);
                                } catch (SQLException ex) {
                                    err(ex, genericEvent);
                                }
                            });
                        });
                        message.replyEmbeds(embedBuilder.build()).queue();
                        break;
                    case "%accept":
                    case "%reject":
                        if (message.getContentRaw().split(" ").length < 2 || message.getContentRaw().split(" ")[1].matches("<\\d*>")) {
                            message.reply("用法：`" + message.getContentRaw().split(" ")[0] + " <邀請者>`，在<邀請者>那邊請標註一個人").queue();
                        } else if (Invite.getInviteByInvitee(message.getMember().getId()) != null
                                && Invite.getInviteByInvitee(message.getMember().getId()).containsKey(message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1))) {
                            if (message.getContentRaw().split(" ")[0].equals("%accept")) {
                                Game game = Invite.getInviteByInvitee(message.getMember().getId()).get(message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1)).accept();

                                message.reply(game.getMessage()).addFiles(FileUpload.fromData(game.toImage())).queue();
                            } else {
                                Invite.getInviteByInvitee(message.getMember().getId()).get(message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1)).remove();
                                message.reply("已拒絕邀請").queue();
                            }
                        } else {
                            message.reply("沒有找到那份邀請，可能是邀請已取消或是其中一方已經進入遊戲").queue();
                        }
                        break;
                    case "%data":
                        if (message.getContentRaw().split(" ").length >= 2 && message.getContentRaw().split(" ")[1].matches("<\\d*>")) {
                            message.reply("用法：`%data <使用者>`，在<使用者>那邊請標註一個人").queue();
                        } else if (registered(message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1))) {
                            if (inServer(message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1))) {
                                Main.main.jda.retrieveUserById(message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1)).queue(user -> {
                                    try {
                                        EmbedBuilder embed = new EmbedBuilder();
                                        embed.setTitle(user.getEffectiveName());
                                        embed.addField("分數", "" + point(message.getContentRaw().split(" ")[1].substring(2, message.getContentRaw().split(" ")[1].length() - 1)), false);
                                        embed.addField("狀態", inServer(user.getId()) ? (playing(user.getId()) ? "遊戲中" : "閒置中") : "已離開伺服器", false);
                                        Connection c = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
                                        Statement s = c.createStatement();
                                        ResultSet r = s.executeQuery("SELECT ID FROM GAME WHERE RED_PLAYER = " + user.getId() + " OR BLACK_PLAYER = " + user.getId() + " ORDER BY ID DESC LIMIT 1");
                                        if (r.next()) {
                                            embed.addField("最近遊戲（ID）", r.getString("ID"), false);
                                        } else {
                                            embed.addField("最近遊戲（ID）", "未曾進行遊戲", false);
                                        }
                                        c.close();
                                        s.close();
                                        r.close();
                                        message.replyEmbeds(embed.build()).queue();
                                    } catch (SQLException ex) {
                                        err(ex, genericEvent);
                                    }
                                });
                            } else {
                                message.reply("該使用者目前似乎不在本伺服器中...").queue();
                            }
                        } else {
                            message.reply("該使用者尚未註冊").queue();
                        }
                        break;
                    case "%move":
                        if (message.getContentRaw().split(" ").length < 2 || !Game.isLegalPosition(message.getContentRaw().split(" ")[1])) {
                            //TODO dealing with wrong input
                        } else {
                            //TODO dealing with move input
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
                        genericEvent.getJDA()
                                .getCategoryById("1270560414274687009")
                                .createVoiceChannel(((GuildVoiceUpdateEvent) genericEvent).getMember().getUser().getEffectiveName() + "的語音頻道")
                                .queue(channel -> {
                                    ((GuildVoiceUpdateEvent) genericEvent).getGuild().moveVoiceMember(((GuildVoiceUpdateEvent) genericEvent).getMember(), channel).queue();
                                });
                    }
                }
            }

            if (genericEvent instanceof GuildMemberJoinEvent) {
                if (registered(((GuildMemberJoinEvent) genericEvent).getMember().getId())) {
                    Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
                    Statement statement = connection.createStatement();
                    statement.executeUpdate("UPDATE PLAYER SET IN_SERVER = TRUE WHERE DISCORD_ID = " + ((GuildMemberJoinEvent) genericEvent).getMember().getId());
                    statement.close();
                    connection.close();
                }
            }

            if (genericEvent instanceof GuildMemberRemoveEvent) {
                if (registered(((GuildMemberRemoveEvent) genericEvent).getMember().getId())) {
                    Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
                    Statement statement = connection.createStatement();
                    statement.executeUpdate("UPDATE PLAYER SET IN_SERVER = FALSE WHERE DISCORD_ID = " + ((GuildMemberRemoveEvent) genericEvent).getMember().getId());
                    statement.close();
                    connection.close();
                }
            }
        } catch (Exception e) {
            err(e, genericEvent);
        }
    }

    public void err(Exception e, GenericEvent genericEvent) {
        if (genericEvent instanceof MessageReceivedEvent) {
            ((MessageReceivedEvent) genericEvent).getMessage().reply("處理的過程好像發生了錯誤...管理員會盡快處理，非常抱歉").queue();
        }
        genericEvent.getJDA().getTextChannelById("1284423527373738047")
                .sendMessage("# " + e.getClass().getSimpleName() + "\n" + e)
                .queue(message -> {
                    if (genericEvent instanceof MessageReceivedEvent) {
                        message.reply(((MessageReceivedEvent) genericEvent).getMessage().getJumpUrl()).queue();
                    }
                });
    }
}
