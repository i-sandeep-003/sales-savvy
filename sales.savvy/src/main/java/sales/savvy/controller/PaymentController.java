package sales.savvy.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // Important for React localhost
public class PaymentController {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    // ✅ Create order (Recommended Way)
    @PostMapping("/create-order")
    public ResponseEntity<Object> createOrder(@RequestBody Map<String, Integer> payload) throws RazorpayException {
        int amount = payload.get("amount");
        System.out.println("Received amount: " + amount);
        RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100); // Razorpay needs amount in paise
        orderRequest.put("currency", "INR");
        Order order = razorpay.orders.create(orderRequest);
        return ResponseEntity.ok(order.toJson());
    }

    // ✅ Payment callback (if used)
    @PostMapping("/payment-callback")
    public RedirectView paymentCallback(
            @RequestParam("razorpay_order_id") String razorpayOrderId,
            @RequestParam("razorpay_payment_id") String razorpayPaymentId,
            @RequestParam("razorpay_signature") String razorpaySignature) throws RazorpayException {
        String signature = razorpayOrderId + "|" + razorpayPaymentId;
        boolean isValid = Utils.verifySignature(signature, razorpaySignature, keySecret);
        if (isValid) {
            return new RedirectView("/success.html?orderId=" + razorpayOrderId);
        } else {
            return new RedirectView("/failure.html");
        }
    }

    // ✅ Get key ID
    @PostMapping("/get-key")
    public String getKey() {
        return keyId;
    }
}
