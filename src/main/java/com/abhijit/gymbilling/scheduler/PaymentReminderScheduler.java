package com.abhijit.gymbilling.scheduler;

import com.abhijit.gymbilling.entity.Customer;
import com.abhijit.gymbilling.repository.CustomerRepository;
import com.abhijit.gymbilling.service.WhatsAppService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PaymentReminderScheduler {

    private final CustomerRepository customerRepository;
    private final WhatsAppService whatsAppService;

    public PaymentReminderScheduler(CustomerRepository customerRepository,
                            WhatsAppService whatsAppService) {
        this.customerRepository = customerRepository;
        this.whatsAppService = whatsAppService;
    }

    // ✅ RUNS DAILY AT 9:00 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkDuePayments() {

        System.out.println("Checking due payments...");

        List<Customer> customers = customerRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Customer customer : customers) {

            if (customer.getDueDate() == null) continue;

            LocalDate reminderDate = customer.getDueDate().minusDays(2);

            if (!customer.isReminderSent() &&
                    today.isEqual(reminderDate) &&
                    !"PAID".equalsIgnoreCase(customer.getStatus())) {

                try {
                    String message = "Hello " + customer.getName() +
                            ", your AAA gym payment is due on " +
                            customer.getDueDate() +
                            ". Please make payment soon for continuing the services. 💪";

                    whatsAppService.sendMessage(customer.getPhoneNumber(), message);

                    customer.setReminderSent(true);
                    customerRepository.save(customer);

                    System.out.println("Reminder sent to: " + customer.getName());

                } catch (Exception e) {
                    System.out.println("Failed sending reminder: " + e.getMessage());
                }
            }
        }
    }
}
