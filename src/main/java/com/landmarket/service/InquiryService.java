package com.landmarket.service;

import com.landmarket.dto.InquiryRequest;
import com.landmarket.model.Inquiry;
import com.landmarket.model.Land;
import com.landmarket.model.User;
import com.landmarket.repository.InquiryRepository;
import com.landmarket.repository.LandRepository;
import com.landmarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InquiryService {

    @Autowired private InquiryRepository inquiryRepository;
    @Autowired private LandRepository landRepository;
    @Autowired private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    // Send inquiry (buyer contacts seller directly)
    @Transactional
    public Inquiry sendInquiry(Long landId, InquiryRequest request, String buyerEmail) {

        Land land = landRepository.findById(landId)
                .orElseThrow(() -> new RuntimeException("Land not found"));

        Inquiry inquiry = Inquiry.builder()
                .land(land)
                .message(request.getMessage())
                .status(Inquiry.InquiryStatus.PENDING)
                .build();

        String finalBuyerEmail = null;

        // Logged-in user
        if (buyerEmail != null) {
            User buyer = userRepository.findByEmail(buyerEmail).orElse(null);
            if (buyer != null) {
                inquiry.setBuyer(buyer);
                inquiry.setInquirerName(buyer.getFullName());
                inquiry.setInquirerPhone(buyer.getPhone());
                inquiry.setInquirerEmail(buyer.getEmail());

                finalBuyerEmail = buyer.getEmail();
            }
        } else {
            // Guest user
            inquiry.setInquirerName(request.getInquirerName());
            inquiry.setInquirerPhone(request.getInquirerPhone());
            inquiry.setInquirerEmail(request.getInquirerEmail());

            finalBuyerEmail = request.getInquirerEmail();
        }

        // ✅ Save inquiry
        Inquiry savedInquiry = inquiryRepository.save(inquiry);

        // ✅ Send email
        try {
            emailService.sendInquiryEmail(
                    land.getOwner().getEmail(),
                    finalBuyerEmail,
                    request.getMessage()
            );
        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }

        return savedInquiry;
    }

    // Get inquiries for a specific land (seller view)
    public List<Inquiry> getInquiriesForLand(Long landId, String sellerEmail) {
        Land land = landRepository.findById(landId)
                .orElseThrow(() -> new RuntimeException("Land not found"));

        if (!land.getOwner().getEmail().equals(sellerEmail)) {
            throw new RuntimeException("Access denied");
        }

        return inquiryRepository.findByLand(land);
    }

    // Get all inquiries received by a seller
    public List<Inquiry> getMyReceivedInquiries(String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return inquiryRepository.findByLandOwner(seller);
    }

    // Get inquiries sent by buyer
    public List<Inquiry> getMySentInquiries(String buyerEmail) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return inquiryRepository.findByBuyer(buyer);
    }
    
    
}
