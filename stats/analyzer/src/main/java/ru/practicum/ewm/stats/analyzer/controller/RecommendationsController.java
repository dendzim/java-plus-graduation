package ru.practicum.ewm.stats.analyzer.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.analyzer.service.RecommendationService;
import ru.practicum.grpc.stats.action.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.action.RecommendedEventProto;
import ru.practicum.grpc.stats.action.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.action.UserPredictionsRequestProto;
import ru.practicum.grpc.stats.service.dashboard.RecommendationsControllerGrpc;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto requestProto,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            recommendationService.getRecommendationsForUser(requestProto)
                    .forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при получении рекомендаций", e);
            responseObserver.onError(e);
        }

    }

    public void getSimilarEvents(SimilarEventsRequestProto similarEventsRequestProto,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            recommendationService.getSimilarEvents(similarEventsRequestProto)
                    .forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("ошибка при получении схожих ивентов", e);
            responseObserver.onError(e);
        }
    }

    public void getInteractionsCount(InteractionsCountRequestProto interactionsCountRequestProto,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            recommendationService.getInteractionsCount(interactionsCountRequestProto)
                    .forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка получения идентификаторов мероприятия", e);
            responseObserver.onError(e);
        }
    }
}
