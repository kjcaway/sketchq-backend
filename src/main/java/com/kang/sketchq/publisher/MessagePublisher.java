package com.kang.sketchq.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Component
public class MessagePublisher implements Consumer<FluxSink<String>> {
    private static final Logger log = LoggerFactory.getLogger(MessagePublisher.class);
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final Executor executor = Executors.newSingleThreadExecutor();

    public boolean push(String message) {
        return queue.offer(message);
    }

    @Override
    public void accept(FluxSink<String> sink) {
        this.executor.execute(() -> {
            while (true) {
                try {
                    sink.next(queue.take());
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        });
    }
}
