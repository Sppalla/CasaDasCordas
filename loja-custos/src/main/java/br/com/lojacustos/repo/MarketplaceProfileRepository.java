package br.com.lojacustos.repo;

import br.com.lojacustos.domain.MarketplaceProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketplaceProfileRepository extends JpaRepository<MarketplaceProfile, Long> {

  Optional<MarketplaceProfile> findByCode(String code);
}
