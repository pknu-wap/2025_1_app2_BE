package com.wap.app2.gachitayo.service.fare;

import com.wap.app2.gachitayo.repository.fare.PaymentStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentStatusService {
    private final PaymentStatusRepository paymentStatusRepository;


}
