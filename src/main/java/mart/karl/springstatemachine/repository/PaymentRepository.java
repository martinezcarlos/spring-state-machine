package mart.karl.springstatemachine.repository;

import mart.karl.springstatemachine.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {}
