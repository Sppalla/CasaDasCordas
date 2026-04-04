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
  private static final BigDecimal HUNDRED = new BigDecimal("100");
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

  private static BigDecimal nz(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }

  /**
   * Custo variável (compra + embalagem + frete entrada) e custo carregado (+ rateio operacional no
   * produto, se houver). A fórmula de preço usa o custo carregado; margem de contribuição e ponto de
   * equilíbrio usam o custo variável + taxas de canal.
   */
  public StoreCostResult storeCostForProduct(
      Product p, AppSettings settings, BigDecimal opPerKg, BigDecimal opPerUnit) {
    BigDecimal pack = nz(p.getPackagingCostPerSale());
    BigDecimal freight = nz(p.getInboundFreightPerSale());

    if (p.getPricingMode() == PricingMode.BY_KG) {
      if (p.getCostPerKg() == null || p.getCostPerKg().compareTo(BigDecimal.ZERO) < 0) {
        return StoreCostResult.invalid("Informe o custo por kg.");
      }
      BigDecimal variable = p.getCostPerKg().add(pack).add(freight, MC);
      BigDecimal loaded = variable;
      if (settings.getAllocationMode() == AllocationMode.BY_TOTAL_KG) {
        loaded = loaded.add(opPerKg);
      }
      return StoreCostResult.ok(
          loaded.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
          variable.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
    }

    if (p.getCostPerUnit() == null || p.getCostPerUnit().compareTo(BigDecimal.ZERO) < 0) {
      return StoreCostResult.invalid("Informe o custo por unidade.");
    }
    BigDecimal variable = p.getCostPerUnit().add(pack).add(freight, MC);
    BigDecimal loaded = variable;
    switch (settings.getAllocationMode()) {
      case BY_TOTAL_UNITS -> loaded = loaded.add(opPerUnit);
      case BY_TOTAL_KG -> {
        if (p.getKgPerUnit() == null || p.getKgPerUnit().compareTo(BigDecimal.ZERO) <= 0) {
          return StoreCostResult.invalid(
              "No rateio por kg, informe quantos kg tem 1 unidade deste produto.");
        }
        loaded = loaded.add(opPerKg.multiply(p.getKgPerUnit(), MC));
      }
      default -> {}
    }
    return StoreCostResult.ok(
        loaded.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
        variable.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
  }

  /**
   * Preço = (custo carregado + taxa fixa do canal) / (1 − (% comissão + % imposto + % margem
   * desejada)/100).
   */
  public MarketplaceQuote quoteMarketplace(
      BigDecimal loadedStoreCost,
      BigDecimal variableCostPerSale,
      BigDecimal marginPercentOnPrice,
      BigDecimal taxPercentOnInvoice,
      MarketplaceProfile mp,
      boolean mercadoLivreUsePremium,
      BigDecimal totalOperationalMonthly) {
    BigDecimal comm = nz(mp.getFeePercent());
    BigDecimal fixed = nz(mp.getFixedFeePerSale());
    if ("ML".equals(mp.getCode())
        && mercadoLivreUsePremium
        && mp.getFeePercentPremium() != null) {
      comm = nz(mp.getFeePercentPremium());
      if (mp.getFixedFeePerSalePremium() != null) {
        fixed = nz(mp.getFixedFeePerSalePremium());
      }
    }

    BigDecimal tax = nz(taxPercentOnInvoice);
    BigDecimal margin = nz(marginPercentOnPrice);

    BigDecimal sumShare =
        comm.divide(HUNDRED, MC)
            .add(tax.divide(HUNDRED, MC), MC)
            .add(margin.divide(HUNDRED, MC), MC);
    if (sumShare.compareTo(BigDecimal.ONE) >= 0) {
      return new MarketplaceQuote(
          mp.getDisplayName(),
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          "Soma de comissão + imposto + margem deve ser menor que 100%.");
    }

    BigDecimal denom = BigDecimal.ONE.subtract(sumShare, MC);
    if (denom.compareTo(BigDecimal.ZERO) <= 0) {
      return new MarketplaceQuote(
          mp.getDisplayName(),
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          "Denominador inválido no cálculo de preço.");
    }

    BigDecimal num = loadedStoreCost.add(fixed, MC);
    BigDecimal suggested = num.divide(denom, MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

    BigDecimal variableFee =
        suggested.multiply(comm.divide(HUNDRED, MC), MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    BigDecimal taxAmount =
        suggested.multiply(tax.divide(HUNDRED, MC), MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    BigDecimal totalFee = variableFee.add(fixed, MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    BigDecimal netAfter =
        suggested.subtract(totalFee, MC).subtract(taxAmount, MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

    BigDecimal contributionMargin =
        suggested
            .subtract(variableCostPerSale, MC)
            .subtract(variableFee, MC)
            .subtract(fixed, MC)
            .subtract(taxAmount, MC)
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

    BigDecimal contributionMarginPercent =
        suggested.compareTo(BigDecimal.ZERO) > 0
            ? contributionMargin
                .multiply(HUNDRED, MC)
                .divide(suggested, MC)
                .setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    BigDecimal markup =
        variableCostPerSale.compareTo(BigDecimal.ZERO) > 0
            ? suggested.divide(variableCostPerSale, MC).setScale(3, RoundingMode.HALF_UP)
            : null;

    BigDecimal breakEvenUnits = null;
    if (contributionMargin.compareTo(BigDecimal.ZERO) > 0
        && totalOperationalMonthly != null
        && totalOperationalMonthly.compareTo(BigDecimal.ZERO) > 0) {
      breakEvenUnits =
          totalOperationalMonthly
              .divide(contributionMargin, MC)
              .setScale(1, RoundingMode.HALF_UP);
    }

    return new MarketplaceQuote(
        mp.getDisplayName(),
        suggested,
        totalFee,
        netAfter,
        contributionMargin,
        contributionMarginPercent,
        markup,
        breakEvenUnits,
        taxAmount,
        comm,
        fixed,
        tax,
        null);
  }

  public List<ProductDashboardRow> buildRows(
      List<Product> products,
      List<MarketplaceProfile> marketplaces,
      AppSettings settings,
      BigDecimal totalOperational) {
    BigDecimal opKg = operationalPerKg(settings, totalOperational);
    BigDecimal opUnit = operationalPerUnit(settings, totalOperational);
    BigDecimal margin = settings.getTargetMarginPercent();
    BigDecimal tax = settings.getTaxPercentOnInvoice();
    boolean mlPremium = settings.isMercadoLivreUsePremium();

    List<ProductDashboardRow> rows = new ArrayList<>();
    for (Product p : products) {
      StoreCostResult sc = storeCostForProduct(p, settings, opKg, opUnit);
      List<MarketplaceQuote> quotes = new ArrayList<>();
      BigDecimal maxAmong = new BigDecimal("0.01");
      if (sc.valid()) {
        for (MarketplaceProfile mp : marketplaces) {
          MarketplaceQuote q =
              quoteMarketplace(
                  sc.storeCost(),
                  sc.variableCost(),
                  margin,
                  tax,
                  mp,
                  mlPremium,
                  totalOperational);
          quotes.add(q);
          if (q.errorMessage() == null
              && q.contributionMargin() != null
              && q.contributionMargin().compareTo(maxAmong) > 0) {
            maxAmong = q.contributionMargin();
          }
        }
      }
      rows.add(new ProductDashboardRow(p, sc, quotes, maxAmong));
    }
    return rows;
  }

  public record StoreCostResult(boolean valid, BigDecimal storeCost, BigDecimal variableCost, String errorMessage) {
    static StoreCostResult ok(BigDecimal loaded, BigDecimal variable) {
      return new StoreCostResult(true, loaded, variable, null);
    }

    static StoreCostResult invalid(String msg) {
      return new StoreCostResult(false, null, null, msg);
    }
  }

  public record MarketplaceQuote(
      String marketplaceName,
      BigDecimal suggestedSalePrice,
      BigDecimal estimatedTotalFee,
      BigDecimal estimatedNetAfterFees,
      BigDecimal contributionMargin,
      BigDecimal contributionMarginPercent,
      BigDecimal markup,
      BigDecimal breakEvenUnits,
      BigDecimal estimatedTaxAmount,
      BigDecimal commissionPercentUsed,
      BigDecimal marketplaceFixedFeeUsed,
      BigDecimal taxPercentUsed,
      String errorMessage) {}

  public record ProductDashboardRow(
      Product product,
      StoreCostResult storeCost,
      List<MarketplaceQuote> marketplaceQuotes,
      BigDecimal maxContributionAmongChannels) {}
}
