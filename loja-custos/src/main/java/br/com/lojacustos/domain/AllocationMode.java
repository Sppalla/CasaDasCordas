package br.com.lojacustos.domain;

public enum AllocationMode {
  /** Sem rateio de custos operacionais no custo do produto */
  NONE,
  /** Rateio = total operacional ÷ kg vendidos no mês */
  BY_TOTAL_KG,
  /** Rateio = total operacional ÷ unidades vendidas no mês */
  BY_TOTAL_UNITS
}
