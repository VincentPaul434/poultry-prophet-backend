package com.poultryprophet.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * SDD 2.4: threshold values stored in the database and editable without code changes.
 * A row with a null {@code farmId} acts as the global default for an indicator.
 */
@Entity
@Table(name = "threshold_config")
public class ThresholdConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id")
    private Long farmId;

    /** Indicator key: BHI, BSI, or WFR. */
    @Column(nullable = false)
    private String indicator;

    @Column(nullable = false)
    private double minValue;

    @Column(nullable = false)
    private double maxValue;

    public ThresholdConfig() {
    }

    public ThresholdConfig(Long farmId, String indicator, double minValue, double maxValue) {
        this.farmId = farmId;
        this.indicator = indicator;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFarmId() {
        return farmId;
    }

    public void setFarmId(Long farmId) {
        this.farmId = farmId;
    }

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }
}
