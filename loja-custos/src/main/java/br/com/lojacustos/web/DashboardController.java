package br.com.lojacustos.web;

import br.com.lojacustos.domain.AllocationMode;
import br.com.lojacustos.domain.PricingMode;
import br.com.lojacustos.service.DashboardService;
import br.com.lojacustos.web.support.BigDecimalParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {

  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("data", dashboardService.load());
    return "dashboard";
  }

  @GetMapping("/produtos")
  public String productsFallback(RedirectAttributes redirect) {
    redirect.addFlashAttribute("msgError", "Use o formulário do painel para adicionar produtos.");
    return "redirect:/";
  }

  @GetMapping("/error")
  public String errorFallback(RedirectAttributes redirect) {
    redirect.addFlashAttribute("msgError", "Não encontramos essa página. Você foi redirecionado ao painel.");
    return "redirect:/";
  }

  @PostMapping("/produtos")
  public String addProduct(
      @RequestParam String name,
      @RequestParam String pricingMode,
      @RequestParam(required = false) String costPerKg,
      @RequestParam(required = false) String costPerUnit,
      @RequestParam(required = false) String kgPerUnit,
      @RequestParam(required = false) String packagingCostPerSale,
      @RequestParam(required = false) String inboundFreightPerSale,
      RedirectAttributes redirect) {
    try {
      PricingMode mode = PricingMode.valueOf(pricingMode);
      var cKg = BigDecimalParser.parseNullable(costPerKg);
      var cUnit = BigDecimalParser.parseNullable(costPerUnit);
      var kgU = BigDecimalParser.parseNullable(kgPerUnit);
      var pack = BigDecimalParser.parseNullable(packagingCostPerSale);
      var freight = BigDecimalParser.parseNullable(inboundFreightPerSale);

      if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Informe o nome do produto.");
      }
      if (mode == PricingMode.BY_KG && (cKg == null || cKg.compareTo(java.math.BigDecimal.ZERO) < 0)) {
        throw new IllegalArgumentException("No modo por kg, informe o custo por kg.");
      }
      if (mode == PricingMode.BY_UNIT && (cUnit == null || cUnit.compareTo(java.math.BigDecimal.ZERO) < 0)) {
        throw new IllegalArgumentException("No modo por unidade, informe o custo por unidade.");
      }

      dashboardService.addProduct(name, mode, cKg, cUnit, kgU, pack, freight);
      redirect.addFlashAttribute("msgSuccess", "Produto adicionado.");
    } catch (Exception e) {
      redirect.addFlashAttribute("msgError", e.getMessage() != null ? e.getMessage() : "Não foi possível salvar o produto.");
    }
    return "redirect:/#produtos-lista";
  }

  @PostMapping("/produtos/logistica")
  public String updateProductLogistics(
      @RequestParam long productId,
      @RequestParam(required = false) String packagingCostPerSale,
      @RequestParam(required = false) String inboundFreightPerSale,
      RedirectAttributes redirect) {
    try {
      var pack = BigDecimalParser.parseNullable(packagingCostPerSale);
      var freight = BigDecimalParser.parseNullable(inboundFreightPerSale);
      dashboardService.updateProductLogistics(productId, pack, freight);
      redirect.addFlashAttribute("msgSuccess", "Custos de embalagem/frete atualizados.");
    } catch (Exception e) {
      redirect.addFlashAttribute("msgError", e.getMessage());
    }
    return "redirect:/#produtos-lista";
  }

  @PostMapping("/produtos/{id}/excluir")
  public String deleteProduct(@PathVariable long id, RedirectAttributes redirect) {
    dashboardService.deleteProduct(id);
    redirect.addFlashAttribute("msgSuccess", "Produto removido.");
    return "redirect:/";
  }

  @PostMapping("/operacional")
  public String addOperational(
      @RequestParam String name,
      @RequestParam String monthlyAmount,
      RedirectAttributes redirect) {
    try {
      if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Informe o nome do custo.");
      }
      var amt = BigDecimalParser.parseRequired(monthlyAmount, "Valor mensal inválido.");
      dashboardService.addOperationalLine(name, amt);
      redirect.addFlashAttribute("msgSuccess", "Custo operacional adicionado.");
    } catch (Exception e) {
      redirect.addFlashAttribute("msgError", e.getMessage());
    }
    return "redirect:/";
  }

  @PostMapping("/operacional/{id}/excluir")
  public String deleteOperational(@PathVariable long id, RedirectAttributes redirect) {
    dashboardService.deleteOperationalLine(id);
    redirect.addFlashAttribute("msgSuccess", "Linha removida.");
    return "redirect:/";
  }

  @PostMapping("/configuracao")
  public String updateConfig(
      @RequestParam String allocationMode,
      @RequestParam(required = false) String monthlyTotalKgSold,
      @RequestParam(required = false) String monthlyTotalUnitsSold,
      @RequestParam String targetMarginPercent,
      @RequestParam(required = false) String taxPercentOnInvoice,
      @RequestParam(required = false) String mercadoLivreUsePremium,
      RedirectAttributes redirect) {
    try {
      AllocationMode mode = AllocationMode.valueOf(allocationMode);
      var kg = BigDecimalParser.parseNullable(monthlyTotalKgSold);
      var units = BigDecimalParser.parseNullable(monthlyTotalUnitsSold);
      var margin = BigDecimalParser.parseRequired(targetMarginPercent, "Margem alvo inválida.");
      if (margin.compareTo(java.math.BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("Margem não pode ser negativa.");
      }
      var tax = BigDecimalParser.parseNullable(taxPercentOnInvoice);
      if (tax != null && tax.compareTo(java.math.BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("Imposto não pode ser negativo.");
      }
      boolean mlPrem = "true".equalsIgnoreCase(mercadoLivreUsePremium);
      dashboardService.updateSettings(mode, kg, units, margin, tax, mlPrem);
      redirect.addFlashAttribute("msgSuccess", "Configuração salva.");
    } catch (Exception e) {
      redirect.addFlashAttribute("msgError", e.getMessage());
    }
    return "redirect:/";
  }

  @PostMapping("/marketplaces/{id}")
  public String updateMarketplace(
      @PathVariable long id,
      @RequestParam String feePercent,
      @RequestParam(required = false) String fixedFeePerSale,
      @RequestParam(required = false) String feePercentPremium,
      @RequestParam(required = false) String fixedFeePerSalePremium,
      RedirectAttributes redirect) {
    try {
      var pct = BigDecimalParser.parseRequired(feePercent, "Percentual inválido.");
      var fix = BigDecimalParser.parseNullable(fixedFeePerSale);
      var pctPrem = BigDecimalParser.parseNullable(feePercentPremium);
      var fixPrem = BigDecimalParser.parseNullable(fixedFeePerSalePremium);
      if (pct.compareTo(java.math.BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("Percentual não pode ser negativo.");
      }
      dashboardService.updateMarketplace(
          id, pct, fix == null ? java.math.BigDecimal.ZERO : fix, pctPrem, fixPrem);
      redirect.addFlashAttribute("msgSuccess", "Taxas do marketplace atualizadas.");
    } catch (Exception e) {
      redirect.addFlashAttribute("msgError", e.getMessage());
    }
    return "redirect:/";
  }
}
