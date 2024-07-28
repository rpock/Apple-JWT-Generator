package biz.pock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.Scanner;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class AppleJWTGenerator {
    private String keyId;
    private String teamId;
    private String clientId;
    private String keyFilePath;
    private Scanner scanner;
    private String errorMessage;

    // ANSI color codes
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                                                        .withZone(ZoneId.systemDefault());

    public AppleJWTGenerator() {
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        while (true) {
            clearScreen();
            printMenu();
            if (errorMessage != null) {
                System.out.println(errorMessage);
                System.out.println();
                errorMessage = null;
            }
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim().toLowerCase();
            
            switch (choice) {
                case "1":
                    setKeyId();
                    break;
                case "2":
                    setTeamId();
                    break;
                case "3":
                    setClientId();
                    break;
                case "4":
                    setKeyFilePath();
                    break;
                case "5":
                    generateJWT();
                    break;
                case "0":
                    clearScreen();
                    System.out.println(ANSI_YELLOW + "\nProgram is terminating. Goodbye!" + ANSI_RESET);
                    return;
                default:
                    errorMessage = ANSI_RED + "\nInvalid input. Please try again." + ANSI_RESET;
            }
        }
    }

    private void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // If clearing the screen fails, we simply print some empty lines
            for (int i = 0; i < 50; ++i) System.out.println();
        }
    }

    private void printMenu() {
        String topBottom = "╔══════════════════════════════════════════════════╗";
        String middle = "╠══════════════════════════════════════════════════╣";
        String side = "║";

        System.out.println("\n" + topBottom);
        System.out.println(side + "       A P P L E   J W T   G E N E R A T O R      " + side);
        System.out.println(middle);
        System.out.println(side + getSpaces(50) + side);
        System.out.println(side + ANSI_CYAN + " 1. Set Key ID " + formatValue(keyId) + ANSI_RESET + getSpaces(50 - 15 - (keyId != null ? keyId.length() + 2 : 7)) + side);
        System.out.println(side + ANSI_GREEN + " 2. Set Team ID " + formatValue(teamId) + ANSI_RESET + getSpaces(50 - 16 - (teamId != null ? teamId.length() + 2 : 7)) + side);
        System.out.println(side + ANSI_YELLOW + " 3. Set Client ID " + formatValue(clientId) + ANSI_RESET + getSpaces(50 - 18 - (clientId != null ? clientId.length() + 2 : 7)) + side);
        System.out.println(side + ANSI_BLUE + " 4. Set Key File Path " + formatValue(shortenPath(keyFilePath, 20)) + ANSI_RESET + getSpaces(50 - 22 - (keyFilePath != null ? Math.min(keyFilePath.length(), 20) + 2 : 7)) + side);
        System.out.println(side + ANSI_PURPLE + " 5. Generate JWT" + ANSI_RESET + getSpaces(34) + side);
        System.out.println(side + getSpaces(50) + side);
        System.out.println(side + ANSI_RED + " 0. Exit" + ANSI_RESET + getSpaces(42) + side);
        System.out.println(side + getSpaces(38) + "by pock.biz " + side);
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    private String formatValue(String value) {
        return value != null ? "[" + value + "]" : "[empty]";
    }

    private String getSpaces(int count) {
        return " ".repeat(Math.max(0, count));
    }

    private String shortenPath(String path, int maxLength) {
        if (path == null) return null;
        if (path.length() <= maxLength) return path;
        int startLength = maxLength / 2 - 2;
        int endLength = maxLength / 2 - 1;
        return path.substring(0, startLength) + "..." + path.substring(path.length() - endLength);
    }

    private void setKeyId() {
        System.out.print(ANSI_CYAN + "Enter the Key ID: " + ANSI_RESET);
        this.keyId = scanner.nextLine().trim();
    }

    private void setTeamId() {
        System.out.print(ANSI_GREEN + "Enter the Team ID: " + ANSI_RESET);
        this.teamId = scanner.nextLine().trim();
    }

    private void setClientId() {
        System.out.print(ANSI_YELLOW + "Enter the Client ID: " + ANSI_RESET);
        this.clientId = scanner.nextLine().trim();
    }

    private void setKeyFilePath() {
        System.out.print(ANSI_BLUE + "Enter the Key File Path: " + ANSI_RESET);
        String inputPath = scanner.nextLine().trim();
        File file = new File(inputPath);
        if (file.exists() && file.isFile()) {
            this.keyFilePath = inputPath;
            if (this.keyFilePath.length() > 20) {
                errorMessage = ANSI_BLUE + "Full path: " + this.keyFilePath + ANSI_RESET;
            }
        } else {
            errorMessage = ANSI_RED + "Error: The specified file does not exist or is not a file." + ANSI_RESET;
        }
    }

    private void generateJWT() {
        if (keyId == null || teamId == null || clientId == null || keyFilePath == null) {
            errorMessage = ANSI_RED + "Error: All fields must be filled before generating a JWT." + ANSI_RESET;
            return;
        }

        try {
            Instant now = Instant.now();
            Instant expirationTime = now.plusSeconds(15777000); // 6 months
            String jwt = generateJwt(keyId, teamId, clientId, keyFilePath, now, expirationTime);
            
            errorMessage = ANSI_GREEN + "Generated JWT: \n" + ANSI_RESET + jwt + "\n\n" +
            ANSI_GREEN + "Generation time: " + ANSI_RESET + formatter.format(now) + "\n" +
            ANSI_GREEN + "Expiration time: " + ANSI_RESET + formatter.format(expirationTime);
        } catch (Exception e) {
            errorMessage = ANSI_RED + "Error generating JWT: " + e.getMessage() + ANSI_RESET;
        }
    }

    private String generateJwt(String keyId, String teamId, String clientId, String keyFilePath, Instant now, Instant expirationTime)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        
        String privateKeyContent = new String(Files.readAllBytes(Paths.get(keyFilePath)));
        PrivateKey privateKey = getPrivateKey(privateKeyContent);

        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setHeaderParam("alg", "ES256")
                .setIssuer(teamId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expirationTime))
                .setAudience("https://appleid.apple.com")
                .setSubject(clientId)
                .setId(UUID.randomUUID().toString())
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }

    private PrivateKey getPrivateKey(String privateKeyContent) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        privateKeyContent = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        return kf.generatePrivate(keySpec);
    }

    public static void main(String[] args) {
        new AppleJWTGenerator().run();
    }
}