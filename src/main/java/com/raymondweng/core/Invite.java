package com.raymondweng.core;

import java.util.HashMap;
import java.util.Map;

public class Invite {
    private static final Map<String, String> sentInvites = new HashMap<>(); //<inviter, invitee>
    private static final Map<String, Map<String, Invite>> invites = new HashMap<>(); //<invitee, <inviter, invite>>

    public final String inviter;
    public final String invitee;

    private Invite(String inviter, String invitee) {
        this.inviter = inviter;
        this.invitee = invitee;
    }

    public static Invite createInvite(String inviter, String invitee) {
        synchronized (sentInvites) {
            synchronized (invites) {
                if (sentInvites.containsKey(inviter)) {
                    return null;
                } else {
                    sentInvites.put(inviter, invitee);
                    Invite inv = new Invite(inviter, invitee);
                    if (!invites.containsKey(invitee)) {
                        invites.put(invitee, new HashMap<>());
                    }
                    invites.get(invitee).put(inviter, inv);
                    return inv;
                }
            }
        }
    }

    public static Invite getInviteByInviter(String inviter) {
        synchronized (sentInvites) {
            synchronized (invites) {
                if (sentInvites.containsKey(inviter)) {
                    return invites.get(sentInvites.get(inviter)).get(inviter);
                } else {
                    return null;
                }
            }
        }
    }

    public static Map<String, Invite> getInviteByInvitee(String invitee) {
        synchronized (invites) {
            return invites.get(invitee);
        }
    }

    public void remove() {
        synchronized (sentInvites) {
            synchronized (invites) {
                sentInvites.remove(inviter);
                if (invites.get(invitee).size() > 1) {
                    invites.get(invitee).remove(inviter);
                } else {
                    invites.remove(invitee);
                }
            }
        }
    }

    public Game accept() {
        invites.get(invitee).values().forEach(Invite::remove);
        boolean inviterRed = Math.random() < 0.5;
        return Game.startGame(inviterRed ? inviter : invitee, inviterRed ? invitee : inviter);
    }

}
