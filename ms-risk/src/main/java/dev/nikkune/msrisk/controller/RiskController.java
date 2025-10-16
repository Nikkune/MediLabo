package dev.nikkune.msrisk.controller;

import dev.nikkune.msrisk.model.RiskLevel;
import dev.nikkune.msrisk.service.IRiskService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class RiskController {
    private static final Logger logger = LogManager.getLogger(RiskController.class);

    private final IRiskService riskService;

    public RiskController(IRiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping
    public String assessRisk(@RequestParam @Valid String firstName, @RequestParam @Valid String lastName) {
        logger.debug("Assessing risk for {} {}", firstName, lastName);
        RiskLevel risk = riskService.calculateRiskLevel(firstName, lastName);
        logger.info("Risk for {} {} is {}", firstName, lastName, risk.getLabel());
        return risk.getLabel();
    }
}
