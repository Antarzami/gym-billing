package com.abhijit.gymbilling.controller;

import com.abhijit.gymbilling.entity.Customer;
import com.abhijit.gymbilling.repository.CustomerRepository;
import com.abhijit.gymbilling.service.WhatsAppService;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final WhatsAppService whatsAppService;

    public CustomerController(CustomerRepository customerRepository,
                              WhatsAppService whatsAppService) {
        this.customerRepository = customerRepository;
        this.whatsAppService = whatsAppService;
    }

    // ================= HOME PAGE =================
    @GetMapping("/")
    public String home(Model model) {

        List<Customer> customers = customerRepository.findAll();

        double totalRevenue = 0;
        double totalPending = 0;

        for (Customer c : customers) {
            double paid = c.getPaidAmount() != null ? c.getPaidAmount() : 0.0;
            double total = c.getTotalAmount() != null ? c.getTotalAmount() : 0.0;

            totalRevenue += paid;
            totalPending += (total - paid);
        }

        model.addAttribute("customer", new Customer());
        model.addAttribute("customers", customers);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalPending", totalPending);
        model.addAttribute("totalMembers", customers.size());

        return "index";
    }

    // ================= UPCOMING PAYMENTS PAGE =================
    // ================= UPCOMING PAYMENTS PAGE =================
    @GetMapping("/upcoming-payments")
    public String upcomingPayments(Model model) {

        LocalDate today = LocalDate.now();
        LocalDate next7Days = today.plusDays(7);

        List<Customer> customers = customerRepository.findAll();

        List<Customer> upcomingCustomers = customers.stream()
                .filter(c ->
                        c.getDueDate() != null &&
                                !c.getDueDate().isBefore(today) &&
                                !c.getDueDate().isAfter(next7Days) &&
                                !"PAID".equalsIgnoreCase(c.getStatus())
                )
                .sorted((a, b) -> a.getDueDate().compareTo(b.getDueDate()))
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("upcomingCustomers", upcomingCustomers);

        return "upcoming-payments";
    }

    // ================= LIVE SEARCH =================
    @GetMapping("/customers/search")
    @ResponseBody
    public List<Customer> searchCustomers(@RequestParam String keyword) {
        return customerRepository
                .findByNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(keyword, keyword);
    }

    // ================= SAVE CUSTOMER =================
    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute Customer customer) {

        double paid = customer.getPaidAmount() != null ? customer.getPaidAmount() : 0.0;
        double total = customer.getTotalAmount() != null ? customer.getTotalAmount() : 0.0;

        customer.setPaidAmount(paid);
        customer.setTotalAmount(total);

        if (paid >= total) {
            customer.setStatus("PAID");
        } else {
            customer.setStatus("PENDING");
        }

        customerRepository.save(customer);

        try {
            String message = "Thank you " + customer.getName() +
                    " for joining AAA GYM 💪\n" +
                    "Status: " + customer.getStatus() + "\n" +
                    "Paid: " + paid + "\n" +
                    "Total: " + total + "\n" +
                    "Due Date: " + customer.getDueDate();

            whatsAppService.sendMessage(customer.getPhoneNumber(), message);
        } catch (Exception e) {
            System.out.println("WhatsApp sending failed: " + e.getMessage());
        }

        return "redirect:/";
    }

    // ================= DELETE CUSTOMER =================
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        customerRepository.deleteById(id);
        return "redirect:/";
    }

    // ================= EDIT CUSTOMER =================
    @GetMapping("/edit/{id}")
    public String editCustomer(@PathVariable Long id, Model model) {

        Customer customer = customerRepository.findById(id).orElse(null);

        if (customer == null) {
            return "redirect:/";
        }

        List<Customer> customers = customerRepository.findAll();

        double totalRevenue = 0;
        double totalPending = 0;

        for (Customer c : customers) {
            double paid = c.getPaidAmount() != null ? c.getPaidAmount() : 0.0;
            double total = c.getTotalAmount() != null ? c.getTotalAmount() : 0.0;

            totalRevenue += paid;
            totalPending += (total - paid);
        }

        model.addAttribute("customer", customer);
        model.addAttribute("customers", customers);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalPending", totalPending);
        model.addAttribute("totalMembers", customers.size());

        return "index";
    }

    // ================= PDF INVOICE =================
    @GetMapping("/invoice/{id}")
    public void generateInvoice(@PathVariable Long id,
                                HttpServletResponse response) throws Exception {

        Customer customer = customerRepository.findById(id).orElse(null);

        if (customer == null) {
            response.sendRedirect("/");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=invoice_" + customer.getId() + ".pdf");

        Document document = new Document();
        OutputStream out = response.getOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();
        document.add(new Paragraph("AAA GYM INVOICE"));
        document.add(new Paragraph("-----------------------------------"));
        document.add(new Paragraph("Customer Name: " + customer.getName()));
        document.add(new Paragraph("Phone: " + customer.getPhoneNumber()));
        document.add(new Paragraph("Billing Date: " + customer.getBillingDate()));
        document.add(new Paragraph("Due Date: " + customer.getDueDate()));
        document.add(new Paragraph("Total Amount: " + customer.getTotalAmount()));
        document.add(new Paragraph("Paid Amount: " + customer.getPaidAmount()));
        document.add(new Paragraph("Status: " + customer.getStatus()));
        document.add(new Paragraph("-----------------------------------"));
        document.add(new Paragraph("Thank you for training with us! 💪"));

        document.close();
        out.close();
    }

    // ================= MANUAL REMINDER =================
    @GetMapping("/manualReminder/{id}")
    @ResponseBody
    public String manualReminder(@PathVariable Long id) {

        Customer customer = customerRepository.findById(id).orElse(null);

        if (customer == null) {
            return "Customer not found";
        }

        try {
            String message = "Hello " + customer.getName() +
                    ", your gym payment is due on " +
                    customer.getDueDate() + ". Please clear it soon 💪";

            whatsAppService.sendMessage(customer.getPhoneNumber(), message);

            return "Reminder sent successfully!";
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }
}
