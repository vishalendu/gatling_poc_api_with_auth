package com.by.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import net.sf.saxon.om.Chain;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class GatlingTut1 extends Simulation{

    private static String jwt="";

    private HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:3001")
            .contentTypeHeader("application/json")
            .acceptHeader("application/json");

    private ChainBuilder setToken = exec(
            session -> session.set("token", GatlingTut1.jwt)
            );

    private ChainBuilder getAuth =
            exec(http("get auth token")
            .post("/authorize")
            .body(StringBody("{\"username\":\"admin\",\"password\":\"password\"}")
            )
            .check(status().is(200))
            .check(jsonPath("$.token").saveAs("token")))
            .exec(session -> {
                System.out.println("****** Before updating token = "+GatlingTut1.jwt);
                GatlingTut1.jwt = session.get("token");
                System.out.println("****** Updated token = "+GatlingTut1.jwt);
                return session;
                    })
            ;

    private ChainBuilder order =
            exec(setToken)
            .exec(session -> {
                System.out.println("****** token = "+session.get("token"));
                return session;
            })
            .exec(http("createOrder")
            .post("/order")
            .header("Authenticate","Bearer #{token}")
            .body(StringBody(session -> getPayload()))
            .check(status().saveAs("status"))
            .check(status().is(200))
            );
    
    private ScenarioBuilder scn = scenario("Get Auth")
            .during(120).on(
             exec(getAuth)
            .pause(60)
            );

    private ScenarioBuilder scn1 = scenario("Create Order")
            .during(120).on(
                exec(order)
                .pause(10)
            );


    {
        setUp(
                scn.injectOpen(atOnceUsers(1)),
                scn1.injectOpen(nothingFor(5), // let the auth return a token
                                atOnceUsers(1))
        ).protocols(httpProtocol);
    }

    public String getPayload(){
        return "{\"orderNum\":\""+ UUID.randomUUID().toString()+"\"}";
    }


}
