package com.tyche.ramesses.adapter.in;

import com.tyche.ramesses.port.in.MarketCheckPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulerAdapter {

    private final MarketCheckPort marketCheckPort;

    @Scheduled(fixedDelayString = "${application.scheduler.frequency}")
    public void runJob() {
        marketCheckPort.checkMarket();
    }
}
