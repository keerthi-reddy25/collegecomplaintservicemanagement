package util;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * All keyboard input goes through this class.
 * Every method keeps asking until the user types a valid value,
 * so wrong input never crashes the program.
 */
public class InputUtil {

    private static final Scanner SCANNER = new Scanner(System.in);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    private InputUtil() {
    }

    /** Reads one line (may be empty). Exits cleanly if the input stream is closed. */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        if (!SCANNER.hasNextLine()) {
            System.out.println();
            System.out.println("Input ended. Exiting the application.");
            System.exit(0);
        }
        return SCANNER.nextLine().trim();
    }

    /** Reads a non-empty text with at most maxLength characters. */
    public static String readRequired(String prompt, int maxLength) {
        while (true) {
            String value = readLine(prompt);
            if (value.isEmpty()) {
                System.out.println("This field cannot be empty. Please try again.");
            } else if (value.length() > maxLength) {
                System.out.println("Too long. Maximum " + maxLength + " characters allowed.");
            } else {
                return value;
            }
        }
    }

    /** Reads a whole number. Keeps asking until the user types digits. */
    public static int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter digits only.");
            }
        }
    }

    /** Reads a whole number between min and max (both included). */
    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }

    /** Reads an e-mail address in a valid format (returned in lower case). */
    public static String readEmail(String prompt) {
        while (true) {
            String value = readRequired(prompt, 100);
            if (EMAIL_PATTERN.matcher(value).matches()) {
                return value.toLowerCase();
            }
            System.out.println("Invalid email format. Example: name@college.com");
        }
    }

    /** Reads a 10-digit phone number. */
    public static String readPhone(String prompt) {
        while (true) {
            String value = readRequired(prompt, 15);
            if (PHONE_PATTERN.matcher(value).matches()) {
                return value;
            }
            System.out.println("Phone number must contain exactly 10 digits.");
        }
    }

    /** Reads a new password with at least 6 characters. */
    public static String readNewPassword(String prompt) {
        while (true) {
            String value = readRequired(prompt, 50);
            if (value.length() >= 6) {
                return value;
            }
            System.out.println("Password must be at least 6 characters long.");
        }
    }

    /** Asks a yes/no question. */
    public static boolean readYesNo(String prompt) {
        while (true) {
            String value = readLine(prompt + " (y/n): ").toLowerCase();
            if (value.equals("y") || value.equals("yes")) {
                return true;
            }
            if (value.equals("n") || value.equals("no")) {
                return false;
            }
            System.out.println("Please type y or n.");
        }
    }
}
