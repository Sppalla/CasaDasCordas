package br.com.lojacustos.web;

import br.com.lojacustos.service.CostCalculationService.MarketplaceQuote;
import br.com.lojacustos.service.DashboardService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CsvExportController {

  private final DashboardService dashboardService;

  public CsvExportController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/export/produtos.csv")
  public ResponseEntity<byte[]> exportProductsCsv() {
    var data = dashboardService.load();
    StringBuilder sb = new StringBuilder();
    sb.append('\uFEFF');
    sb.append(
        "Produto;Modo;Custo variavel;Custo loja (c/ rateio);Imposto %;Margem alvo %;Canal;Preco sugerido;Taxa canal;Imposto R$;Liquido;Margem contrib R$;Margem contrib %;Markup;Ponto equilibrio (un);Observacao\n");
    var tax = data.settings().getTaxPercentOnInvoice();
    var margin = data.settings().getTargetMarginPercent();
    for (var row : data.rows()) {
      String mode =
          row.product().getPricingMode().name().equals("BY_KG") ? "Por kg" : "Por unidade";
      String vCost =
          row.storeCost().valid()
              ? dec(row.storeCost().variableCost())
              : "";
      String loaded =
          row.storeCost().valid() ? dec(row.storeCost().storeCost()) : "";
      if (!row.storeCost().valid()) {
        sb.append(csv(row.product().getName()))
            .append(';')
            .append(mode)
            .append(";;;")
            .append(dec(tax))
            .append(';')
            .append(dec(margin))
            .append(";;;;;;;;;")
            .append(';')
            .append(csv(row.storeCost().errorMessage()))
            .append('\n');
        continue;
      }
      for (MarketplaceQuote q : row.marketplaceQuotes()) {
        sb.append(csv(row.product().getName()))
            .append(';')
            .append(mode)
            .append(';')
            .append(vCost)
            .append(';')
            .append(loaded)
            .append(';')
            .append(dec(tax))
            .append(';')
            .append(dec(margin))
            .append(';')
            .append(csv(q.marketplaceName()))
            .append(';')
            .append(q.suggestedSalePrice() != null ? dec(q.suggestedSalePrice()) : "")
            .append(';')
            .append(q.estimatedTotalFee() != null ? dec(q.estimatedTotalFee()) : "")
            .append(';')
            .append(q.estimatedTaxAmount() != null ? dec(q.estimatedTaxAmount()) : "")
            .append(';')
            .append(q.estimatedNetAfterFees() != null ? dec(q.estimatedNetAfterFees()) : "")
            .append(';')
            .append(q.contributionMargin() != null ? dec(q.contributionMargin()) : "")
            .append(';')
            .append(q.contributionMarginPercent() != null ? dec(q.contributionMarginPercent()) : "")
            .append(';')
            .append(q.markup() != null ? dec(q.markup()) : "")
            .append(';')
            .append(q.breakEvenUnits() != null ? dec(q.breakEvenUnits()) : "")
            .append(';')
            .append(csv(q.errorMessage() != null ? q.errorMessage() : ""))
            .append('\n');
      }
    }
    byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
    headers.setContentDisposition(
        ContentDisposition.attachment().filename("produtos-loja-custos.csv", StandardCharsets.UTF_8).build());
    return ResponseEntity.ok().headers(headers).body(bytes);
  }

  private static String dec(BigDecimal b) {
    return b == null ? "" : b.toPlainString().replace('.', ',');
  }

  private static String csv(String s) {
    if (s == null) {
      return "";
    }
    String t = s.replace("\"", "\"\"");
    if (t.contains(";") || t.contains("\"") || t.contains("\n") || t.contains("\r")) {
      return "\"" + t + "\"";
    }
    return t;
  }
}
