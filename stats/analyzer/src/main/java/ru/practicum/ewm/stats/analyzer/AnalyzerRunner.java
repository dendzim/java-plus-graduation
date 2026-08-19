package ru.practicum.ewm.stats.analyzer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.processor.EventSimilarityProcessor;
import ru.practicum.ewm.stats.analyzer.processor.UserActionProcessor;

@RequiredArgsConstructor
@Component
public class AnalyzerRunner implements CommandLineRunner {
    final EventSimilarityProcessor eventSimilarityProcessor;
    final UserActionProcessor userActionProcessor;

    @Override
    public void run(String... args) throws Exception {
        Thread EventSimilarityThread = new Thread(eventSimilarityProcessor);
        EventSimilarityThread.setName("EventSimilarityThread");
        EventSimilarityThread.start();

        userActionProcessor.start();
    }
}
