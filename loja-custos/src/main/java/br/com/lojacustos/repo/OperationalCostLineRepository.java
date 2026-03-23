package br.com.lojacustos.repo;

import br.com.lojacustos.domain.OperationalCostLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationalCostLineRepository extends JpaRepository<OperationalCostLine, Long> {}
