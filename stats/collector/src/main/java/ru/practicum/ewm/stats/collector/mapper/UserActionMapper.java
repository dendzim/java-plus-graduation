package ru.practicum.ewm.stats.collector.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;

import java.time.Instant;

@Component
public class UserActionMapper {
    public static UserActionAvro toAvro(UserActionProto proto) {
        if (proto == null) {
            throw new IllegalArgumentException("UserActionProto не может быть null");
        }
        UserActionAvro.Builder builder = UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(toEnumAvro(proto.getActionType()))
                .setTimestamp(Instant.ofEpochSecond(
                        proto.getTimestamp().getSeconds(),
                        proto.getTimestamp().getNanos()));
        return builder.build();
    }

    private static ActionTypeAvro toEnumAvro(ActionTypeProto prot) {
        return switch (prot) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Неизвестное значение: " + prot);
        };
    }
}
