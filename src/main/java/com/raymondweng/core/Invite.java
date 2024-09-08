package com.raymondweng.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Invite {
    private static final Map<String, String> sentInvites = new HashMap<>(); //<inviter, invitee>
    private static final Map<String, Map<String, Invite>> invites = new HashMap<>(); //<invitee, <inviter, invite>>

    private boolean alive = true;
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
                return invites.get(sentInvites.get(inviter)).get(inviter);
            }
        }
    }

    public static Collection<Invite> getInviteByInvitee(String invitee) {
        synchronized (invites) {
            return invites.get(invitee).values();
        }
    }

    public void remove() {
        synchronized (sentInvites) {
            synchronized (invites) {
                sentInvites.remove(inviter);
                if (invites.get(invitee).size() > 1) {
                    invites.get(invitee).remove(this);
                }
                alive = false;
            }
        }
    }

    public boolean isAlive() {
        return alive;
    }

}
