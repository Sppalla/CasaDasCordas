package br.com.lojacustos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PricingMode pricingMode;

  /** Custo de compra / custo para a loja por kg (modo BY_KG) */
  @Column(precision = 19, scale = 4)
  private BigDecimal costPerKg;

  /** Custo de compra / custo para a loja por unidade (modo BY_UNIT) */
  @Column(precision = 19, scale = 4)
  private BigDecimal costPerUnit;

  /**
   * Quando o produto é por unidade e o rateio operacional é por kg: peso médio de 1 unidade em kg
   * (ex.: pacote 500g → 0,5).
   */
  @Column(precision = 19, scale = 6)
  private BigDecimal kgPerUnit;

  /** Embalagem + insumos por unidade de venda (mesma base do custo: por kg ou por unidade). */
  @Column(precision = 19, scale = 4)
  private BigDecimal packagingCostPerSale;

  /** Frete de entrada (aquisição) rateado por unidade de venda (mesma base). */
  @Column(precision = 19, scale = 4)
  private BigDecimal inboundFreightPerSale;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PricingMode getPricingMode() {
    return pricingMode;
  }

  public void setPricingMode(PricingMode pricingMode) {
    this.pricingMode = pricingMode;
  }

  public BigDecimal getCostPerKg() {
    return costPerKg;
  }

  public void setCostPerKg(BigDecimal costPerKg) {
    this.costPerKg = costPerKg;
  }

  public BigDecimal getCostPerUnit() {
    return costPerUnit;
  }

  public void setCostPerUnit(BigDecimal costPerUnit) {
    this.costPerUnit = costPerUnit;
  }

  public BigDecimal getKgPerUnit() {
    return kgPerUnit;
  }

  public void setKgPerUnit(BigDecimal kgPerUnit) {
    this.kgPerUnit = kgPerUnit;
  }

  public BigDecimal getPackagingCostPerSale() {
    return packagingCostPerSale;
  }

  public void setPackagingCostPerSale(BigDecimal packagingCostPerSale) {
    this.packagingCostPerSale = packagingCostPerSale;
  }

  public BigDecimal getInboundFreightPerSale() {
    return inboundFreightPerSale;
  }

  public void setInboundFreightPerSale(BigDecimal inboundFreightPerSale) {
    this.inboundFreightPerSale = inboundFreightPerSale;
  }
}
