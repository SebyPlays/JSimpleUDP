package com.github.sebyplays.jsimpleudp.event;

import com.github.sebyplays.jevent.Event;
import com.github.sebyplays.jevent.api.JEvent;

public class PacketReceivedEvent extends JEvent {
    public PacketReceivedEvent(Event event) {
        super(event);
    }
}
