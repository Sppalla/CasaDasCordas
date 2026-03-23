package br.com.lojacustos.web.support;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class BigDecimalParser {

  private BigDecimalParser() {}

  public static BigDecimal parseNullable(String raw) {
    if (raw == null) {
      return null;
    }
    String s = raw.trim();
    if (s.isEmpty()) {
      return null;
    }
    s = s.replace("R$", "").trim();
    if (s.contains(",") && s.contains(".")) {
      // assume pt-BR: 1.234,56
      s = s.replace(".", "").replace(",", ".");
    } else if (s.contains(",")) {
      s = s.replace(",", ".");
    }
    try {
      return new BigDecimal(s);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Número inválido: " + raw);
    }
  }

  public static BigDecimal parseRequired(String raw, String errorMessage) {
    BigDecimal v = parseNullable(raw);
    if (v == null) {
      throw new IllegalArgumentException(errorMessage);
    }
    return v;
  }

  /** Formatação simples para exibição pt-BR (2 casas). */
  public static String formatBr(BigDecimal value) {
    if (value == null) {
      return "—";
    }
    DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(new Locale("pt", "BR"));
    DecimalFormat df = new DecimalFormat("#,##0.00", sym);
    return df.format(value);
  }
}
