package br.com.lojacustos.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component("calc")
public class CalcViewHelper {

  private static final BigDecimal HUNDRED = new BigDecimal("100");

  /** Largura da barra (0–100) para comparar margens de contribuição entre canais. */
  public int barPercent(BigDecimal contributionMargin, BigDecimal maxAmongChannels) {
    if (contributionMargin == null
        || maxAmongChannels == null
        || maxAmongChannels.compareTo(BigDecimal.ZERO) <= 0) {
      return 0;
    }
    if (contributionMargin.compareTo(BigDecimal.ZERO) <= 0) {
      return 0;
    }
    BigDecimal pct =
        contributionMargin.multiply(HUNDRED).divide(maxAmongChannels, 0, RoundingMode.HALF_UP);
    if (pct.compareTo(HUNDRED) > 0) {
      return 100;
    }
    return Math.max(0, pct.intValue());
  }
}
