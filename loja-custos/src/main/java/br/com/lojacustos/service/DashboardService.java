package br.com.lojacustos.service;

import br.com.lojacustos.domain.AllocationMode;
import br.com.lojacustos.domain.AppSettings;
import br.com.lojacustos.domain.MarketplaceProfile;
import br.com.lojacustos.domain.OperationalCostLine;
import br.com.lojacustos.domain.PricingMode;
import br.com.lojacustos.domain.Product;
import br.com.lojacustos.repo.AppSettingsRepository;
import br.com.lojacustos.repo.MarketplaceProfileRepository;
import br.com.lojacustos.repo.OperationalCostLineRepository;
import br.com.lojacustos.repo.ProductRepository;
import br.com.lojacustos.service.CostCalculationService.ProductDashboardRow;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

  private final ProductRepository productRepository;
  private final OperationalCostLineRepository operationalCostLineRepository;
  private final MarketplaceProfileRepository marketplaceProfileRepository;
  private final AppSettingsRepository appSettingsRepository;
  private final CostCalculationService costCalculationService;

  public DashboardService(
      ProductRepository productRepository,
      OperationalCostLineRepository operationalCostLineRepository,
      MarketplaceProfileRepository marketplaceProfileRepository,
      AppSettingsRepository appSettingsRepository,
      CostCalculationService costCalculationService) {
    this.productRepository = productRepository;
    this.operationalCostLineRepository = operationalCostLineRepository;
    this.marketplaceProfileRepository = marketplaceProfileRepository;
    this.appSettingsRepository = appSettingsRepository;
    this.costCalculationService = costCalculationService;
  }

  @Transactional(readOnly = true)
  public DashboardData load() {
    AppSettings settings = settingsOrDefault();
    List<Product> products = productRepository.findAll();
    List<OperationalCostLine> lines = operationalCostLineRepository.findAll();
    List<MarketplaceProfile> mps = marketplaceProfileRepository.findAll();
    BigDecimal totalOp = costCalculationService.totalOperationalMonthly(lines);
    List<ProductDashboardRow> rows =
        costCalculationService.buildRows(products, mps, settings, totalOp);
    return new DashboardData(settings, products, lines, mps, totalOp, rows);
  }

  @Transactional
  public void addProduct(
      String name,
      PricingMode mode,
      BigDecimal costPerKg,
      BigDecimal costPerUnit,
      BigDecimal kgPerUnit,
      BigDecimal packagingCostPerSale,
      BigDecimal inboundFreightPerSale) {
    Product p = new Product();
    p.setName(name.trim());
    p.setPricingMode(mode);
    p.setCostPerKg(costPerKg);
    p.setCostPerUnit(costPerUnit);
    p.setKgPerUnit(kgPerUnit);
    p.setPackagingCostPerSale(
        packagingCostPerSale == null ? java.math.BigDecimal.ZERO : packagingCostPerSale);
    p.setInboundFreightPerSale(
        inboundFreightPerSale == null ? java.math.BigDecimal.ZERO : inboundFreightPerSale);
    productRepository.save(p);
  }

  @Transactional
  public void updateProductLogistics(
      long id, BigDecimal packagingCostPerSale, BigDecimal inboundFreightPerSale) {
    Product p =
        productRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
    p.setPackagingCostPerSale(
        packagingCostPerSale == null ? java.math.BigDecimal.ZERO : packagingCostPerSale);
    p.setInboundFreightPerSale(
        inboundFreightPerSale == null ? java.math.BigDecimal.ZERO : inboundFreightPerSale);
    productRepository.save(p);
  }

  @Transactional
  public void deleteProduct(long id) {
    productRepository.deleteById(id);
  }

  @Transactional
  public void addOperationalLine(String name, BigDecimal amount) {
    OperationalCostLine line = new OperationalCostLine();
    line.setName(name.trim());
    line.setMonthlyAmount(amount);
    operationalCostLineRepository.save(line);
  }

  @Transactional
  public void deleteOperationalLine(long id) {
    operationalCostLineRepository.deleteById(id);
  }

  @Transactional
  public void updateSettings(
      AllocationMode allocationMode,
      BigDecimal monthlyTotalKgSold,
      BigDecimal monthlyTotalUnitsSold,
      BigDecimal targetMarginPercent,
      BigDecimal taxPercentOnInvoice,
      boolean mercadoLivreUsePremium) {
    AppSettings s = settingsOrDefault();
    s.setAllocationMode(allocationMode == null ? AllocationMode.NONE : allocationMode);
    s.setMonthlyTotalKgSold(monthlyTotalKgSold);
    s.setMonthlyTotalUnitsSold(monthlyTotalUnitsSold);
    if (targetMarginPercent != null) {
      s.setTargetMarginPercent(targetMarginPercent);
    }
    s.setTaxPercentOnInvoice(
        taxPercentOnInvoice == null ? java.math.BigDecimal.ZERO : taxPercentOnInvoice);
    s.setMercadoLivreUsePremium(mercadoLivreUsePremium);
    appSettingsRepository.save(s);
  }

  @Transactional
  public void updateMarketplace(
      long id,
      BigDecimal feePercent,
      BigDecimal fixedPerSale,
      BigDecimal feePercentPremium,
      BigDecimal fixedFeePerSalePremium) {
    MarketplaceProfile mp =
        marketplaceProfileRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Marketplace não encontrado"));
    mp.setFeePercent(feePercent == null ? BigDecimal.ZERO : feePercent);
    mp.setFixedFeePerSale(fixedPerSale == null ? BigDecimal.ZERO : fixedPerSale);
    mp.setFeePercentPremium(feePercentPremium);
    mp.setFixedFeePerSalePremium(fixedFeePerSalePremium);
    marketplaceProfileRepository.save(mp);
  }

  private AppSettings settingsOrDefault() {
    return appSettingsRepository
        .findById(AppSettings.SINGLETON_ID)
        .orElseGet(
            () -> {
              AppSettings s = new AppSettings();
              s.setId(AppSettings.SINGLETON_ID);
              return appSettingsRepository.save(s);
            });
  }

  public record DashboardData(
      AppSettings settings,
      List<Product> products,
      List<OperationalCostLine> operationalLines,
      List<MarketplaceProfile> marketplaces,
      BigDecimal totalOperationalMonthly,
      List<ProductDashboardRow> rows) {}
}
