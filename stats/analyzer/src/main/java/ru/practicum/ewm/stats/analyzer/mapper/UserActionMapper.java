package ru.practicum.ewm.stats.analyzer.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.model.ActionType;
import ru.practicum.ewm.stats.analyzer.model.UserAction;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
public class UserActionMapper {
    public UserAction toUserAction(UserActionAvro avro) {
        UserAction ua = new UserAction();
        ua.setUserId(avro.getUserId());
        ua.setEventId(avro.getEventId());
        ua.setActionType(toActionType(avro.getActionType()));
        ua.setTimestamp(avro.getTimestamp());
        return ua;
    }

    public ActionType toActionType(ActionTypeAvro avro) {
        return switch (avro) {
            case VIEW -> ActionType.VIEW;
            case LIKE -> ActionType.LIKE;
            case REGISTER -> ActionType.REGISTER;
        };
    }
}
