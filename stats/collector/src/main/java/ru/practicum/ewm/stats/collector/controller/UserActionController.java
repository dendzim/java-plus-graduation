package ru.practicum.ewm.stats.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.collector.service.UserActionService;
import ru.practicum.grpc.stats.action.UserActionControllerGrpc;
import ru.practicum.grpc.stats.action.UserActionProto;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionService userActionService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Сбор действий пользователя {}: действие={}", request.getUserId(), request.getActionType());
            userActionService.collectUserAction(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка выполнения запроса: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}
