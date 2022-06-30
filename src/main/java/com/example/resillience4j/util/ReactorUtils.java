package com.example.resillience4j.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class ReactorUtils {
    public static <T> Mono<List<T>> listErrorResume(String name, Throwable t) {
        log.error(name, t);
        return Mono.just(Collections.emptyList());
    }

    public static <T> Mono<T> objectErrorResume(String name, Throwable t, T fallback) {
        log.error(name, t);
        return Mono.just(fallback);
    }

    public static <K, V> Mono<Map<K, V>> mapErrorResume(String name, Throwable t, Map<K, V> fallback) {
        log.error(name, t);
        return Mono.just(fallback);
    }

    public static <T> Mono<T> objectWithoutErrorHandle(Supplier<T> supplier,
                                                       ThreadPoolTaskExecutor executor) {
        return Mono.defer(execute(supplier))
                   .subscribeOn(Schedulers.fromExecutor(executor));
    }

    public static <T> Mono<T> object(String name,
                                     Supplier<T> supplier,
                                     T fallback,
                                     ThreadPoolTaskExecutor executor) {
        return Mono.defer(execute(supplier))
                   .subscribeOn(Schedulers.fromExecutor(executor))
                   .onErrorResume(t -> objectErrorResume(name, t, fallback));
    }

    public static <T> Mono<T> object(String name,
                                     Supplier<T> supplier,
                                     T fallback,
                                     ThreadPoolTaskExecutor executor,
                                     Duration timeout) {
        return Mono.defer(execute(supplier))
                   .subscribeOn(Schedulers.fromExecutor(executor))
                   .timeout(timeout, Mono.just(fallback))
                   .onErrorResume(t -> objectErrorResume(name, t, fallback));
    }

    public static <T> Mono<T> object(String name,
                                     Supplier<T> supplier, ThreadPoolTaskExecutor executor) {
        return Mono.defer(execute(supplier))
                   .subscribeOn(Schedulers.fromExecutor(executor))
                   .doOnError(e -> log.error(name, e));
    }

    public static <T> Mono<T> object(String name,
                                     Supplier<T> supplier, ThreadPoolTaskExecutor executor, Duration timeout) {
        return Mono.defer(execute(supplier))
                   .subscribeOn(Schedulers.fromExecutor(executor))
                   .timeout(timeout)
                   .doOnError(e -> log.error(name, e));
    }

    public static <T> Supplier<Mono<T>> execute(Supplier<T> supplier) {
        return () -> {
            try {
                return Mono.just(supplier.get());
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
    }

    public static <T> Mono<List<T>> list(String name,
                                         Supplier<List<T>> supplier,
                                         ThreadPoolTaskExecutor executor) {
        return Mono.defer(executeList(supplier))
                   .subscribeOn(Schedulers.fromExecutor(executor))
                   .onErrorResume(t -> ReactorUtils.listErrorResume(name, t));
    }

    public static <T, K, V> Mono<Map<K, V>> map(String name,
                                                Supplier<List<T>> supplier,
                                                Function<T, K> keyExtractor,
                                                Function<T, V> valueExtractor,
                                                Map<K, V> fallback,
                                                ThreadPoolTaskExecutor executor) {
        return Mono.defer(executeMap(supplier, keyExtractor, valueExtractor))
                   .onErrorResume(t -> ReactorUtils.mapErrorResume(name, t, fallback))
                   .subscribeOn(Schedulers.fromExecutor(executor))
                   .doOnError(e -> log.error(name, e));
    }

    public static <T, K> Mono<Map<K, List<T>>> groupBy(String name,
                                                       Supplier<List<T>> supplier,
                                                       Function<T, K> classifier,
                                                       Map<K, List<T>> fallback,
                                                       ThreadPoolTaskExecutor executor) {
        return Mono.defer(executeGroupBy(supplier, classifier))
                   .onErrorResume(t -> ReactorUtils.mapErrorResume(name, t, fallback))
                   .subscribeOn(Schedulers.fromExecutor(executor))
                   .doOnError(e -> log.error(name, e));
    }

    public static <T> Mono<List<T>> list(String name,
                                         Supplier<List<T>> supplier,
                                         ThreadPoolTaskExecutor executor,
                                         Duration timeout) {
        return Mono.defer(executeList(supplier))
                   .timeout(timeout, Mono.just(Collections.emptyList()))
                   .onErrorResume(t -> ReactorUtils.listErrorResume(name, t))
                   .subscribeOn(Schedulers.fromExecutor(executor));
    }

    public static <T> Supplier<Mono<List<T>>> executeList(Supplier<List<T>> supplier) {
        return () -> {
            try {
                return Mono.just(supplier.get());
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
    }

    public static <T, K, V> Supplier<Mono<Map<K, V>>> executeMap(Supplier<List<T>> supplier,
                                                                 Function<T, K> keyExtractor,
                                                                 Function<T, V> valueExtractor) {
        return () -> {
            try {
                return Mono.just(supplier.get().stream().collect(Collectors.toMap(keyExtractor, valueExtractor)));
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
    }

    public static <T, K> Supplier<Mono<Map<K, List<T>>>> executeGroupBy(Supplier<List<T>> supplier,
                                                                        Function<T, K> classifier) {
        return () -> {
            try {
                return Mono.just(supplier.get().stream().collect(Collectors.groupingBy(classifier)));
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Mono<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> zip(
            Mono<? extends T1> p1,
            Mono<? extends T2> p2,
            Mono<? extends T3> p3,
            Mono<? extends T4> p4,
            Mono<? extends T5> p5,
            Mono<? extends T6> p6,
            Mono<? extends T7> p7,
            Mono<? extends T8> p8,
            Mono<? extends T9> p9) {
        //noinspection unchecked
        return Mono.zip(objects -> new Tuple9<>((T1) objects[0], (T2) objects[1], (T3) objects[2], (T4) objects[3], (T5) objects[4], (T6) objects[5], (T7) objects[6], (T8) objects[7], (T9) objects[8]),
                        p1,
                        p2,
                        p3,
                        p4,
                        p5,
                        p6,
                        p7,
                        p8,
                        p9);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Function<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>, R> function(
            Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> function) {
        return tuple -> function.apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6(), tuple.getT7(), tuple.getT8(), tuple.getT9());
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Mono<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>> zip(
            Mono<? extends T1> p1,
            Mono<? extends T2> p2,
            Mono<? extends T3> p3,
            Mono<? extends T4> p4,
            Mono<? extends T5> p5,
            Mono<? extends T6> p6,
            Mono<? extends T7> p7,
            Mono<? extends T8> p8,
            Mono<? extends T9> p9,
            Mono<? extends T10> p10) {
        //noinspection unchecked
        return Mono.zip(objects -> new Tuple10<>((T1) objects[0], (T2) objects[1], (T3) objects[2], (T4) objects[3], (T5) objects[4], (T6) objects[5], (T7) objects[6], (T8) objects[7], (T9) objects[8], (T10) objects[9]),
                        p1,
                        p2,
                        p3,
                        p4,
                        p5,
                        p6,
                        p7,
                        p8,
                        p9,
                        p10);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> Function<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>, R> function(
            Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> function) {
        return tuple -> function.apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6(), tuple.getT7(), tuple.getT8(), tuple.getT9(), tuple.getT10());
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Mono<Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> zip(
            Mono<? extends T1> p1,
            Mono<? extends T2> p2,
            Mono<? extends T3> p3,
            Mono<? extends T4> p4,
            Mono<? extends T5> p5,
            Mono<? extends T6> p6,
            Mono<? extends T7> p7,
            Mono<? extends T8> p8,
            Mono<? extends T9> p9,
            Mono<? extends T10> p10,
            Mono<? extends T11> p11
    ) {
        //noinspection unchecked
        return Mono.zip(objects -> new Tuple11<>((T1) objects[0], (T2) objects[1], (T3) objects[2], (T4) objects[3], (T5) objects[4], (T6) objects[5], (T7) objects[6], (T8) objects[7], (T9) objects[8], (T10) objects[9], (T11) objects[10]),
                        p1,
                        p2,
                        p3,
                        p4,
                        p5,
                        p6,
                        p7,
                        p8,
                        p9,
                        p10,
                        p11);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> Function<Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>, R> function(
            Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> function) {
        return tuple -> function.apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6(), tuple.getT7(), tuple.getT8(), tuple.getT9(), tuple.getT10(), tuple.getT11());
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Mono<Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> zip(
            Mono<? extends T1> p1,
            Mono<? extends T2> p2,
            Mono<? extends T3> p3,
            Mono<? extends T4> p4,
            Mono<? extends T5> p5,
            Mono<? extends T6> p6,
            Mono<? extends T7> p7,
            Mono<? extends T8> p8,
            Mono<? extends T9> p9,
            Mono<? extends T10> p10,
            Mono<? extends T11> p11,
            Mono<? extends T12> p12
    ) {
        //noinspection unchecked
        return Mono.zip(objects -> new Tuple12<>((T1) objects[0], (T2) objects[1], (T3) objects[2], (T4) objects[3], (T5) objects[4], (T6) objects[5], (T7) objects[6], (T8) objects[7], (T9) objects[8], (T10) objects[9], (T11) objects[10], (T12) objects[11]),
                        p1,
                        p2,
                        p3,
                        p4,
                        p5,
                        p6,
                        p7,
                        p8,
                        p9,
                        p10,
                        p11,
                        p12);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> Function<Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>, R> function(
            Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> function) {
        return tuple -> function.apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6(), tuple.getT7(), tuple.getT8(), tuple.getT9(), tuple.getT10(), tuple.getT11(), tuple.getT12());
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Mono<Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> zip(
            Mono<? extends T1> p1,
            Mono<? extends T2> p2,
            Mono<? extends T3> p3,
            Mono<? extends T4> p4,
            Mono<? extends T5> p5,
            Mono<? extends T6> p6,
            Mono<? extends T7> p7,
            Mono<? extends T8> p8,
            Mono<? extends T9> p9,
            Mono<? extends T10> p10,
            Mono<? extends T11> p11,
            Mono<? extends T12> p12,
            Mono<? extends T13> p13
    ) {
        //noinspection unchecked
        return Mono.zip(objects -> new Tuple13<>((T1) objects[0], (T2) objects[1], (T3) objects[2], (T4) objects[3], (T5) objects[4], (T6) objects[5], (T7) objects[6], (T8) objects[7], (T9) objects[8], (T10) objects[9], (T11) objects[10], (T12) objects[11], (T13) objects[12]),
                        p1,
                        p2,
                        p3,
                        p4,
                        p5,
                        p6,
                        p7,
                        p8,
                        p9,
                        p10,
                        p11,
                        p12,
                        p13);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> Function<Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>, R> function(
            Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> function) {
        return tuple -> function.apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6(), tuple.getT7(), tuple.getT8(), tuple.getT9(), tuple.getT10(), tuple.getT11(), tuple.getT12(), tuple.getT13());
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Mono<Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> zip(
            Mono<? extends T1> p1,
            Mono<? extends T2> p2,
            Mono<? extends T3> p3,
            Mono<? extends T4> p4,
            Mono<? extends T5> p5,
            Mono<? extends T6> p6,
            Mono<? extends T7> p7,
            Mono<? extends T8> p8,
            Mono<? extends T9> p9,
            Mono<? extends T10> p10,
            Mono<? extends T11> p11,
            Mono<? extends T12> p12,
            Mono<? extends T13> p13,
            Mono<? extends T14> p14
    ) {
        //noinspection unchecked
        return Mono.zip(objects -> new Tuple14<>((T1) objects[0], (T2) objects[1], (T3) objects[2], (T4) objects[3], (T5) objects[4], (T6) objects[5], (T7) objects[6], (T8) objects[7], (T9) objects[8], (T10) objects[9], (T11) objects[10], (T12) objects[11], (T13) objects[12], (T14) objects[13]),
                        p1,
                        p2,
                        p3,
                        p4,
                        p5,
                        p6,
                        p7,
                        p8,
                        p9,
                        p10,
                        p11,
                        p12,
                        p13,
                        p14);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> Function<Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>, R> function(
            Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> function) {
        return tuple -> function.apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6(), tuple.getT7(), tuple.getT8(), tuple.getT9(), tuple.getT10(), tuple.getT11(), tuple.getT12(), tuple.getT13(), tuple.getT14());
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Mono<Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> zip(
            Mono<? extends T1> p1,
            Mono<? extends T2> p2,
            Mono<? extends T3> p3,
            Mono<? extends T4> p4,
            Mono<? extends T5> p5,
            Mono<? extends T6> p6,
            Mono<? extends T7> p7,
            Mono<? extends T8> p8,
            Mono<? extends T9> p9,
            Mono<? extends T10> p10,
            Mono<? extends T11> p11,
            Mono<? extends T12> p12,
            Mono<? extends T13> p13,
            Mono<? extends T14> p14,
            Mono<? extends T15> p15
    ) {
        //noinspection unchecked
        return Mono.zip(objects -> new Tuple15<>((T1) objects[0], (T2) objects[1], (T3) objects[2], (T4) objects[3], (T5) objects[4], (T6) objects[5], (T7) objects[6], (T8) objects[7], (T9) objects[8], (T10) objects[9], (T11) objects[10], (T12) objects[11], (T13) objects[12], (T14) objects[13], (T15) objects[14]),
                        p1,
                        p2,
                        p3,
                        p4,
                        p5,
                        p6,
                        p7,
                        p8,
                        p9,
                        p10,
                        p11,
                        p12,
                        p13,
                        p14,
                        p15);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> Function<Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>, R> function(
            Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> function) {
        return tuple -> function.apply(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6(), tuple.getT7(), tuple.getT8(), tuple.getT9(), tuple.getT10(), tuple.getT11(), tuple.getT12(), tuple.getT13(), tuple.getT14(), tuple.getT15());
    }
}
