package ru.rest.wallet.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {
    private final JdbcTemplate jdbcTemplate;

    public PingController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/ping")
    public String ping(){
        return "Wallet Service is running";
    }
    @GetMapping("/db")
    public String database() {

        Integer result =
                jdbcTemplate.queryForObject(
                        "SELECT 1",
                        Integer.class
                );

        return result != null && result == 1
                ? "Database connected"
                : "Database error";
    }
}
