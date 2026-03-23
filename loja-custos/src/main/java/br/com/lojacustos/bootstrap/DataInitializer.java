package br.com.lojacustos.bootstrap;

import br.com.lojacustos.config.MarketplaceDefaultsProperties;
import br.com.lojacustos.domain.AppSettings;
import br.com.lojacustos.domain.MarketplaceProfile;
import br.com.lojacustos.repo.AppSettingsRepository;
import br.com.lojacustos.repo.MarketplaceProfileRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

  private final MarketplaceProfileRepository marketplaceProfileRepository;
  private final AppSettingsRepository appSettingsRepository;
  private final MarketplaceDefaultsProperties defaults;

  public DataInitializer(
      MarketplaceProfileRepository marketplaceProfileRepository,
      AppSettingsRepository appSettingsRepository,
      MarketplaceDefaultsProperties defaults) {
    this.marketplaceProfileRepository = marketplaceProfileRepository;
    this.appSettingsRepository = appSettingsRepository;
    this.defaults = defaults;
  }

  @Override
  public void run(String... args) {
    if (appSettingsRepository.findById(AppSettings.SINGLETON_ID).isEmpty()) {
      AppSettings s = new AppSettings();
      s.setId(AppSettings.SINGLETON_ID);
      appSettingsRepository.save(s);
    }

    if (marketplaceProfileRepository.count() == 0) {
      marketplaceProfileRepository.save(
          profile(
              "ML",
              "Mercado Livre",
              defaults.getMercadoLivre().getFeePercent(),
              defaults.getMercadoLivre().getFixedPerSale(),
              "Referência genérica (Classic/Premium variam). Ajuste conforme seu tipo de anúncio e categoria."));
      marketplaceProfileRepository.save(
          profile(
              "SHOPEE",
              "Shopee",
              defaults.getShopee().getFeePercent(),
              defaults.getShopee().getFixedPerSale(),
              "Shopee costuma combinar % + taxa fixa por pedido; valores reais dependem de CPF/CNPJ e faixa de preço."));
      marketplaceProfileRepository.save(
          profile(
              "TIKTOK",
              "TikTok Shop",
              defaults.getTiktokShop().getFeePercent(),
              defaults.getTiktokShop().getFixedPerSale(),
              "Referência comum: % sobre o produto + possível taxa fixa em itens abaixo de certo valor; confira central do seller."));
    }
  }

  private static MarketplaceProfile profile(
      String code, String name, BigDecimal pct, BigDecimal fixed, String notes) {
    MarketplaceProfile m = new MarketplaceProfile();
    m.setCode(code);
    m.setDisplayName(name);
    m.setFeePercent(pct == null ? BigDecimal.ZERO : pct);
    m.setFixedFeePerSale(fixed == null ? BigDecimal.ZERO : fixed);
    m.setNotes(notes);
    return m;
  }
}
