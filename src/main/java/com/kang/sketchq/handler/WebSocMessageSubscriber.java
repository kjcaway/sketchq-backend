package com.kang.sketchq.handler;

import com.kang.sketchq.type.Drawing;
import javafx.event.Event;
import reactor.core.publisher.UnicastProcessor;

import java.util.Optional;

public class WebSocMessageSubscriber {
    private final UnicastProcessor<Drawing> eventPublisher;
    private Optional<Drawing> lastReceivedEvent = Optional.empty();

    public WebSocMessageSubscriber(UnicastProcessor<Drawing> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void onNext(Drawing event) {
        lastReceivedEvent = Optional.of(event);
        eventPublisher.onNext(event);
    }

    public void onError(Throwable error) {
        //TODO log error
        error.printStackTrace();
    }

    public void onComplete() {

        lastReceivedEvent.ifPresent(event -> eventPublisher.onNext(
                new Drawing("red", new int[]{1, 1}, new int[]{1, 1})));
    }
}
