package br.com.lojacustos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "app_settings")
public class AppSettings {

  public static final long SINGLETON_ID = 1L;

  @Id
  private Long id = SINGLETON_ID;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private AllocationMode allocationMode = AllocationMode.NONE;

  /** Usado quando allocationMode = BY_TOTAL_KG */
  @Column(precision = 19, scale = 4)
  private BigDecimal monthlyTotalKgSold;

  /** Usado quando allocationMode = BY_TOTAL_UNITS */
  @Column(precision = 19, scale = 4)
  private BigDecimal monthlyTotalUnitsSold;

  /** Margem desejada sobre o custo total da loja (%) — usada na sugestão de preço */
  @Column(nullable = false, precision = 9, scale = 4)
  private BigDecimal targetMarginPercent = new BigDecimal("20");

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AllocationMode getAllocationMode() {
    return allocationMode;
  }

  public void setAllocationMode(AllocationMode allocationMode) {
    this.allocationMode = allocationMode;
  }

  public BigDecimal getMonthlyTotalKgSold() {
    return monthlyTotalKgSold;
  }

  public void setMonthlyTotalKgSold(BigDecimal monthlyTotalKgSold) {
    this.monthlyTotalKgSold = monthlyTotalKgSold;
  }

  public BigDecimal getMonthlyTotalUnitsSold() {
    return monthlyTotalUnitsSold;
  }

  public void setMonthlyTotalUnitsSold(BigDecimal monthlyTotalUnitsSold) {
    this.monthlyTotalUnitsSold = monthlyTotalUnitsSold;
  }

  public BigDecimal getTargetMarginPercent() {
    return targetMarginPercent;
  }

  public void setTargetMarginPercent(BigDecimal targetMarginPercent) {
    this.targetMarginPercent = targetMarginPercent;
  }
}
