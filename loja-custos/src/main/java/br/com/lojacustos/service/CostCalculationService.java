package br.com.lojacustos.service;

import br.com.lojacustos.domain.AllocationMode;
import br.com.lojacustos.domain.AppSettings;
import br.com.lojacustos.domain.MarketplaceProfile;
import br.com.lojacustos.domain.PricingMode;
import br.com.lojacustos.domain.Product;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CostCalculationService {

  private static final MathContext MC = new MathContext(12, RoundingMode.HALF_UP);
  private static final int MONEY_SCALE = 2;

  public BigDecimal totalOperationalMonthly(Iterable<br.com.lojacustos.domain.OperationalCostLine> lines) {
    BigDecimal sum = BigDecimal.ZERO;
    for (var line : lines) {
      if (line.getMonthlyAmount() != null) {
        sum = sum.add(line.getMonthlyAmount());
      }
    }
    return sum.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
  }

  public BigDecimal operationalPerKg(AppSettings settings, BigDecimal totalOperational) {
    if (settings.getAllocationMode() != AllocationMode.BY_TOTAL_KG) {
      return BigDecimal.ZERO;
    }
    BigDecimal kg = settings.getMonthlyTotalKgSold();
    if (kg == null || kg.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return totalOperational.divide(kg, MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
  }

  public BigDecimal operationalPerUnit(AppSettings settings, BigDecimal totalOperational) {
    if (settings.getAllocationMode() != AllocationMode.BY_TOTAL_UNITS) {
      return BigDecimal.ZERO;
    }
    BigDecimal units = settings.getMonthlyTotalUnitsSold();
    if (units == null || units.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return totalOperational.divide(units, MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
  }

  /**
   * Custo “para a loja” (compra + rateio operacional, se configurado).
   *
   * @return empty se dados insuficientes (ex.: unidade sem kg/unidade no rateio por kg)
   */
  public StoreCostResult storeCostForProduct(
      Product p, AppSettings settings, BigDecimal opPerKg, BigDecimal opPerUnit) {
    if (p.getPricingMode() == PricingMode.BY_KG) {
      if (p.getCostPerKg() == null || p.getCostPerKg().compareTo(BigDecimal.ZERO) < 0) {
        return StoreCostResult.invalid("Informe o custo por kg.");
      }
      BigDecimal base = p.getCostPerKg();
      if (settings.getAllocationMode() == AllocationMode.BY_TOTAL_KG) {
        base = base.add(opPerKg);
      }
      return StoreCostResult.ok(base.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
    }

    if (p.getCostPerUnit() == null || p.getCostPerUnit().compareTo(BigDecimal.ZERO) < 0) {
      return StoreCostResult.invalid("Informe o custo por unidade.");
    }
    BigDecimal base = p.getCostPerUnit();
    switch (settings.getAllocationMode()) {
      case BY_TOTAL_UNITS -> base = base.add(opPerUnit);
      case BY_TOTAL_KG -> {
        if (p.getKgPerUnit() == null || p.getKgPerUnit().compareTo(BigDecimal.ZERO) <= 0) {
          return StoreCostResult.invalid(
              "No rateio por kg, informe quantos kg tem 1 unidade deste produto.");
        }
        base = base.add(opPerKg.multiply(p.getKgPerUnit(), MC));
      }
      default -> {}
    }
    return StoreCostResult.ok(base.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
  }

  public MarketplaceQuote quoteMarketplace(
      BigDecimal storeCost, BigDecimal marginPercent, MarketplaceProfile mp) {
    BigDecimal m = marginPercent == null ? BigDecimal.ZERO : marginPercent;
    BigDecimal marginFactor = BigDecimal.ONE.add(m.divide(new BigDecimal("100"), MC));
    BigDecimal targetNet =
        storeCost.multiply(marginFactor, MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

    BigDecimal pct = mp.getFeePercent() == null ? BigDecimal.ZERO : mp.getFeePercent();
    BigDecimal fixed = mp.getFixedFeePerSale() == null ? BigDecimal.ZERO : mp.getFixedFeePerSale();

    if (pct.compareTo(new BigDecimal("100")) >= 0) {
      return new MarketplaceQuote(
          mp.getDisplayName(), null, null, null, "Taxa percentual deve ser menor que 100%.");
    }

    BigDecimal oneMinus =
        BigDecimal.ONE.subtract(pct.divide(new BigDecimal("100"), MC), MC);
    if (oneMinus.compareTo(BigDecimal.ZERO) <= 0) {
      return new MarketplaceQuote(
          mp.getDisplayName(), null, null, null, "Taxa percentual inválida.");
    }

    BigDecimal suggested =
        targetNet.add(fixed, MC).divide(oneMinus, MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

    BigDecimal variableFee =
        suggested.multiply(pct.divide(new BigDecimal("100"), MC), MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    BigDecimal totalFee = variableFee.add(fixed, MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    BigDecimal netAfter =
        suggested.subtract(totalFee, MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

    return new MarketplaceQuote(mp.getDisplayName(), suggested, totalFee, netAfter, null);
  }

  public List<ProductDashboardRow> buildRows(
      List<Product> products,
      List<MarketplaceProfile> marketplaces,
      AppSettings settings,
      BigDecimal totalOperational) {
    BigDecimal opKg = operationalPerKg(settings, totalOperational);
    BigDecimal opUnit = operationalPerUnit(settings, totalOperational);
    BigDecimal margin = settings.getTargetMarginPercent();

    List<ProductDashboardRow> rows = new ArrayList<>();
    for (Product p : products) {
      StoreCostResult sc = storeCostForProduct(p, settings, opKg, opUnit);
      List<MarketplaceQuote> quotes = new ArrayList<>();
      if (sc.valid()) {
        for (MarketplaceProfile mp : marketplaces) {
          quotes.add(quoteMarketplace(sc.storeCost(), margin, mp));
        }
      }
      rows.add(new ProductDashboardRow(p, sc, quotes));
    }
    return rows;
  }

  public record StoreCostResult(boolean valid, BigDecimal storeCost, String errorMessage) {
    static StoreCostResult ok(BigDecimal cost) {
      return new StoreCostResult(true, cost, null);
    }

    static StoreCostResult invalid(String msg) {
      return new StoreCostResult(false, null, msg);
    }
  }

  public record MarketplaceQuote(
      String marketplaceName,
      BigDecimal suggestedSalePrice,
      BigDecimal estimatedTotalFee,
      BigDecimal estimatedNetAfterFees,
      String errorMessage) {}

  public record ProductDashboardRow(
      Product product, StoreCostResult storeCost, List<MarketplaceQuote> marketplaceQuotes) {}
}
