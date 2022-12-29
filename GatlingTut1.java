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
            .baseUrl("http://localhost:3000")
            .contentTypeHeader("application/json")
            .acceptHeader("application/json")
            .header("Authenticate","Bearer #{token}");


    private ChainBuilder setToken = exec(
            session -> session.set("token", GatlingTut1.jwt));
    private ChainBuilder getAuth =
            exec(setToken)
            .exec(http("get auth token")
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
            .exec(http("createOrder")
            .post("/order")
            .body(StringBody(session -> getPayload()))
            .check(status().saveAs("status"))
            .check(status().is(200))
            );
    private ScenarioBuilder scn = scenario("Get Auth")
            .during(Duration.ofSeconds(120)).on(
             exec(getAuth)
            .pause(Duration.ofSeconds(60)
            ));

    private ScenarioBuilder scn1 = scenario("Create Order")
            .during(Duration.ofSeconds(120)).on(
            pause(Duration.ofSeconds(5)) // wait for auth request to complete
            .exec(order)
            .pause(Duration.ofSeconds(10)
            ));



    {

        setUp(
                scn.injectOpen(
                        atOnceUsers(1)
                ),
                scn1.injectOpen(
                        atOnceUsers(1)
                )
        ).protocols(httpProtocol);




    }

    public String getPayload(){
        return "{\"orderNum\":\""+ UUID.randomUUID().toString()+"\"}";
    }


}
