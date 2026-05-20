import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class test_password {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = args.length > 0 ? args[0] : "123456";
        String encoded = encoder.encode(rawPassword);
        System.out.println("Raw: " + rawPassword);
        System.out.println("Encoded: " + encoded);
        
        // Test existing hash
        String existingHash = "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOh1d4f6O2";
        System.out.println("Matches 123456: " + encoder.matches("123456", existingHash));
        System.out.println("Matches admin123: " + encoder.matches("admin123", existingHash));
    }
}
