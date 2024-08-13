package com.raymondweng.listeners;


import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public class EventListener implements net.dv8tion.jda.api.hooks.EventListener {
    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if(genericEvent instanceof MessageReceivedEvent && !Objects.requireNonNull(((MessageReceivedEvent) genericEvent).getMember()).getId().equals(genericEvent.getJDA().getSelfUser().getId())) {
            Message message = ((MessageReceivedEvent) genericEvent).getMessage();
            message.reply("hi").addFiles(FileUpload.fromData(new File("./maps/logo.jpg"))).queue();

        }
    }
}
