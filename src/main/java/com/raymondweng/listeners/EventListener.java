package com.raymondweng.listeners;


import com.raymondweng.Main;
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
                    StringBuilder m = new StringBuilder();
                    try {
                        Statement stmt = Main.main.connection.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT * FROM PLAYER WHERE POINT >= 1500 ORDER BY POINT DESC LIMIT 10");
                        while (rs.next()) {
                            m.append(message.getJDA().getUserById(rs.getLong("DISCORD_ID"))).append("\n");
                        }
                        while (m.toString().split("\n").length < 10){
                            m.append("空缺\n");
                        }
                        if(m.toString().split("\n")[9].equals("空缺")){
                            m.append("\n註：只有分數大於1500的人可以進榜，如果還沒進榜請繼續努力");
                        }
                        message.reply(m.toString()).queue();
                        stmt.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "%help":
                    message.reply("請前往<#1272745478538264589>確認規則以及使用方式").queue();
                    break;
            }

        }
    }
}
