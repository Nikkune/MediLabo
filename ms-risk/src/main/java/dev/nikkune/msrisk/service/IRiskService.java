package dev.nikkune.msrisk.service;

import dev.nikkune.msrisk.model.RiskLevel;

public interface IRiskService {
    RiskLevel calculateRiskLevel(String firstName, String lastName);
}
