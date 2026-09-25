package com.keelean.accountmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Creates accounts for one partner from many concurrent requests against a real Postgres. Before sequence
 * reservation took a row lock, concurrent requests for a partner failed the optimistic lock on its config.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "kafka.listen.auto.start=false",
        "logging.level.com.keelean=WARN",
        "logging.level.org.springframework.web=WARN"
})
class ConcurrentAccountCreationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    private static boolean poolCreated;

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void createDedicatedPool() throws Exception {
        if (!poolCreated) {
            assertOk(post("/account-pool", "{\"capacity\":\"THOUSAND_10\",\"prefixSeries\":31,\"poolType\":\"DEDICATED\"}"));
            poolCreated = true;
        }
    }

    @Test
    void concurrentCreationsForOnePartnerAllSucceedWithUniqueIds() throws Exception {
        createConfig("CONC", "DYNAMIC");
        createConfig("CONC", "STATIC");

        List<String[]> calls = new ArrayList<>();
        // 50 dynamic pre-creations share the partner's DYNAMIC sequence
        IntStream.range(0, 50).forEach(i -> calls.add(new String[]{"/virtualAccounts/pre",
                "{\"partnerId\":\"CONC\",\"referenceId\":\"D-" + i + "\"}"}));
        // 20 static full creations and 4 x 25-item pre-bulk requests share its STATIC sequence
        IntStream.range(0, 20).forEach(i -> calls.add(new String[]{"/virtualAccounts",
                "{\"partnerId\":\"CONC\",\"referenceId\":\"S-" + i + "\",\"accountType\":\"STATIC\",\"accountName\":\"ACME S" + i
                        + "\",\"amount\":100,\"timeoutInMins\":30}"}));
        IntStream.range(0, 4).forEach(b -> calls.add(new String[]{"/virtualAccounts/preBulk", "{\"requests\":["
                + IntStream.range(0, 25).mapToObj(i -> "{\"partnerId\":\"CONC\",\"referenceId\":\"PB-" + b + "-" + i + "\"}")
                .collect(Collectors.joining(",")) + "]}"}));

        List<HttpResponse<String>> responses = new ArrayList<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<HttpResponse<String>>> futures = new ArrayList<>();
            for (String[] call : calls) {
                futures.add(executor.submit(() -> post(call[0], call[1])));
            }
            for (Future<HttpResponse<String>> future : futures) {
                responses.add(future.get());
            }
        }

        List<String> failures = responses.stream().filter(r -> r.statusCode() != 200)
                .map(r -> r.statusCode() + " " + r.body()).toList();
        assertEquals(List.of(), failures);

        assertEquals(170, count("select count(*) from account_customer where partner_id = 'CONC'"));
        assertEquals(170, count("select count(distinct account_id) from account_customer where partner_id = 'CONC'"));
        // Sequences start at -1, so after n reservations the current sequence is n - 1
        assertEquals(49, count("select current_sequence from partner_account_config where partner_id = 'CONC' and meta->>'accountType' = 'DYNAMIC'"));
        assertEquals(119, count("select current_sequence from partner_account_config where partner_id = 'CONC' and meta->>'accountType' = 'STATIC'"));
    }

    @Test
    void bulkRequestWithOneInvalidItemCreatesNothing() throws Exception {
        createConfig("ATOM", "STATIC");

        HttpResponse<String> response = post("/virtualAccounts/bulk", "{\"requests\":["
                + "{\"partnerId\":\"ATOM\",\"referenceId\":\"A-1\",\"accountType\":\"STATIC\",\"accountName\":\"ACME A1\",\"amount\":10,\"timeoutInMins\":30},"
                + "{\"partnerId\":\"ATOM\",\"referenceId\":\"A-2\",\"accountType\":\"STATIC\",\"accountName\":\"ACME A2\",\"amount\":10,\"timeoutInMins\":30},"
                // Minimum deposit multiplier must be between 1 and 10
                + "{\"partnerId\":\"ATOM\",\"referenceId\":\"A-3\",\"accountType\":\"STATIC\",\"accountName\":\"ACME A3\",\"amount\":10,\"timeoutInMins\":30,\"minDeposit\":50,\"maxDeposit\":60}"
                + "]}");

        assertEquals(400, response.statusCode(), response.body());
        assertEquals(0, count("select count(*) from account_customer where partner_id = 'ATOM'"));
    }

    private void createConfig(String partnerId, String accountType) throws Exception {
        // Static configs need a deposit multiplier range
        String deposits = "STATIC".equals(accountType) ? ",\"minDeposit\":1,\"maxDeposit\":10" : "";
        assertOk(post("/va/configs", "{\"partnerId\":\"" + partnerId + "\",\"code\":\"ACME\",\"accountPrefix\":\"31\","
                + "\"capacity\":\"THOUSAND_10\",\"defaultLookUpDisplayName\":\"ACME\",\"accountType\":\"" + accountType + "\"" + deposits + "}"));
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/ca/v1" + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void assertOk(HttpResponse<String> response) {
        assertEquals(200, response.statusCode(), response.body());
    }

    private int count(String sql) {
        return jdbc.queryForObject(sql, Integer.class);
    }
}
