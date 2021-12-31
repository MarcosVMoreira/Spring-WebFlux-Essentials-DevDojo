package com.marcosmoreira.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class WebfluxApplication {

    static {
        BlockHound.install(
            builder -> builder.allowBlockingCallsInside("java.util.UUID", "randomUUID")
        );
    }

    public static void main(String[] args) {
//        System.setProperty("reactor.netty.ioWorkerCount", "100");
        //ativar numero de threads para comparar com tomcat durante teste de stress
        SpringApplication.run(WebfluxApplication.class, args);
    }
}