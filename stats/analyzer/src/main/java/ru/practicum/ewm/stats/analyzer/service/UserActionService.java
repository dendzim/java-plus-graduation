package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.analyzer.mapper.UserActionMapper;
import ru.practicum.ewm.stats.analyzer.model.ActionType;
import ru.practicum.ewm.stats.analyzer.model.UserAction;
import ru.practicum.ewm.stats.analyzer.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserActionService {

    private final UserActionRepository userActionRepository;
    private final UserActionMapper userActionMapper;


    public void saveUserAction(List<UserActionAvro> userActionAvroList) {
        for (UserActionAvro avro : userActionAvroList) {
            userActionRepository.findByUserIdAndEventId(avro.getUserId(), avro.getEventId())
                    .ifPresentOrElse(
                            userAction -> updateAction(userAction, avro),
                            () -> userActionRepository.save(userActionMapper.toUserAction(avro))
                    );
        }
    }

    private void updateAction(UserAction userAction, UserActionAvro avro) {
        double oldWeight = getWeight(userAction.getActionType());
        double newWeight = getWeight(avro.getActionType());
        if (newWeight <= oldWeight) return;

        userAction.setActionType(userActionMapper.toActionType(avro.getActionType()));
        userAction.setTimestamp(avro.getTimestamp());
        userActionRepository.save(userAction);
    }

    public List<UserAction> findByUserIdOrderByTimestamp(Long userId) {
        return userActionRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public Double calculateMaxRatingPerUserByEventId(Long eventId) {
        return userActionRepository.findAllByEventId(eventId).stream()
                .mapToDouble(userAction -> getWeight(userAction.getActionType()))
                .sum();
    }

    public double getWeight(ActionType actionType) {
        return switch (actionType) {
            case ActionType.VIEW -> 0.4;
            case ActionType.REGISTER -> 0.8;
            case ActionType.LIKE -> 1.0;
        };
    }

    public double getWeight(ActionTypeAvro actionTypeAvro) {
        return switch (actionTypeAvro) {
            case ActionTypeAvro.VIEW -> 0.4;
            case ActionTypeAvro.REGISTER -> 0.8;
            case ActionTypeAvro.LIKE -> 1.0;
        };
    }
}
