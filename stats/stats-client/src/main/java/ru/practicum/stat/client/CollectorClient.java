package ru.practicum.stat.client;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.action.UserActionControllerGrpc;
import ru.practicum.grpc.stats.action.UserActionProto;

@Slf4j
@Component
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub userActionClient;

    public void sendUserAction(UserActionProto userAction) {
        try {
            userActionClient.collectUserAction(userAction);
        } catch (StatusRuntimeException e) {
            log.error("Ошибка вовремя отправки в сервис Collector: {}", e.getStatus().getDescription());
        }
    }
}
