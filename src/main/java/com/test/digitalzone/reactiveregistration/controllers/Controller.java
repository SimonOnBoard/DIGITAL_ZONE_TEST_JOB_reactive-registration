package com.test.digitalzone.reactiveregistration.controllers;


import com.test.digitalzone.reactiveregistration.dto.StatisticsDto;
import com.test.digitalzone.reactiveregistration.dto.ViewEventDto;
import com.test.digitalzone.reactiveregistration.services.interfaces.SaveEventService;
import com.test.digitalzone.reactiveregistration.services.interfaces.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Controller {
    private final SaveEventService saveEventService;
    private final StatisticsService statisticsService;

//    @GetMapping("/test")
//    public void testRedis() throws ExecutionException, InterruptedException {
//        Random random = new Random();
//        for(int i = 0; i < 100; i++){
//            publisher.publish("aaa" + i);
//        }
//
////        x.add("day","y");
////        x.add("day","x");
////        Mono<Long> y = x.size("day");
////        y.subscribe(
////                value -> System.out.println(value),
////                error -> error.getMessage(),
////        );
//    }

    @GetMapping("/registerEvent")
    public StatisticsDto addViewEvent(@RequestBody ViewEventDto eventDto){
        saveEventService.saveEvent(eventDto);
        return statisticsService.getCurrentDayStatistics();
    }
}
