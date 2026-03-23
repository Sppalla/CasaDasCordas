package br.com.lojacustos.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loja-custos.marketplace-defaults")
public class MarketplaceDefaultsProperties {

  private Fee mercadoLivre = new Fee();
  private Fee shopee = new Fee();
  private Fee tiktokShop = new Fee();

  public Fee getMercadoLivre() {
    return mercadoLivre;
  }

  public void setMercadoLivre(Fee mercadoLivre) {
    this.mercadoLivre = mercadoLivre;
  }

  public Fee getShopee() {
    return shopee;
  }

  public void setShopee(Fee shopee) {
    this.shopee = shopee;
  }

  public Fee getTiktokShop() {
    return tiktokShop;
  }

  public void setTiktokShop(Fee tiktokShop) {
    this.tiktokShop = tiktokShop;
  }

  public static class Fee {
    private BigDecimal feePercent = BigDecimal.ZERO;
    private BigDecimal fixedPerSale = BigDecimal.ZERO;

    public BigDecimal getFeePercent() {
      return feePercent;
    }

    public void setFeePercent(BigDecimal feePercent) {
      this.feePercent = feePercent;
    }

    public BigDecimal getFixedPerSale() {
      return fixedPerSale;
    }

    public void setFixedPerSale(BigDecimal fixedPerSale) {
      this.fixedPerSale = fixedPerSale;
    }
  }
}
