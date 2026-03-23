package br.com.lojacustos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "marketplace_profiles")
public class MarketplaceProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Código estável: ML, SHOPEE, TIKTOK */
  @Column(nullable = false, unique = true, length = 32)
  private String code;

  @Column(nullable = false, length = 80)
  private String displayName;

  @Column(nullable = false, precision = 9, scale = 4)
  private BigDecimal feePercent = BigDecimal.ZERO;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal fixedFeePerSale = BigDecimal.ZERO;

  @Column(length = 500)
  private String notes;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public BigDecimal getFeePercent() {
    return feePercent;
  }

  public void setFeePercent(BigDecimal feePercent) {
    this.feePercent = feePercent;
  }

  public BigDecimal getFixedFeePerSale() {
    return fixedFeePerSale;
  }

  public void setFixedFeePerSale(BigDecimal fixedFeePerSale) {
    this.fixedFeePerSale = fixedFeePerSale;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
